using System;
using System.ComponentModel;
using Windows.System;
using System.Reactive.Linq;
using Windows.Devices.Input;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Mosaic;
using Fmg.Core.Types;
using Fmg.Core.Mosaic.Cells;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.Mosaic {

    /// <summary> MVC: controller. UWP implementation over control <see cref="FrameworkElement"/> </summary>
    /// <typeparam name="TImageAsFrameworkElement">image-control based of <see cref="FrameworkElement"/></typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicView">mosaic view</typeparam>
    public abstract class MosaicFrameworkElementController<TImageAsFrameworkElement, TImageInner, TMosaicView>
                                        : MosaicController<TImageAsFrameworkElement, TImageInner, TMosaicView, IMosaicDrawModel<TImageInner>>
        where TImageAsFrameworkElement : FrameworkElement
        where TImageInner : class
        where TMosaicView : IMosaicView<TImageAsFrameworkElement, TImageInner, IMosaicDrawModel<TImageInner>>
    {

        private readonly ClickInfo _clickInfo = new ClickInfo();
        /// <summary>
        /// true : bind Control.SizeProperty to Model.Size
        /// false: bind Model.Size to Control.SizeProperty
        /// </summary>
        public bool BindSizeDirection { get; set; } = true;
        private bool _extendedManipulation = false;
        #region if ExtendedManipulation
        /// <summary> мин отступ от краев экрана для мозаики </summary>
        private readonly double MinIndent = (30.0).DpToPx();
        private const bool DeferredZoom = true;
        private bool _manipulationStarted;
        private bool _turnX;
        private bool _turnY;
        private DateTime _dtInertiaStarting;
        private static double? _baseWheelDelta;
        private IDisposable _areaScaleObservable;
        private Transform _originalTransform;
        private CompositeTransform _scaleTransform;
        private double _deferredArea;

        private class ZoomStartInfo {
            public Windows.Foundation.Point _devicePosition;
            public SizeDouble _mosaicSize;
            public SizeDouble _mosaicOffset;
        }
        private ZoomStartInfo _zoomStartInfo;


        private sealed class NeedAreaChangingEventArgs { }
        private delegate void NeedAreaChangingEventHandler(object sender, NeedAreaChangingEventArgs ev);
        private event NeedAreaChangingEventHandler NeedAreaChanging;

        #endregion

        protected MosaicFrameworkElementController(TMosaicView view)
            : base(view)
        {
            SubscribeToControl();
        }

        public abstract TImageAsFrameworkElement Control { get; }

        public bool ExtendedManipulation {
            get => _extendedManipulation;
            set {
                if (_notifier.SetProperty(ref _extendedManipulation, value)) {
                    UnsubscribeToControl();
                    SubscribeToControl();
                }
            }
        }

        protected void SubscribeToControl() {
            var ctrl = Control;
            ctrl.Tapped += OnTapped;
            ctrl.DoubleTapped += OnDoubleTapped;
            ctrl.RightTapped += OnRightTapped;
            ctrl.PointerPressed += OnPointerPressed;
            ctrl.PointerReleased += OnPointerReleased;
            ctrl.PointerCaptureLost += OnPointerCaptureLost;
            ctrl.LostFocus += OnFocusLost;
            ctrl.SizeChanged += OnControlSizeChanged;

            if (!ExtendedManipulation)
                return;

            ctrl.PointerWheelChanged += OnPointerWheelChanged;
#if DEBUG
            ctrl.PointerMoved += OnPointerMoved;
#endif
            ctrl.ManipulationStarting        += OnManipulationStarting;
            ctrl.ManipulationStarted         += OnManipulationStarted;
            ctrl.ManipulationInertiaStarting += OnManipulationInertiaStarting;
            ctrl.ManipulationDelta           += OnManipulationDelta;
            ctrl.ManipulationCompleted       += OnManipulationCompleted;
            ctrl.ManipulationMode =
                ManipulationModes.TranslateX |
                ManipulationModes.TranslateY |
                ManipulationModes.Rotate |
                ManipulationModes.Scale |
                ManipulationModes.TranslateInertia;

            _areaScaleObservable = Observable
                .FromEventPattern<NeedAreaChangingEventHandler, NeedAreaChangingEventArgs>(h => NeedAreaChanging += h, h => NeedAreaChanging -= h)
                .Throttle(TimeSpan.FromSeconds(0.7)) // debounce events
                .Subscribe(x => AsyncRunner.InvokeFromUiLater(() => OnDeferredAreaChanging(x.Sender, x.EventArgs), Windows.UI.Core.CoreDispatcherPriority.High));

            ctrl.KeyUp += OnKeyUp;
        }

        protected void UnsubscribeToControl() {
            var ctrl = Control;
            ctrl.Tapped += OnTapped;
            ctrl.DoubleTapped -= OnDoubleTapped;
            ctrl.RightTapped -= OnRightTapped;
            ctrl.PointerPressed -= OnPointerPressed;
            ctrl.PointerReleased -= OnPointerReleased;
            ctrl.PointerCaptureLost -= OnPointerCaptureLost;
            ctrl.LostFocus -= OnFocusLost;
            ctrl.SizeChanged -= OnControlSizeChanged;

            if (!ExtendedManipulation)
                return;

            ctrl.PointerWheelChanged -= OnPointerWheelChanged;
#if DEBUG
            ctrl.PointerMoved -= OnPointerMoved;
#endif
            ctrl.ManipulationStarting        -= OnManipulationStarting;
            ctrl.ManipulationStarted         -= OnManipulationStarted;
            ctrl.ManipulationInertiaStarting -= OnManipulationInertiaStarting;
            ctrl.ManipulationDelta           -= OnManipulationDelta;
            ctrl.ManipulationCompleted       -= OnManipulationCompleted;
            ctrl.ManipulationMode = ManipulationModes.None;
            ctrl.KeyUp -= OnKeyUp;
        }

        private SizeDouble Offset {
            get => Model.MosaicOffset;
            set {
                //using (CreateTracer(GetCallerName(), "" + Model.MosaicOffset, () => "" + Model.MosaicOffset))
                {
                    Model.MosaicOffset = RecheckOffset(value);
                }
            }
        }

        /// <summary> узнаю мах размер площади ячеек мозаики (для размера поля 3x3) так, чтобы поле влазило в текущий размер Control'а </summary>
        /// <returns>макс площадь ячейки</returns>
        private double CalcMaxArea() {
            if (_maxArea.HasValue)
                return _maxArea.Value;
            var mosaicSizeField = new Matrisize(3, 3);
            var size = Model.Size;
            double area = MosaicHelper.FindAreaBySize(this.MosaicType, mosaicSizeField, ref size);
            //System.Diagnostics.Debug.WriteLine("MosaicFrameworkElementController.CalcMaxArea: area="+area);
            _maxArea = area; // caching value
            return area;
        }
        double? _maxArea; // cached value

        private void BeforeZoom(Windows.Foundation.Point? mouseDevicePosition) {
            if (mouseDevicePosition.HasValue) {
                if (_zoomStartInfo == null)
                    _zoomStartInfo = new ZoomStartInfo();
                _zoomStartInfo._devicePosition = mouseDevicePosition.Value;
                _zoomStartInfo._mosaicOffset = Offset;
                _zoomStartInfo._mosaicSize = Model.MosaicSize;
            } else {
                _zoomStartInfo = null;
            }
        }

        private void AfterZoom() {
            if (_zoomStartInfo == null)
                return;

            var devicePos = _zoomStartInfo._devicePosition;
            var mosaicSizeNew = Model.MosaicSize;//GetMosaicSize(Model.SizeField, Model.Area);
            var offsetNew = new SizeDouble(
                        devicePos.X - (devicePos.X - _zoomStartInfo._mosaicOffset.Width ) * mosaicSizeNew.Width  / _zoomStartInfo._mosaicSize.Width,
                        devicePos.Y - (devicePos.Y - _zoomStartInfo._mosaicOffset.Height) * mosaicSizeNew.Height / _zoomStartInfo._mosaicSize.Height);
            Offset = offsetNew;
        }

        /// <summary> Zoom + </summary>
        void ZoomInc(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
            BeforeZoom(mouseDevicePosition);
            if (DeferredZoom) {
                Scale(1.01 * zoomPower);
            } else {
                Model.Area *= 1.01 * zoomPower;
                AfterZoom();
            }
        }

        /// <summary> Zoom - </summary>
        void ZoomDec(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
            BeforeZoom(mouseDevicePosition);
            if (DeferredZoom) {
                Scale(0.99 / zoomPower);
            } else {
                Model.Area *= 0.99 / zoomPower;
                AfterZoom();
            }
        }

        private void Scale(double scaleMul) {
            var ctrl = Control;
            if (!(ctrl.RenderTransform is CompositeTransform)) {
                if (_scaleTransform == null) {
                    _scaleTransform = new CompositeTransform();
                    _originalTransform = ctrl.RenderTransform;
                }
                ctrl.RenderTransform = _scaleTransform;
                _deferredArea = Model.Area;
            }


            _deferredArea *= scaleMul;
            _deferredArea = Math.Min(Math.Max(MosaicInitData.AREA_MINIMUM, _deferredArea), CalcMaxArea()); // recheck

            var deferredMosaicSize = GetMosaicSize(Model.SizeField, _deferredArea);
            var currentMosaicSize = Model.MosaicSize;

            if (_zoomStartInfo != null) {
                var p = _zoomStartInfo._devicePosition;
                _scaleTransform.CenterX = p.X;
                _scaleTransform.CenterY = p.Y;
            }

            _scaleTransform.ScaleX = deferredMosaicSize.Width / currentMosaicSize.Width;
            _scaleTransform.ScaleY = deferredMosaicSize.Height / currentMosaicSize.Height;

            NeedAreaChanging(this, null); // fire event
        }

        private void OnDeferredAreaChanging(object sender, NeedAreaChangingEventArgs ev) {
            Model.Area = _deferredArea;
            AfterZoom();

            // restore
            _scaleTransform.CenterX = _scaleTransform.CenterY = 0;
            _scaleTransform.ScaleX = _scaleTransform.ScaleY = 1;
            Control.RenderTransform = _originalTransform;
        }

        /// <summary> Zoom minimum </summary>
        private void ZoomMin() {
            Model.Area = MosaicInitData.AREA_MINIMUM;
        }

        /// <summary> Zoom maximum </summary>
        private void ZoomMax() {
            var maxArea = CalcMaxArea();
            Model.Area = maxArea;
        }

        protected override void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnModelPropertyChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(Model.Size):
                OnSizeChanged(ev as PropertyChangedExEventArgs<SizeDouble>);
                break;
            case nameof(Model.Area):
                OnAreaChanged(ev as PropertyChangedExEventArgs<double>);
                break;
            }
        }

        private void OnControlSizeChanged(object sender, SizeChangedEventArgs ev) {
            if (!BindSizeDirection)
                Model.Size = ev.NewSize.ToFmSizeDouble();
        }

        private void OnSizeChanged(PropertyChangedExEventArgs<SizeDouble> ev) {
            if (!BindSizeDirection)
                return;
            var control = Control;
            var newSize = ev.NewValue;
            control.Width  = newSize.Width;
            control.Height = newSize.Height;
        }

        private void OnAreaChanged(PropertyChangedExEventArgs<double> ev) {
            if (!ExtendedManipulation)
                return;
            using (var tracer = CreateTracer(GetCallerName(), string.Format("newArea={0:0.00}, oldValue={1:0.00}", ev.NewValue, ev.OldValue))) {
                Offset = Offset; // implicit call RecheckOffset
            }
        }

        private void OnPointerWheelChanged(object sender, PointerRoutedEventArgs ev) {
            //using (CreateTracer()) {
            var wheelDelta = ev.GetCurrentPoint(Control).Properties.MouseWheelDelta;
            if (!_baseWheelDelta.HasValue)
                _baseWheelDelta = Math.Abs(wheelDelta);

            var wheelPower = 1 + ((Math.Abs(wheelDelta) / _baseWheelDelta.Value) - 1) / 10;

            if (wheelDelta > 0)
                ZoomInc(wheelPower, ev.GetCurrentPoint(null).Position);
            else
                ZoomDec(wheelPower, ev.GetCurrentPoint(null).Position);

            ev.Handled = true;
            //}
        }

        bool ClickHandler(ClickResult clickResult) {
            if (clickResult == null)
                return false;
            _clickInfo.CellDown = clickResult.CellDown;
            _clickInfo.IsLeft = clickResult.IsLeft;
            var handled = clickResult.IsAnyChanges;
            if (clickResult.IsDown)
                _clickInfo.DownHandled = handled;
            else
                _clickInfo.UpHandled = handled;
            _clickInfo.Released = !clickResult.IsDown;
            //Logger.Info(">>>>>> _clickInfo= " + _clickInfo + "\n" + System.Environment.StackTrace);
            return handled;
        }

        void OnFocusLost(object sender, RoutedEventArgs ev) {
            //LoggerSimple.Put("<> " + GetCallerName());
            this.MouseFocusLost();
        }
        bool OnClickLost() {
            return ClickHandler(this.MouseFocusLost());
        }

        bool OnClick(Windows.Foundation.Point pos, bool leftClick, bool down) {
            var point = pos.ToFmPointDouble();
#if  false // otherwise not work the long tapping (to setting flag label)
            if (point.X < 0 || point.Y < 0) {
                return clickHandler(new ClickResult(null, leftClick, down));
            }

            var winSize = MosaicField.WindowSize;
            if (point.X > winSize.Width || point.Y > winSize.Height) {
                return clickHandler(new ClickResult(null, leftClick, down));
            }
#endif
            return ClickHandler(down
                ? MousePressed(point, leftClick)
                : MouseReleased(point, leftClick));
        }

        void OnTapped(object sender, TappedRoutedEventArgs ev) {
            //using (new Fmg.Common.Tracer(GetCallerName(), () => "handled=" + ev.Handled))
            {
                if (ev.PointerDeviceType != PointerDeviceType.Mouse) {
                    ev.Handled = OnClick(ev.GetPosition(Control), true, false);
                }
            }
        }

        protected void OnDoubleTapped(object sender, DoubleTappedRoutedEventArgs ev) {
            using (new Tracer(GetCallerName(), null, () => "handled=" + ev.Handled))
            {
                var imgControl = Control;
                var model = Model;
                var mosaicSize = model.MosaicSize;
                var offset = Offset;
                var rcMosaic = new Windows.Foundation.Rect(offset.Width, offset.Height, mosaicSize.Width, mosaicSize.Height);
                if (rcMosaic.Contains(ev.GetPosition(imgControl))) {
                    if (this.GameStatus == EGameStatus.eGSEnd) {
                        this.GameNew();
                        ev.Handled = true;
                    }
                } else {
                    _zoomStartInfo = null;

                    // centered mosaic
                    var size = model.Size;

                    // 1. modify area
                    var tmp = size;
                    model.Area = MosaicHelper.FindAreaBySize(model.MosaicType, model.SizeField, ref tmp);

                    // 2. modify offset
                    mosaicSize = model.MosaicSize; // ! reload value
                    offset.Width  = (size.Width  - mosaicSize.Width ) / 2;
                    offset.Height = (size.Height - mosaicSize.Height) / 2;
                    Offset = offset;

                    ev.Handled = true;
                }
            }
        }

        protected void OnRightTapped(object sender, RightTappedRoutedEventArgs ev) {
            //using (new Fmg.Common.Tracer(GetCallerName(), () => "handled=" + ev.Handled))
            {
                if (ev.PointerDeviceType == PointerDeviceType.Mouse) {
                    ev.Handled = _clickInfo.DownHandled || _clickInfo.UpHandled; // TODO: для избежания появления appBar'ов при установке '?'
                } else if (!_manipulationStarted) {

                    // 1. release left click in invalid coord
                    OnClick(new Windows.Foundation.Point(-1, -1), true, false);

                    // 2. make right click - up & down
                    var imgControl = Control;
                    var pos = ev.GetPosition(imgControl);
                    var handled1 = OnClick(pos, false, true);
                    var handled2 = OnClick(pos, false, false);
                    ev.Handled = handled1 || handled2;
                }
            }
        }

        protected void OnPointerPressed(object sender, PointerRoutedEventArgs ev) {
            var imgControl = Control;
            var currPoint = ev.GetCurrentPoint(imgControl);
            //using (CreateTracer(GetCallerName(), "pointerId=" + currPoint.PointerId, () => "ev.Handled = " + ev.Handled))
            {
                //_clickInfo.PointerDevice = pointerPoint.PointerDevice.PointerDeviceType;
                var props = currPoint.Properties;

                ev.Handled = OnClickLost(); // Protection from the two-finger click.
                //if (_manipulationStarted) {
                //    // touch two-finger
                //    OnClickLost(); // Protection from the two-finger click.
                //}

                if (!ev.Handled)
                    ev.Handled = OnClick(currPoint.Position, props.IsLeftButtonPressed, true);

                _clickInfo.DownHandled = ev.Handled;
            }
        }

        protected void OnPointerReleased(object sender, PointerRoutedEventArgs ev) {
            var imgControl = Control;
            var currPoint = ev.GetCurrentPoint(imgControl);
            //using (CreateTracer(GetCallerName(), string.Format($"pointerId={currPoint.PointerId}, _manipulationStarted={_manipulationStarted}"), () => "ev.Handled=" + ev.Handled))
            {
                //if (_manipulationStarted)
                if (ev.Pointer.PointerDeviceType == PointerDeviceType.Mouse) {
                    var isLeftClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.LeftButtonReleased);
                    var isRightClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.RightButtonReleased);
                    if (isLeftClick || isRightClick) {
                        System.Diagnostics.Debug.Assert(isLeftClick != isRightClick);
                        ev.Handled = OnClick(currPoint.Position, isLeftClick, false);
                    }
                } else {
                    AsyncRunner.InvokeFromUiLater(() => {
                        if (!_clickInfo.Released) {
                            Logger.Info("ã OnPointerReleased: forced left release click...");
                            OnClick(currPoint.Position, true, false);
                        }
                    }, CoreDispatcherPriority.High);
                }

                _clickInfo.UpHandled = ev.Handled;
            }
        }

        protected void OnPointerCaptureLost(object sender, PointerRoutedEventArgs ev) {
            var imgControl = Control;
            var currPoint = ev.GetCurrentPoint(imgControl);
            //using (CreateTracer(GetCallerName(), string.Format($"pointerId={currPoint.PointerId}, _manipulationStarted={_manipulationStarted}"), () => "ev.Handled=" + ev.Handled))
            {
                if (!_clickInfo.Released) {
                    Logger.Info("ã OnPointerCaptureLost: forced left release click...");
                    OnClick(currPoint.Position, true, false);
                }
            }
        }

#if DEBUG
        protected void OnPointerMoved(object sender, PointerRoutedEventArgs ev) {
            Tracer tracer = null;
            //using (tracer = CreateTracer(GetCallerName(), () => "ev.Handled = " + ev.Handled))
            {
                Pointer ptr = ev.Pointer;

                // Multiple, simultaneous mouse button inputs are processed here.
                // Mouse input is associated with a single pointer assigned when mouse input is first detected.
                // Clicking additional mouse buttons (left, wheel, or right) during the interaction creates secondary
                // associations between those buttons and the pointer through the pointer pressed event.
                // The pointer released event is fired only when the last mouse button  associated with the
                // interaction (not necessarily the initial button) is released.
                // Because of this exclusive association, other mouse button clicks are routed through the pointer move event.
                if (ptr.PointerDeviceType == PointerDeviceType.Mouse) {
                    // To get mouse state, we need extended pointer details.
                    // We get the pointer info through the getCurrentPoint method of the event argument.
                    PointerPoint ptrPt = ev.GetCurrentPoint(null);
                    if (ptrPt.Properties.IsLeftButtonPressed) {
                        //tracer.Put("Left button: " + ptrPt.PointerId);
                        tracer?.Put("Left button: " + ptrPt.PointerId);
                    }
                    if (ptrPt.Properties.IsMiddleButtonPressed) {
                        //tracer.Put("Wheel button: " + ptrPt.PointerId);
                        tracer?.Put("Wheel button: " + ptrPt.PointerId);
                    }
                    if (ptrPt.Properties.IsRightButtonPressed) {
                        //tracer.Put("Right button: " + ptrPt.PointerId);
                        tracer?.Put("Right button: " + ptrPt.PointerId);
                    }
                } else {
                    if (_manipulationStarted) {
                        var currPoint = ev.GetCurrentPoint(null);
                        var currProp = currPoint.Properties;
                        if (/*currProp.IsPrimary && */currProp.IsLeftButtonPressed) {
                            Action<PointerPoint> log = t => {
                                var prop = t.Properties;
                                tracer?.Put($"point={{PointerId(frame)={t.PointerId}({t.FrameId}), "
                                    + $"IsInContact={t.IsInContact}, "
                                    + $"Position={t.Position.ToFmPointDouble()}, "
                                    //+ $"RawPosition={t.RawPosition.ToFmPointDouble()}, "
                                    + $"Properties={{ "
                                    + $"ContactRect={prop.ContactRect.ToFmRectDouble()}, "
                                    //+ $"ContactRectRaw={prop.ContactRectRaw.ToFmRectDouble()}, "
                                    //+ $"IsCanceled={prop.IsCanceled}, "
                                    //+ $"IsBarrelButtonPressed={prop.IsBarrelButtonPressed}, "
                                    //+ $"IsEraser={prop.IsEraser}, "
                                    //+ $"IsHorizontalMouseWheel={prop.IsHorizontalMouseWheel}, "
                                    //+ $"IsInRange={prop.IsInRange}, "
                                    //+ $"IsInverted={prop.IsInverted}, "
                                    //+ $"IsLeftButtonPressed={prop.IsLeftButtonPressed}, "
                                    //+ $"IsRightButtonPressed={prop.IsRightButtonPressed}, "
                                    //+ $"IsMiddleButtonPressed={prop.IsMiddleButtonPressed}, "
                                    + $"IsPrimary={prop.IsPrimary}, "
                                    //+ $"IsXButton1Pressed={prop.IsXButton1Pressed}, "
                                    //+ $"IsXButton2Pressed={prop.IsXButton2Pressed}, "
                                    //+ $"MouseWheelDelta={prop.MouseWheelDelta}, "
                                    //+ $"Orientation={prop.Orientation:0.00}, "
                                    //+ $"PointerUpdateKind={prop.PointerUpdateKind}, "
                                    //+ $"Pressure={prop.Pressure:0.00}, "
                                    //+ $"TouchConfidence={prop.TouchConfidence}, "
                                    //+ $"Twist={prop.Twist:0.00}, "
                                    //+ $"XTilt={prop.XTilt:0.00}, "
                                    //+ $"YTilt={prop.YTilt:0.00}, "
                                    //+ $"ZDistance={prop.ZDistance:0.00} "
                                    + $"}}, "
                                    + $"Timestamp={t.Timestamp}}}");
                            };
                            //log(currPoint);
                            //foreach (var p in ev.GetIntermediatePoints(null)) {
                            //    log(p);
                            //}
                            //Logger.Put("  OnPointerMoved: ----------");
                        }
                    }
                }
            }
        }
#endif

        protected void OnManipulationStarting(object sender, ManipulationStartingRoutedEventArgs ev) {
            //using (CreateTracer())
            {
                _manipulationStarted = false;
            }
        }

        protected void OnManipulationCompleted(object sender, ManipulationCompletedRoutedEventArgs ev) {
#if  DEBUG
            var pnt1 = ev.Position; // thisPage.TransformToVisual(Control).TransformPoint(ev.Position);
#else
            var pnt1 = new Windows.Foundation.Point();
#endif
            //var pnt2 = ContentRoot.TransformToVisual(Mosaic.Container).TransformPoint(ev.Position);
            //var content = Window.Current.Content;
            //using (CreateTracer(GetCallerName(), $"Pos=[{ev.Position} / {pnt1}]; " +
            //                                         $"Container=[" +
            //                                         $"{((ev.Container == null) ? "null" : ev.Container.GetType().ToString())}" +
            //                                         $"]; Cumulative.Translation=[{ev.Cumulative.Translation}]"))
            {
                //ev.Handled = true;
                OnClickLost();
            }
        }

        protected void OnManipulationStarted(object sender, ManipulationStartedRoutedEventArgs ev) {
            //using (CreateTracer())
            {
                _turnX = _turnY = false;
                _dtInertiaStarting = DateTime.MinValue;
                _manipulationStarted = true;
            }
        }

        protected void OnManipulationInertiaStarting(object sender, ManipulationInertiaStartingRoutedEventArgs ev) {
            _dtInertiaStarting = DateTime.Now;
        }

        protected void OnManipulationDelta(object sender, ManipulationDeltaRoutedEventArgs ev) {
            var isInertial = ev.IsInertial;
            var delta = ev.Delta;
            var deltaTrans = delta.Translation;
            var deltaScale = delta.Scale;
            Tracer tracer = null;
            using (tracer = CreateTracer(GetCallerName(), string.Format($"pos={ev.Position}; Inertia={isInertial}; deltaTranslation=[{deltaTrans}]; deltaScale={deltaScale}; deltaExpansion={delta.Expansion}, Rotation={delta.Rotation}")))
            {
                ev.Handled = true;
                if (Math.Abs(1 - deltaScale) > 0.009) {
                    #region scale / zoom
                    if (deltaScale > 0)
                        ZoomInc(deltaScale, ev.Position);
                    else
                        ZoomDec(2 + deltaScale, ev.Position);
                    #endregion
                } else {
                    #region drag
                    var needDrag = true;
                    var offset = Offset;
                    var size = Model.Size;
                    #region check possibility dragging
                    if (_clickInfo.CellDown != null) {
                        var startPoint = ev.Position.ToFmPointDouble();
                        //var inCellRegion = _tmpClickedCell.PointInRegion(startPoint.ToFmRect());
                        //this._contentRoot.Background = new SolidColorBrush(inCellRegion ? Colors.Aquamarine : Colors.DeepPink);
                        var rcOuter = _clickInfo.CellDown.GetRcOuter().MoveXY(offset);
                        var min = Cast.DpToPx(25);
                        rcOuter = rcOuter.MoveXY(-min, -min);
                        rcOuter.Width  += min * 2;
                        rcOuter.Height += min * 2;
                        needDrag = !rcOuter.Contains(startPoint);
                    }
                    #endregion

                    if (needDrag) {
                        #region Compound motion
                        if (_turnX)
                            deltaTrans.X *= -1;
                        if (_turnY)
                            deltaTrans.Y *= -1;

                        if (isInertial) {
                            //var coefFading = Math.Max(0.05, 1 - 0.32 * (DateTime.Now - _dtInertiaStarting).TotalSeconds);
                            var coefFading = Math.Max(0, 1 - 0.32 * (DateTime.Now - _dtInertiaStarting).TotalSeconds);

                            deltaTrans.X *= coefFading;
                            deltaTrans.Y *= coefFading;

                            int fullX = (int)(deltaTrans.X / size.Width);
                            int fullY = (int)(deltaTrans.Y / size.Height);
                            if (fullX != 0) {
                                deltaTrans.X -= fullX * size.Width;
                                if ((fullX & 1) == 1)
                                    _turnX = !_turnX;
                            }
                            if (fullY != 0) {
                                deltaTrans.Y -= fullY * size.Height;
                                if ((fullY & 1) == 1)
                                    _turnY = !_turnY;
                            }

                            tracer?.Put("inertial coeff fading = " + coefFading + "; deltaTrans=" + deltaTrans);
                        }

                        var mosaicSize = Model.MosaicSize;
                        if ((offset.Width + mosaicSize.Width + deltaTrans.X) < MinIndent) { // правый край мозаики пересёк левую сторону контрола?
                            if (isInertial) {
                                _turnX = !_turnX; // разворачиваю по оси X

                                var dx1 = mosaicSize.Width + offset.Width - MinIndent; // часть deltaTrans.X которая не вылезла за границу
                                var dx2 = deltaTrans.X + dx1;                          // часть deltaTrans.X которая вылезла за границу
                                deltaTrans.X -= 2 * dx2;                               // та часть deltaTrans.X которая залезла за границу, разворачиваю обратно
                            } else {
                                offset.Width = MinIndent - mosaicSize.Width - deltaTrans.X; // привязываю к левой стороне контрола
                            }
                        } else
                        if ((offset.Width + deltaTrans.X) > (size.Width - MinIndent)) { // левый край мозаики пересёк правую сторону контрола?
                            if (isInertial) {
                                _turnX = !_turnX; // разворачиваю по оси X

                                var dx1 = size.Width - offset.Width - MinIndent; // часть deltaTrans.X которая не вылезла за границу
                                var dx2 = deltaTrans.X - dx1;                    // часть deltaTrans.X которая вылезла за границу
                                deltaTrans.X -= 2 * dx2;                         // та часть deltaTrans.X которая залезла за границу, разворачиваю обратно
                            } else {
                                offset.Width = size.Width - MinIndent - deltaTrans.X; // привязываю к правой стороне контрола
                            }
                        }

                        if ((offset.Height + mosaicSize.Height + deltaTrans.Y) < MinIndent) { // нижний край мозаики пересёк верхнюю сторону контрола?
                            if (isInertial) {
                                _turnY = !_turnY; // разворачиваю по оси Y

                                var dy1 = mosaicSize.Height + offset.Height - MinIndent; // часть deltaTrans.Y которая не вылезла за границу
                                var dy2 = deltaTrans.Y + dy1;                            // часть deltaTrans.Y которая вылезла за границу
                                deltaTrans.Y -= 2 * dy2;                                 // та часть deltaTrans.Y которая залезла за границу, разворачиваю обратно
                            } else {
                                offset.Height = MinIndent - mosaicSize.Height - deltaTrans.Y; // привязываю к верхней стороне контрола
                            }
                        } else
                        if ((offset.Height + deltaTrans.Y) > (size.Height - MinIndent)) { // вержний край мозаики пересёк нижнюю сторону контрола?
                            if (isInertial) {
                                _turnY = !_turnY; // разворачиваю по оси Y

                                var dy1 = size.Height - offset.Height - MinIndent; // часть deltaTrans.Y которая не вылезла за границу
                                var dy2 = deltaTrans.Y - dy1;                      // часть deltaTrans.Y которая вылезла за границу
                                deltaTrans.Y -= 2 * dy2;                           // та часть deltaTrans.Y которая залезла за границу, разворачиваю обратно
                            } else {
                                offset.Height = size.Height - MinIndent - deltaTrans.Y; // привязываю к нижней стороне контрола
                            }
                        }
                        #endregion
                        offset.Width  += deltaTrans.X;
                        offset.Height += deltaTrans.Y;
                        System.Diagnostics.Debug.Assert(offset == RecheckOffset(offset));
                        Offset = offset;
                    }
                    #endregion
                }
            }
        }

        /// <summary> Перепроверить смещение к полю мозаики так, что поле мозаики было в пределах страницы </summary>
        private SizeDouble RecheckOffset(SizeDouble offset) {
            var size = Model.Size;
            var mosaicSize = Model.MosaicSize;
            if ((offset.Width + mosaicSize.Width) < MinIndent) { // правый край мозаики пересёк левую сторону контрола?
                offset.Width = MinIndent - mosaicSize.Width; // привязываю к левой стороне контрола
            } else {
                if (offset.Width > (size.Width - MinIndent)) // левый край мозаики пересёк правую сторону контрола?
                    offset.Width = size.Width - MinIndent; // привязываю к правой стороне контрола
            }

            if ((offset.Height + mosaicSize.Height) < MinIndent) { // нижний край мозаики пересёк верхнюю сторону контрола?
                offset.Height = MinIndent - mosaicSize.Height; // привязываю к верхней стороне контрола
            } else {
                if (offset.Height > (size.Height - MinIndent)) // вержний край мозаики пересёк нижнюю сторону контрола?
                    offset.Height = size.Height - MinIndent; // привязываю к нижней стороне контрола
            }

            return offset;
        }


        private void OnKeyUp(object sender, KeyRoutedEventArgs ev) {
            //using (CreateTracer(GetCallerName(), "virtKey=" + ev.Key)) {
            ev.Handled = true;

            void setSkillLevel(ESkillLevel skill) {
                Model.SizeField = skill.GetDefaultSize();
                MinesCount = skill.GetNumberMines(Model.MosaicType);
            }

            switch (ev.Key) {
            case VirtualKey.F2:
                GameNew();
                break;
            case VirtualKey.Number1:
                setSkillLevel(ESkillLevel.eBeginner);
                break;
            case VirtualKey.Number2:
                setSkillLevel(ESkillLevel.eAmateur);
                break;
            case VirtualKey.Number3:
                setSkillLevel(ESkillLevel.eProfi);
                break;
            case VirtualKey.Number4:
                setSkillLevel(ESkillLevel.eCrazy);
                break;
            case (VirtualKey)187: // Plus (without Shift)
            case VirtualKey.Add: // numpad Plus
                ZoomInc();
                break;
            case (VirtualKey)189: // Minus (without Shift)
            case VirtualKey.Subtract: // numpad Minus
                ZoomDec();
                break;
            default:
                ev.Handled = false;
                break;
            }
            //}
        }

        protected override bool CheckNeedRestoreLastGame() {
            return false;
        }

        protected override void Disposing() {
            UnsubscribeToControl();

            base.Disposing();

            _areaScaleObservable?.Dispose();
            _scaleTransform = null;
        }


        class ClickInfo {
            public BaseCell CellDown { get; set; }
            public bool IsLeft { get; set; }
            /// <summary> pressed or released </summary>
            public bool Released { get; set; }
            public bool DownHandled { get; set; }
            public bool UpHandled { get; set; }
            //public PointerDeviceType PointerDevice { get; set; }
            public override string ToString() {
                return "{ IsLeft=" + IsLeft
                    + ", Released=" + Released
                    + ", CellDown=" + (CellDown == null ? "null" : CellDown.ToString())
                    + " }";
            }

        }

        private Tracer CreateTracer([System.Runtime.CompilerServices.CallerMemberName] string callerName = null, string ctorMessage = null, Func<string> disposeMessage = null) {
            var typeName = GetType().Name;
            var thisName = nameof(MosaicFrameworkElementController<TImageAsFrameworkElement, TImageInner, TMosaicView>);
            if (typeName != thisName)
                typeName += "(" + thisName + ")";
            return new Tracer(typeName + "." + callerName, ctorMessage, disposeMessage);
        }

    }

}
