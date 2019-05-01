using System;
using System.ComponentModel;
using Windows.System;
using System.Reactive.Linq;
using Windows.Devices.Input;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.common.geom;
using fmg.common.notifier;
using fmg.common.Converters;
using fmg.core.mosaic;
using fmg.core.types;
using fmg.core.types.click;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic {

    /// <summary> MVC: controller. UWP implementation over control <see cref="FrameworkElement"/> </summary>
    /// <typeparam name="TImageAsFrameworkElement">image-control based of <see cref="FrameworkElement"/></typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicView">mosaic view</typeparam>
    public abstract class MosaicFrameworkElementController<TImageAsFrameworkElement, TImageInner, TMosaicView>
                                        : MosaicController<TImageAsFrameworkElement, TImageInner, TMosaicView, MosaicDrawModel<TImageInner>>
        where TImageAsFrameworkElement : FrameworkElement
        where TImageInner : class
        where TMosaicView : IMosaicView<TImageAsFrameworkElement, TImageInner, MosaicDrawModel<TImageInner>>
    {

        private readonly ClickInfo _clickInfo = new ClickInfo();
        private bool _extendedManipulation = !true;
        #region if _extendedManipulation
        /// <summary> мин отступ от краев экрана для мозаики </summary>
        private const double MinIndent = 30;
        private const bool DeferredZoom = !true;
        private bool _manipulationStarted;
        private bool _turnX;
        private bool _turnY;
        private DateTime _dtInertiaStarting;
        private Windows.Foundation.Point? _mouseDevicePosition_AreaChanging = null;
        private static double? _baseWheelDelta;
        private IDisposable _areaScaleObservable;
        private Transform _originalTransform;
        private CompositeTransform _scaleTransform;
        private double _deferredArea;

        private sealed class NeedAreaChangingEventArgs { }
        private delegate void NeedAreaChangingEventHandler(object sender, NeedAreaChangingEventArgs ev);
        private event NeedAreaChangingEventHandler NeedAreaChanging;

        #endregion

        protected MosaicFrameworkElementController(TMosaicView view)
            : base(view)
        {
            SubscribeToControl();
            SetBinding();
        }

        public abstract TImageAsFrameworkElement Control { get; }

        protected virtual void SetBinding() {
            var control = Control;
            //if (_extendedManipulation)
            //    return;
            Func<SizeDouble> getSize = () => Size;
            control.SetBinding(FrameworkElement.WidthProperty, new Binding {
                Source = this,//.View,
                Path = new PropertyPath(nameof(Size)),
                Mode = BindingMode.TwoWay,
                Converter = new SizeToWidthConverter(),
                ConverterParameter = getSize
            });
            control.SetBinding(FrameworkElement.HeightProperty, new Binding {
                Source = this,//.View,
                Path = new PropertyPath(nameof(Size)),
                Mode = BindingMode.TwoWay,
                Converter = new SizeToHeightConverter(),
                ConverterParameter = getSize
            });
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

            if (!_extendedManipulation)
                return;

            ctrl.SizeChanged += OnControlSizeChanged;
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

            if (!_extendedManipulation)
                return;

            ctrl.SizeChanged -= OnControlSizeChanged;
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

        /// <summary> Поменять игру на новый уровень сложности </summary>
        public void SetGame(ESkillLevel skill) {
            //if (isPaused())
            //    ChangePause(e);

            int numberMines;
            Matrisize sizeFld;
            if (skill == ESkillLevel.eCustom) {
                // TODO ... dialog box 'Select custom skill level...'
                return;
            } else {
                numberMines = skill.GetNumberMines(Model.MosaicType);
                sizeFld = skill.GetDefaultSize();
            }

            Model.SizeField = sizeFld;
            MinesCount = numberMines;

            RecheckLocation();
        }

        /// <summary> get this Control size </summary>
        SizeDouble GetControlSize() {
            return Control.DesiredSize.ToFmSizeDouble();
        }

        /// <summary> узнаю мах размер площади ячеек мозаики (для размера поля 3x3) так, чтобы поле влазило в текущий размер Control'а </summary>
        /// <returns>макс площадь ячейки</returns>
        private double CalcMaxArea() {
            if (_maxArea.HasValue)
                return _maxArea.Value;
            var mosaicSizeField = new Matrisize(3, 3);
            var sizeControl = GetControlSize();
            double area = MosaicHelper.FindAreaBySize(this.MosaicType, mosaicSizeField, ref sizeControl);
            //System.Diagnostics.Debug.WriteLine("MosaicFrameworkElementController.CalcMaxArea: area="+area);
            _maxArea = area; // caching value
            return area;
        }
        double? _maxArea; // cached value

        /// <summary> check that mosaic field is placed in the window/page </summary>
        private void RecheckLocation() {
            SizeOptimal();

            var o = GetOffset();
            var sizeWinMosaic = Size;
            var sizeControl = GetControlSize();
            o.Left = (sizeControl.Width - sizeWinMosaic.Width) / 2;
            o.Top = (sizeControl.Height - sizeWinMosaic.Height) / 2;
            ApplyOffset(o);
        }

        /// <summary> Zoom + </summary>
        void ZoomInc(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
            _mouseDevicePosition_AreaChanging = mouseDevicePosition;
            if (DeferredZoom) {
                Scale(1.01 * zoomPower);
            } else {
                Model.Area *= 1.01 * zoomPower;
            }
        }

        /// <summary> Zoom - </summary>
        void ZoomDec(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
            _mouseDevicePosition_AreaChanging = mouseDevicePosition;
            if (DeferredZoom) {
                Scale(0.99 / zoomPower);
            } else {
                Model.Area *= 0.99 / zoomPower;
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

            var deferredViewSize = GetMosaicSize(Model.SizeField, _deferredArea);
            var currentViewSize = Size;

            if (_mouseDevicePosition_AreaChanging.HasValue) {
                var p = ToImagePoint(_mouseDevicePosition_AreaChanging.Value);
                _scaleTransform.CenterX = p.X;
                _scaleTransform.CenterY = p.Y;
            }

            _scaleTransform.ScaleX = deferredViewSize.Width / currentViewSize.Width;
            _scaleTransform.ScaleY = deferredViewSize.Height / currentViewSize.Height;

            NeedAreaChanging(this, null); // fire event
        }

        private void OnDeferredAreaChanging(object sender, NeedAreaChangingEventArgs ev) {
            Model.Area = _deferredArea;

            // restore
            _scaleTransform.CenterX = _scaleTransform.CenterY = 0;
            _scaleTransform.ScaleX = _scaleTransform.ScaleY = 1;
            Control.RenderTransform = _originalTransform;
        }

        /// <summary> Zoom minimum </summary>
        void ZoomMin() {
            Model.Area = MosaicInitData.AREA_MINIMUM;
        }

        /// <summary> Zoom maximum </summary>
        void ZoomMax() {
            var maxArea = CalcMaxArea();
            Model.Area = maxArea;
        }

        public void SizeOptimal() {
            var sizeControl = GetControlSize();
            var model = Model;
            Model.Area = MosaicHelper.FindAreaBySize(model.MosaicType, model.SizeField, ref sizeControl);
        }

        private void OnControlSizeChanged(object sender, SizeChangedEventArgs e) {
            RecheckLocation();
        }


        protected override void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnModelPropertyChanged(sender, ev);
            if (!_extendedManipulation)
                return;

            switch (ev.PropertyName) {
            case nameof(Model.Area):
                OnChangedArea(ev as PropertyChangedExEventArgs<double>);
                break;
            }
        }

        private void OnChangedArea(PropertyChangedExEventArgs<double> ev) {
            using (var tracer = CreateTracer(GetFullCallerName(), string.Format("newArea={0:0.00}, oldValue={1:0.00}", ev.NewValue, ev.OldValue))) {
                var o = GetOffset();

                var newViewSize = Size;
                if (_mouseDevicePosition_AreaChanging.HasValue) {
                    var devicePos = _mouseDevicePosition_AreaChanging.Value;
                    var oldViewSize = GetMosaicSize(Model.SizeField, ev.OldValue);

                    // точка над игровым полем со старой площадью ячеек
                    var pointOld = ToImagePoint(devicePos);
                    var percentX = pointOld.X / oldViewSize.Width;  // 0.0 .. 1.0
                    var percentY = pointOld.Y / oldViewSize.Height; // 0.0 .. 1.0

                    // таже точка над игровым полем, но с учётом zoom'а (новой площади)
                    var pointNew = new PointDouble(newViewSize.Width * percentX, newViewSize.Height * percentY);

                    // смещаю игровое поле так, чтобы точка была на том же месте экрана
                    o.Left += pointOld.X - pointNew.X;
                    o.Top += pointOld.Y - pointNew.Y;
                }

                RecheckOffset(ref o, newViewSize);
                ApplyOffset(o);
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
            var point = ToImagePoint(pos);
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
            //using (new fmg.common.Tracer(GetCallerName(), () => "handled=" + ev.Handled))
            {
                if (ev.PointerDeviceType != PointerDeviceType.Mouse) {
                    ev.Handled = OnClick(ev.GetPosition(Control), true, false);
                }
            }
        }

        protected void OnDoubleTapped(object sender, DoubleTappedRoutedEventArgs ev) {
            //using (new fmg.common.Tracer(GetCallerName(), () => "handled=" + ev.Handled))
            {
                var imgControl = Control;
                var rcImage = new Windows.Foundation.Rect(0, 0, imgControl.Width, imgControl.Height);
                if (rcImage.Contains(ev.GetPosition(imgControl))) {
                    if (this.GameStatus == EGameStatus.eGSEnd) {
                        this.GameNew();
                        ev.Handled = true;
                    }
                } else {
                    RecheckLocation();
                    ev.Handled = true;
                }
            }
        }

        protected void OnRightTapped(object sender, RightTappedRoutedEventArgs ev) {
            //using (new fmg.common.Tracer(GetCallerName(), () => "handled=" + ev.Handled))
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
            //using (CreateTracer(GetFullCallerName(), "pointerId=" + currPoint.PointerId, () => "ev.Handled = " + ev.Handled))
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
            //using (CreateTracer(GetFullCallerName(), string.Format($"pointerId={currPoint.PointerId}, _manipulationStarted={_manipulationStarted}"), () => "ev.Handled=" + ev.Handled))
            {
                //if (_manipulationStarted)
                if (ev.Pointer.PointerDeviceType == PointerDeviceType.Mouse) {
                    var isLeftClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.LeftButtonReleased);
                    var isRightClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.RightButtonReleased);
                    System.Diagnostics.Debug.Assert(isLeftClick != isRightClick);
                    ev.Handled = OnClick(currPoint.Position, isLeftClick, false);
                } else {
                    AsyncRunner.InvokeFromUiLater(() => {
                        if (!_clickInfo.Released) {
                            LoggerSimple.Put("ã OnPointerReleased: forced left release click...");
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
            //using (CreateTracer(GetFullCallerName(), string.Format($"pointerId={currPoint.PointerId}, _manipulationStarted={_manipulationStarted}"), () => "ev.Handled=" + ev.Handled))
            {
                if (!_clickInfo.Released) {
                    LoggerSimple.Put("ã OnPointerCaptureLost: forced left release click...");
                    OnClick(currPoint.Position, true, false);
                }
            }
        }

#if DEBUG
        protected void OnPointerMoved(object sender, PointerRoutedEventArgs ev) {
            //using (var tracer = CreateTracer(GetFullCallerName(), () => "ev.Handled = " + ev.Handled))
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
                        LoggerSimple.Put("  OnPointerMoved: Left button: " + ptrPt.PointerId);
                    }
                    if (ptrPt.Properties.IsMiddleButtonPressed) {
                        //tracer.Put("Wheel button: " + ptrPt.PointerId);
                        LoggerSimple.Put("  OnPointerMoved: Wheel button: " + ptrPt.PointerId);
                    }
                    if (ptrPt.Properties.IsRightButtonPressed) {
                        //tracer.Put("Right button: " + ptrPt.PointerId);
                        LoggerSimple.Put("  OnPointerMoved: Right button: " + ptrPt.PointerId);
                    }
                } else {
                    if (_manipulationStarted) {
                        var currPoint = ev.GetCurrentPoint(null);
                        var currProp = currPoint.Properties;
                        if (/*currProp.IsPrimary && */currProp.IsLeftButtonPressed) {
                            Action<PointerPoint> log = t => {
                                var prop = t.Properties;
                                LoggerSimple.Put($"  OnPointerMoved: point={{PointerId(frame)={t.PointerId}({t.FrameId}), "
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
            using (CreateTracer()) {
                _manipulationStarted = false;
            }
        }

        protected void OnManipulationStarted(object sender, ManipulationStartedRoutedEventArgs ev) {
            using (CreateTracer()) {
                _turnX = _turnY = false;
                _dtInertiaStarting = DateTime.MinValue;
                _manipulationStarted = true;
            }
        }

        protected void OnManipulationInertiaStarting(object sender, ManipulationInertiaStartingRoutedEventArgs ev) {
            _dtInertiaStarting = DateTime.Now;
        }

        protected void OnManipulationDelta(object sender, ManipulationDeltaRoutedEventArgs ev) {
            var delta = ev.Delta;
            using (var tracer = CreateTracer(GetFullCallerName(), string.Format($"pos={ev.Position}; Scale={delta.Scale}; Expansion={delta.Expansion}, Rotation={delta.Rotation}"))) {
                ev.Handled = true;
                if (Math.Abs(1 - delta.Scale) > 0.009) {
                    #region scale / zoom
                    if (delta.Scale > 0)
                        ZoomInc(delta.Scale, ev.Position);
                    else
                        ZoomDec(2 + delta.Scale, ev.Position);
                    #endregion
                } else {
                    #region drag
                    var needDrag = true;
                    var o = GetOffset();
                    var sizeControl = GetControlSize();
                    #region check possibility dragging
                    if (_clickInfo.CellDown != null) {
                        var noMarginPoint = ToImagePoint(ev.Position); // new Windows.Foundation.Point(ev.Position.X - o.Left, ev.Position.Y - o.Top);
                        //var inCellRegion = _tmpClickedCell.PointInRegion(noMarginPoint.ToFmRect());
                        //this._contentRoot.Background = new SolidColorBrush(inCellRegion ? Colors.Aquamarine : Colors.DeepPink);
                        var rcOuter = _clickInfo.CellDown.GetRcOuter();
                        var min = Math.Min(sizeControl.Width / 20, sizeControl.Height / 20);
                        rcOuter.MoveXY(-min, -min);
                        rcOuter.Width += min * 2;
                        rcOuter.Height += min * 2;
                        needDrag = !rcOuter.Contains(noMarginPoint);
                    }
                    #endregion

                    if (needDrag) {
                        var deltaTrans = delta.Translation;
                        var applyDelta = true;
                        #region Compound motion
                        if (_turnX)
                            deltaTrans.X *= -1;
                        if (_turnY)
                            deltaTrans.Y *= -1;

                        if (ev.IsInertial) {
                            //var coefFading = Math.Max(0.05, 1 - 0.32 * (DateTime.Now - _dtInertiaStarting).TotalSeconds);
                            var coefFading = Math.Max(0, 1 - 0.32 * (DateTime.Now - _dtInertiaStarting).TotalSeconds);
                            tracer.Put("inertial coeff fading = " + coefFading);
                            deltaTrans.X *= coefFading;
                            deltaTrans.Y *= coefFading;
                        }

                        var sizeViewMosaic = Size;
                        if ((o.Left + sizeViewMosaic.Width + deltaTrans.X) < MinIndent) {
                            // правый край мозаики пересёк левую сторону страницы/экрана
                            if (ev.IsInertial)
                                _turnX = !_turnX; // разворачиавю по оси X
                            else
                                o.Left = MinIndent - sizeViewMosaic.Width; // привязываю к левой стороне страницы/экрана
                            applyDelta = ev.IsInertial;
                        } else
                        if ((o.Left + deltaTrans.X) > (sizeControl.Width - MinIndent)) {
                            // левый край мозаики пересёк правую сторону страницы/экрана
                            if (ev.IsInertial)
                                _turnX = !_turnX; // разворачиавю по оси X
                            else
                                o.Left = sizeControl.Width - MinIndent; // привязываю к правой стороне страницы/экрана
                            applyDelta = ev.IsInertial;
                        }
                        if ((o.Top + sizeViewMosaic.Height + deltaTrans.Y) < MinIndent) {
                            // нижний край мозаики пересёк верхнюю сторону страницы/экрана
                            if (ev.IsInertial)
                                _turnY = !_turnY; // разворачиавю по оси Y
                            else
                                o.Top = MinIndent - sizeViewMosaic.Height; // привязываю к верхней стороне страницы/экрана
                            applyDelta = ev.IsInertial;
                        } else
                        if ((o.Top + deltaTrans.Y) > (sizeControl.Height - MinIndent)) {
                            // вержний край мозаики пересёк нижнюю сторону страницы/экрана
                            if (ev.IsInertial)
                                _turnY = !_turnY; // разворачиавю по оси Y
                            else
                                o.Top = sizeControl.Height - MinIndent; // привязываю к нижней стороне страницы/экрана
                            applyDelta = ev.IsInertial;
                        }
                        #endregion
                        if (applyDelta) {
                            o.Left += deltaTrans.X;
                            o.Top += deltaTrans.Y;
                        }
                        RecheckOffset(ref o, sizeViewMosaic);
                        ApplyOffset(o);
                    }
                    #endregion
                }
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
            using (CreateTracer(GetFullCallerName(), $"Pos=[{ev.Position} / {pnt1}]; " +
                                                     $"Container=[" +
                                                     $"{((ev.Container == null) ? "null" : ev.Container.GetType().ToString())}" +
                                                     $"]; Cumulative.Translation=[{ev.Cumulative.Translation}]")) {
                //ev.Handled = true;
                OnClickLost();
            }
        }


        private void OnKeyUp(object sender, KeyRoutedEventArgs ev) {
            //using (CreateTracer(GetFullCallerName(), "virtKey=" + ev.Key)) {
            ev.Handled = true;
            switch (ev.Key) {
            case VirtualKey.F2:
                GameNew();
                break;
            case VirtualKey.Number1:
                SetGame(ESkillLevel.eBeginner);
                break;
            case VirtualKey.Number2:
                SetGame(ESkillLevel.eAmateur);
                break;
            case VirtualKey.Number3:
                SetGame(ESkillLevel.eProfi);
                break;
            case VirtualKey.Number4:
                SetGame(ESkillLevel.eCrazy);
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

        Thickness GetOffset() {
            //return Control.Padding;   // variant 1
            return Control.Margin;      // variant 2
            //return _contentRoot.Margin;             // variant 3
            //return _contentRoot.Padding;           // variant 4
        }

        private void ApplyOffset(Thickness offset) {
            //var pad = Control.Padding; // variant 1
            var pad = Control.Margin;    // variant 2
            //var pad = _contentRoot.Margin;           // variant 3
            //var pad = _contentRoot.Padding;          // variant 4
            pad.Left = offset.Left;
            pad.Top = offset.Top;
            //Control.Padding = pad; // variant 1
            Control.Margin = pad;    // variant 2
            //_contentRoot.Margin = pad;           // variant 3
            //_contentRoot.Padding = pad;          // variant 4
        }

        /// <summary> Перепроверить смещение к полю мозаики так, что поле мозаики было в пределах страницы </summary>
        private void RecheckOffset(ref Thickness offset, SizeDouble sizeWinMosaic) {
            var sizeControl = GetControlSize();

            if (offset.Left < (MinIndent - sizeWinMosaic.Width)) { // правый край мозаики пересёк левую сторону страницы/экрана?
                offset.Left = MinIndent - sizeWinMosaic.Width; // привязываю к левой стороне страницы/экрана
            } else {
                if (offset.Left > (sizeControl.Width - MinIndent)) // левый край мозаики пересёк правую сторону страницы/экрана?
                    offset.Left = sizeControl.Width - MinIndent; // привязываю к правой стороне страницы/экрана
            }

            if (offset.Top < (MinIndent - sizeWinMosaic.Height)) { // нижний край мозаики пересёк верхнюю сторону страницы/экрана?
                offset.Top = MinIndent - sizeWinMosaic.Height; // привязываю к верхней стороне страницы/экрана
            } else {
                if (offset.Top > (sizeControl.Height - MinIndent)) // вержний край мозаики пересёк нижнюю сторону страницы/экрана?
                    offset.Top = sizeControl.Height - MinIndent; // привязываю к нижней стороне страницы/экрана
            }
        }

        // TODO rename or remove
        private PointDouble ToImagePoint(Windows.Foundation.Point pagePoint) {
            //var imgControl = Control;
            var point = pagePoint.ToFmPointDouble(); // imgControl.TransformToVisual(imgControl).TransformPoint(pagePoint).ToFmPointDouble();
            //var o = GetOffset();
            //var point2 = new PointDouble(pagePoint.X - o.Left, pagePoint.Y - o.Top);
            //System.Diagnostics.Debug.Assert(point == point2);
            return point;
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
        }

        private string GetFullCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) {
            var typeName = GetType().Name;
            var thisName = nameof(MosaicFrameworkElementController<TImageAsFrameworkElement, TImageInner, TMosaicView>);
            if (typeName != thisName)
                typeName += "(" + thisName + ")";
            return typeName + "." + callerName;
        }
        private Tracer CreateTracer([System.Runtime.CompilerServices.CallerMemberName] string callerName = null, string ctorMessage = null, Func<string> disposeMessage = null) {
            return new Tracer(GetFullCallerName(callerName), ctorMessage, disposeMessage);
        }

    }

}
