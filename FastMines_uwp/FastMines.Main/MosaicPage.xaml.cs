using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.System;
using Windows.Devices.Input;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using Windows.UI.ViewManagement;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;
using fmg.core.types.click;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.data.controller.types;
using fmg.uwp.utils;
using fmg.uwp.mosaic;
using fmg.uwp.mosaic.win2d;
using fmg.uwp.draw.mosaic.win2d;
using Logger = fmg.common.LoggerSimple;

namespace fmg {

   public sealed partial class MosaicPage : Page {
      /// <summary> мин отступ от краев экрана для мозаики </summary>
      private const double MinIndent = 30;

      private Mosaic _mosaic;
      private readonly ClickInfo _clickInfo = new ClickInfo();
      private bool _manipulationStarted;
      private bool _turnX;
      private bool _turnY;
      private DateTime _dtInertiaStarting;
      private Windows.Foundation.Point? _mouseDevicePosition_AreaChanging = null;
      private static double? _baseWheelDelta;

      public Mosaic MosaicField {
         get {
            if (_mosaic == null)
               MosaicField = new Mosaic(); // call this setter
            return _mosaic;
         }
         private set {
            if (_mosaic != null) {
               _mosaic.PropertyChanged -= OnMosaicPropertyChanged;
               _mosaic.Dispose();
            }
            _mosaic = value;
            if (_mosaic != null) {
               _mosaic.PropertyChanged += OnMosaicPropertyChanged;
            }
         }
      }

      public MosaicPage() {
         this.InitializeComponent();

         this.Loaded += OnPageLoaded;
         this.Unloaded += OnPageUnloaded;
         this.SizeChanged += OnPageSizeChanged;
         this.ManipulationMode =
            ManipulationModes.TranslateX |
            ManipulationModes.TranslateY |
               ManipulationModes.Rotate |
            ManipulationModes.Scale |
            ManipulationModes.TranslateInertia;

         if (Windows.ApplicationModel.DesignMode.DesignModeEnabled) {
            AsyncRunner.InvokeFromUiLater(() => {
               MosaicField.SizeField = new Matrisize(10, 10);
               MosaicField.MosaicType = EMosaic.eMosaicRhombus1;
               MosaicField.MinesCount = 3;
               MosaicField.Area = 1500;
               //MosaicField.Repaint();
            }, CoreDispatcherPriority.High);
         }
      }

      protected override void OnNavigatedTo(NavigationEventArgs ev) {
         base.OnNavigatedTo(ev);

         System.Diagnostics.Debug.Assert(ev.Parameter is MosaicPageInitParam);
         var initParam = ev.Parameter as MosaicPageInitParam;
         MosaicField.SizeField  = initParam.SizeField;
         MosaicField.MosaicType = initParam.MosaicTypes;
         MosaicField.MinesCount = initParam.MinesCount;
      }

      /// <summary> Поменять игру на новый уровень сложности </summary>
      void SetGame(ESkillLevel skill) {
         //if (isPaused())
         //   ChangePause(e);

         int numberMines;
         Matrisize sizeFld;
         if (skill == ESkillLevel.eCustom) {
            //System.out.println("... dialog box 'Select custom skill level...' ");
            //getCustomSkillDiaLog.Put().setVisible(!getCustomSkillDiaLog.Put().isVisible());
            return;
         } else {
            numberMines = skill.GetNumberMines(MosaicField.MosaicType);
            sizeFld = skill.DefaultSize();
         }

         MosaicField.SizeField = sizeFld;
       //MosaicField.MosaicType = MosaicField.MosaicType;
         MosaicField.MinesCount = numberMines;

         RecheckLocation();
      }

      /// <summary> get this Page size </summary>
      SizeDouble GetPageSize() {
         var bounds = ApplicationView.GetForCurrentView().VisibleBounds;
         //var scale = Windows.Graphics.Display.DisplayInformation.GetForCurrentView().RawPixelsPerViewPixel;
         //return new SizeDouble(bounds.Width * scale, bounds.Height * scale);
         return new SizeDouble(bounds.Width, bounds.Height);
      }

      /// <summary> get margin around mosaic control </summary>
      Bound GetMosaicMargin() {
         // @TODO: not implemented...
         return new Bound();
      }

      ///// <summary> узнать размер окна проекта при указанном размере окна мозаики </summary>
      //Size CalcMainSize(Size sizeMosaicInPixel) {
      //   var mosaicMargin = GetMosaicMargin();
      //   return new Size(
      //         mosaicMargin.LeftAndRight + sizeMosaicInPixel.Width,
      //         mosaicMargin.TopAndBottom + sizeMosaicInPixel.Height);
      //}

      /// <summary> узнать размер окна мозаики при указанном размере окна проекта </summary>
      SizeDouble CalcMosaicWindowSize(Size sizeMainWindow) {
         var mosaicMargin = GetMosaicMargin();
         SizeDouble res = new SizeDouble(
               sizeMainWindow.Width  - mosaicMargin.LeftAndRight,
               sizeMainWindow.Height - mosaicMargin.TopAndBottom);
         if (res.Height < 0 || res.Width < 0)
            throw new Exception("Bad algorithm... :(");
         return res;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в текущее разрешение экрана </summary>
      /// <param name="mosaicSizeField">интересуемый размер поля мозаики</param>
      /// <returns>макс площадь ячейки</returns>
      private double CalcMaxArea(Matrisize mosaicSizeField) {
         // TODO на самом деле узнаю размер площади ячеек мозаики, для размера поля мозаики 3x3
         if (_maxArea.HasValue)
            return _maxArea.Value;
         mosaicSizeField = new Matrisize(3, 3);
         var sizeMosaic = CalcMosaicWindowSize(ScreenResolutionHelper.GetDesktopSize());
         double area = MosaicHelper.FindAreaBySize(MosaicField.MosaicType, mosaicSizeField, ref sizeMosaic);
         //System.Diagnostics.Debug.WriteLine("Main.CalcMaxArea: area="+area);
         _maxArea = area; // caching value
         return area;
      }
      double? _maxArea; // cached value

      /// <summary> узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <returns>max размер поля мозаики</returns>
      public Matrisize CalcMaxMosaicSize(double area) {
         var sizeMosaic = CalcMosaicWindowSize(ScreenResolutionHelper.GetDesktopSize());
         return MosaicHelper.FindSizeByArea(MosaicField.CellAttr, sizeMosaic);
      }

      /// <summary> check that mosaic field is placed in the window/page </summary>
      private void RecheckLocation() {
         AreaOptimal();

         var o = GetOffset();
         var sizeWinMosaic = MosaicField.WindowSize;
         var sizePage = GetPageSize();
         o.Left = (sizePage.Width - sizeWinMosaic.Width) / 2;
         o.Top = (sizePage.Height - sizeWinMosaic.Height) / 2;
         ApplyOffset(o);
      }

      double Area {
         get {
            return MosaicField.Area;
         }
         set {
            value = Math.Min(value, CalcMaxArea(MosaicField.SizeField)); // recheck
            MosaicField.Area = value;
         }
      }

      /// <summary> Zoom + </summary>
      void AreaInc(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
         _mouseDevicePosition_AreaChanging = mouseDevicePosition;
         Area *= 1.01 * zoomPower;
      }

      /// <summary> Zoom - </summary>
      void AreaDec(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
         _mouseDevicePosition_AreaChanging = mouseDevicePosition;
         Area *= 0.99 / zoomPower;
      }

      /// <summary> Zoom minimum </summary>
      void AreaMin() {
         Area = 0;
      }

      /// <summary> Zoom maximum </summary>
      void AreaMax() {
         var maxArea = CalcMaxArea(MosaicField.SizeField);
         Area = maxArea;
      }

      void AreaOptimal() {
         var sizePage = GetPageSize();
         Area = MosaicHelper.FindAreaBySize(MosaicField.MosaicType, MosaicField.SizeField, ref sizePage);
      }

      private void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case nameof(Mosaic.MosaicType):
            Mosaic_OnChangedMosaicType(sender as Mosaic, ev as PropertyChangedExEventArgs<EMosaic>);
            break;
         case nameof(Mosaic.Area):
            Mosaic_OnChangedArea(sender as Mosaic, ev as PropertyChangedExEventArgs<double>);
            break;
         case nameof(Mosaic.GameStatus):
            Mosaic_OnChangedGameStatus(sender as Mosaic, ev as PropertyChangedExEventArgs<EGameStatus>);
            break;
         case nameof(Mosaic.SizeField):
            Mosaic_OnChangedSizeField(sender as Mosaic, ev as PropertyChangedExEventArgs<Matrisize>);
            break;
         case nameof(Mosaic.MinesCount):
            Mosaic_OnChangedMinesCount(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case nameof(Mosaic.CountFlag):
            Mosaic_OnChangedCountFlag(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case nameof(Mosaic.CountOpen):
            Mosaic_OnChangedCountOpen(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case nameof(Mosaic.CountMinesLeft):
            Mosaic_OnChangedCountMinesLeft(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case nameof(Mosaic.CountClick):
            Mosaic_OnChangedCountClick(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case Mosaic.PROPERTY_MODIFIED_CELLS:
            InvalidateCells((ev as IPropertyChangedExEventArgs<IEnumerable<BaseCell>>).NewValue);
            break;
         }

      }












      private void GoBack() {
         if (this.Frame != null && this.Frame.CanGoBack)
            this.Frame.GoBack();
      }

      private void OnPageLoaded(object sender, RoutedEventArgs e) {
         Window.Current.CoreWindow.KeyUp += OnKeyUp_CoreWindow;
      }

      private void OnPageUnloaded(object sender, RoutedEventArgs e) {
         Window.Current.CoreWindow.KeyUp -= OnKeyUp_CoreWindow;

         MosaicField = null; // call explicit setter

         // Explicitly remove references to allow the Win2D controls to get garbage collected
         _canvasVirtualControl.RemoveFromVisualTree();
         _canvasVirtualControl = null;
      }

      private void OnPageSizeChanged(object sender, RoutedEventArgs e) {
         RecheckLocation();
      }

      private void Mosaic_OnChangedGameStatus(Mosaic sender, PropertyChangedExEventArgs<EGameStatus> ev) { }
      private void Mosaic_OnChangedMinesCount(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountFlag(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountOpen(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountMinesLeft(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountClick(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedArea(Mosaic sender, PropertyChangedExEventArgs<double> ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, MosaicField));
         using (var tracer = new Tracer("Mosaic_OnChangedArea", string.Format("newArea={0:0.00}, oldValue={1:0.00}", ev.NewValue, ev.OldValue))) {
            //ChangeSizeImagesMineFlag();

            if (_mouseDevicePosition_AreaChanging.HasValue) {
               var devicePos = _mouseDevicePosition_AreaChanging.Value;

               var oldWinSize = MosaicField.GetWindowSize(MosaicField.SizeField, ev.OldValue);
               var newWinSize = MosaicField.WindowSize;

               // точка над игровым полем со старой площадью ячеек
               var pointOld = ToCanvasPoint(devicePos);
               var percent = new Tuple<double, double>(pointOld.X * 100 / oldWinSize.Width, pointOld.Y * 100 / oldWinSize.Height);

               // таже точка над игровым полем, но с учётом zoom'а (новой площади)
               var pointNew = new PointDouble(newWinSize.Width * percent.Item1 / 100, newWinSize.Height * percent.Item2 / 100);

               var o = GetOffset();
               // смещаю игровое поле так, чтобы точка была на том же месте экрана
               o.Left += pointOld.X - pointNew.X;
               o.Top  += pointOld.Y - pointNew.Y;

               RecheckOffset(ref o, newWinSize);
               ApplyOffset(o);
            }
         }
      }

      private void Mosaic_OnChangedMosaicType(Mosaic sender, PropertyChangedExEventArgs<EMosaic> ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, MosaicField));
         using (new Tracer()) {
            //ChangeSizeImagesMineFlag();
         }
      }

      private void Mosaic_OnChangedSizeField(Mosaic sender, PropertyChangedExEventArgs<Matrisize> ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, MosaicField));
         using (new Tracer()) {
         }
      }

//#if DEBUG
//      protected override void OnPointerMoved(PointerRoutedEventArgs ev) {
//         var ttv = this.TransformToVisual(_canvasVirtualControl);
//         using (new Tracer("MosaicPage.OnPointerMoved",
//                 "pos1=" + ev.GetCurrentPoint(null).Position.ToFmPointDouble() +
//               "; pos2=" + ev.GetCurrentPoint(_canvasVirtualControl).Position.ToFmPointDouble() +
//               "; pos3=" + ttv.TransformPoint(ev.GetCurrentPoint(null).Position).ToFmPointDouble(),
//               () => "handled="+ev.Handled))
//         {
//            base.OnPointerMoved(ev);
//         }
//      }
//#endif

      protected override void OnPointerWheelChanged(PointerRoutedEventArgs ev) {
         //using (new Tracer()) {
         var wheelDelta = ev.GetCurrentPoint(this).Properties.MouseWheelDelta;
         if (!_baseWheelDelta.HasValue)
            _baseWheelDelta = Math.Abs(wheelDelta);

         var wheelPower = 1 + ((Math.Abs(wheelDelta)/_baseWheelDelta.Value) - 1)/10;

         if (wheelDelta > 0)
            AreaInc(wheelPower, ev.GetCurrentPoint(null).Position);
         else
            AreaDec(wheelPower, ev.GetCurrentPoint(null).Position);

         ev.Handled = true;
         base.OnPointerWheelChanged(ev);
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

      bool OnClickLost() {
         return ClickHandler(MosaicField.MouseFocusLost());
      }

      bool OnClick(Windows.Foundation.Point pos, bool leftClick, bool down) {
         var point = ToCanvasPoint(pos);
#if false // otherwise not work the long tapping (to setting flag label)
         if (point.X < 0 || point.Y < 0) {
            return clickHandler(new ClickResult(null, leftClick, down));
         }

         var winSize = MosaicField.WindowSize;
         if (point.X > winSize.Width || point.Y > winSize.Height) {
            return clickHandler(new ClickResult(null, leftClick, down));
         }
#endif
         return ClickHandler(down
               ? MosaicField.MousePressed(point, leftClick)
               : MosaicField.MouseReleased(point, leftClick));
      }

      protected override void OnTapped(TappedRoutedEventArgs ev) {
         using (new Tracer(GetCallerName(), () => "ev.Handled = " + ev.Handled)) {
            //if (!_manipulationStarted) {
            if (ev.PointerDeviceType != PointerDeviceType.Mouse) {
               ev.Handled = OnClick(ev.GetPosition(this), true, false);
            }
            if (!ev.Handled)
               base.OnTapped(ev);
         }
      }

      protected override void OnDoubleTapped(DoubleTappedRoutedEventArgs ev) {
         using (new Tracer(GetCallerName(), () => "ev.Handled = " + ev.Handled)) {
            var rcCanvas = new Windows.Foundation.Rect(0, 0, _canvasVirtualControl.Width, _canvasVirtualControl.Height);
            if (rcCanvas.Contains(ev.GetPosition(_canvasVirtualControl))) {
               if (MosaicField.GameStatus == EGameStatus.eGSEnd) {
                  MosaicField.GameNew();
                  ev.Handled = true;
               }
            } else {
               RecheckLocation();
               ev.Handled = true;
            }

            if (!ev.Handled)
               base.OnDoubleTapped(ev);
         }
      }

      protected override void OnRightTapped(RightTappedRoutedEventArgs ev) {
         using (new Tracer(GetCallerName(), () => "ev.Handled = " + ev.Handled)) {
            if (ev.PointerDeviceType == PointerDeviceType.Mouse)
               ev.Handled = _clickInfo.DownHandled || _clickInfo.UpHandled; // TODO: для избежания появления appBar'ов при установке '?'
            else if (!_manipulationStarted) {

               // 1. release left click in invalid coord
               OnClick(new Windows.Foundation.Point(-1, -1), true, false);

               // 2. make right click - up & down
               var pos = ev.GetPosition(this);
               var handled1 = OnClick(pos, false, true);
               var handled2 = OnClick(pos, false, false);
               ev.Handled = handled1 || handled2;
            }

            if (!ev.Handled)
               base.OnRightTapped(ev);

         }
      }

      protected override void OnPointerPressed(PointerRoutedEventArgs ev) {
         var currPoint = ev.GetCurrentPoint(this);
         using (new Tracer(GetCallerName(), "pointerId=" + currPoint.PointerId, () => "ev.Handled = " + ev.Handled)) {

            //_clickInfo.PointerDevice = pointerPoint.PointerDevice.PointerDeviceType;
            var props = currPoint.Properties;
            // Ignore button chords with the left, right, and middle buttons
            if (!props.IsLeftButtonPressed && !props.IsRightButtonPressed && !props.IsMiddleButtonPressed) {
               // If back or foward are pressed (but not both) navigate appropriately
               var backPressed = props.IsXButton1Pressed;
               if (backPressed) {
                  ev.Handled = true;
                  GoBack();
               }
            }

            ev.Handled = OnClickLost(); // Protection from the two-finger click.
            //if (_manipulationStarted) {
            //   // touch two-finger
            //   OnClickLost(); // Protection from the two-finger click.
            //}

            if (!ev.Handled)
               ev.Handled = OnClick(currPoint.Position, props.IsLeftButtonPressed, true);

            _clickInfo.DownHandled = ev.Handled;
            if (!ev.Handled)
               base.OnPointerPressed(ev);
         }
      }

      protected override void OnPointerReleased(PointerRoutedEventArgs ev) {
         var currPoint = ev.GetCurrentPoint(this);
         using (new Tracer(GetCallerName(), string.Format($"pointerId={currPoint.PointerId}, _manipulationStarted={_manipulationStarted}"), () => "ev.Handled=" + ev.Handled)) {
            //if (_manipulationStarted)
            if (ev.Pointer.PointerDeviceType == PointerDeviceType.Mouse) {
               var isLeftClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.LeftButtonReleased);
               var isRightClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.RightButtonReleased);
               System.Diagnostics.Debug.Assert(isLeftClick != isRightClick);
               ev.Handled = OnClick(currPoint.Position, isLeftClick, false);
            } else {
               AsyncRunner.InvokeFromUiLater(() => {
                  if (!_clickInfo.Released) {
                     Logger.Put("ã OnPointerReleased: forced left release click...");
                     OnClick(currPoint.Position, true, false);
                  }
               }, CoreDispatcherPriority.High);
            }

            _clickInfo.UpHandled = ev.Handled;
            if (!ev.Handled)
               base.OnPointerReleased(ev);
         }
      }

      protected override void OnPointerCaptureLost(PointerRoutedEventArgs ev) {
         var currPoint = ev.GetCurrentPoint(this);
         using (new Tracer(GetCallerName(), string.Format($"pointerId={currPoint.PointerId}, _manipulationStarted={_manipulationStarted}"), () => "ev.Handled=" + ev.Handled)) {
            if (!_clickInfo.Released) {
               Logger.Put("ã OnPointerCaptureLost: forced left release click...");
               OnClick(currPoint.Position, true, false);
            }

            if (!ev.Handled)
               base.OnPointerCaptureLost(ev);
         }
      }

#if DEBUG
      protected override void OnPointerMoved(PointerRoutedEventArgs ev) {
         //using (var tracer = new Tracer(GetCallerName(), () => "ev.Handled = " + ev.Handled))
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
                  Logger.Put("  OnPointerMoved: Left button: " + ptrPt.PointerId);
               }
               if (ptrPt.Properties.IsMiddleButtonPressed) {
                  //tracer.Put("Wheel button: " + ptrPt.PointerId);
                  Logger.Put("  OnPointerMoved: Wheel button: " + ptrPt.PointerId);
               }
               if (ptrPt.Properties.IsRightButtonPressed) {
                  //tracer.Put("Right button: " + ptrPt.PointerId);
                  Logger.Put("  OnPointerMoved: Right button: " + ptrPt.PointerId);
               }
            } else {
               if (_manipulationStarted) {
                  var currPoint = ev.GetCurrentPoint(null);
                  var currProp = currPoint.Properties;
                  if (/*currProp.IsPrimary && */currProp.IsLeftButtonPressed) {
                     Action<PointerPoint> log = t => {
                        var prop = t.Properties;
                        Logger.Put($"  OnPointerMoved: point={{PointerId(frame)={t.PointerId}({t.FrameId}), "
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
                     //   log(p);
                     //}
                     //Logger.Put("  OnPointerMoved: ----------");
                  }
               }
            }

            if (!ev.Handled)
               base.OnPointerMoved(ev);
         }
      }
#endif

      protected override void OnManipulationStarting(ManipulationStartingRoutedEventArgs ev) {
         using (new Tracer()) {
            base.OnManipulationStarting(ev);
            _manipulationStarted = false;
         }
      }

      protected override void OnManipulationStarted(ManipulationStartedRoutedEventArgs ev) {
         using (new Tracer()) {
            _turnX = _turnY = false;
            _dtInertiaStarting = DateTime.MinValue;
            base.OnManipulationStarted(ev);
            _manipulationStarted = true;
         }
      }

      protected override void OnManipulationInertiaStarting(ManipulationInertiaStartingRoutedEventArgs ev) {
         _dtInertiaStarting = DateTime.Now;
         base.OnManipulationInertiaStarting(ev);
      }

      protected override void OnManipulationDelta(ManipulationDeltaRoutedEventArgs ev) {
         var delta = ev.Delta;
         using (var tracer = new Tracer(GetCallerName(), string.Format($"pos={ev.Position}; Scale={delta.Scale}; Expansion={delta.Expansion}, Rotation={delta.Rotation}"))) {
            ev.Handled = true;
            if (Math.Abs(1 - delta.Scale) > 0.009) {
#region scale / zoom
               if (delta.Scale > 0)
                  AreaInc(delta.Scale, ev.Position);
               else
                  AreaDec(2 + delta.Scale, ev.Position);
#endregion
            } else {
#region drag
               var needDrag = true;
               var o = GetOffset();
               var sizePage = GetPageSize();
#region check possibility dragging
               if (_clickInfo.CellDown != null)
               {
                  var noMarginPoint = ToCanvasPoint(ev.Position); // new Windows.Foundation.Point(ev.Position.X - o.Left, ev.Position.Y - o.Top);
                  //var inCellRegion = _tmpClickedCell.PointInRegion(noMarginPoint.ToFmRect());
                  //this._contentRoot.Background = new SolidColorBrush(inCellRegion ? Colors.Aquamarine : Colors.DeepPink);
                  var rcOuter = _clickInfo.CellDown.getRcOuter();
                  var min = Math.Min(sizePage.Width/20, sizePage.Height/20);
                  rcOuter.MoveXY(-min, -min);
                  rcOuter.Width  += min*2;
                  rcOuter.Height += min*2;
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

                  var sizeWinMosaic = MosaicField.WindowSize;
                  if ((o.Left + sizeWinMosaic.Width + deltaTrans.X) < MinIndent) {
                     // правый край мозаики пересёк левую сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnX = !_turnX; // разворачиавю по оси X
                     else
                        o.Left = MinIndent - sizeWinMosaic.Width; // привязываю к левой стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  } else
                  if ((o.Left + deltaTrans.X) > (sizePage.Width - MinIndent)) {
                     // левый край мозаики пересёк правую сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnX = !_turnX; // разворачиавю по оси X
                     else
                        o.Left = sizePage.Width - MinIndent; // привязываю к правой стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  }
                  if ((o.Top + sizeWinMosaic.Height + deltaTrans.Y) < MinIndent) {
                     // нижний край мозаики пересёк верхнюю сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnY = !_turnY; // разворачиавю по оси Y
                     else
                        o.Top = MinIndent - sizeWinMosaic.Height; // привязываю к верхней стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  } else
                  if ((o.Top + deltaTrans.Y) > (sizePage.Height - MinIndent)) {
                     // вержний край мозаики пересёк нижнюю сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnY = !_turnY; // разворачиавю по оси Y
                     else
                        o.Top = sizePage.Height - MinIndent; // привязываю к нижней стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  }
#endregion
                  if (applyDelta) {
                     o.Left += deltaTrans.X;
                     o.Top += deltaTrans.Y;
                  }
                  RecheckOffset(ref o, sizeWinMosaic);
                  ApplyOffset(o);
               }
#endregion
            }
            base.OnManipulationDelta(ev);
         }
      }

      protected override void OnManipulationCompleted(ManipulationCompletedRoutedEventArgs ev) {
#if DEBUG
         var pnt1 = this.TransformToVisual(_canvasVirtualControl).TransformPoint(ev.Position);
#else
         var pnt1 = new Windows.Foundation.Point();
#endif
         //var pnt2 = ContentRoot.TransformToVisual(Mosaic.Container).TransformPoint(ev.Position);
         //var content = Window.Current.Content;
         using (new Tracer(GetCallerName(), $"Pos=[{ev.Position} / {pnt1}]; " +
                                            $"Container=[" +
                                            $"{((ev.Container == null) ? "null" : ev.Container.GetType().ToString())}" +
                                            $"]; Cumulative.Translation=[{ev.Cumulative.Translation}]"))
         {
            //ev.Handled = true;
            base.OnManipulationCompleted(ev);
            OnClickLost();
         }
      }
      protected override void OnKeyUp(KeyRoutedEventArgs ev) {
         using (new Tracer(GetCallerName(), "virtKey=" + ev.Key)) {
            base.OnKeyUp(ev);
         }
      }

      private void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs ev) {
         //using (new Tracer(GetCallerName(), "virtKey=" + ev.Key)) {
         ev.Handled = true;
         switch (ev.VirtualKey) {
            case VirtualKey.F2:
               MosaicField.GameNew();
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
               AreaInc();
               break;
            case (VirtualKey)189: // Minus (without Shift)
            case VirtualKey.Subtract: // numpad Minus
               AreaDec();
               break;
            default:
               ev.Handled = false;
               break;
         }
         //}
      }

      private void OnClickBttnBack___________not_binded(object sender, RoutedEventArgs ev) {
         GoBack();
      }
      private void OnClickBttnNewGame___________not_binded(object sender, RoutedEventArgs ev) {
         MosaicField.GameNew();
      }

      private void OnClickBttnSkillBeginner___________not_binded(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eBeginner);
      }
      private void OnClickBttnSkillAmateur___________not_binded(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eAmateur);
      }
      private void OnClickBttnSkillProfi___________not_binded(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eProfi);
      }
      private void OnClickBttnSkillCrazy___________not_binded(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eCrazy);
      }

      Thickness GetOffset() {
         return _contentRoot.Padding;           // variant 1
         //return _canvasVirtualControl.Margin; // variant 2
      }

      private void ApplyOffset(Thickness offset) {
         var pad = _contentRoot.Padding;           // variant 1
         //var pad = _canvasVirtualControl.Margin; // variant 2
         pad.Left = offset.Left;
         pad.Top = offset.Top;
         _contentRoot.Padding = pad;           // variant 1
         //_canvasVirtualControl.Margin = pad; // variant 2
      }

      /// <summary> Перепроверить смещение к полю мозаики так, что поле мозаики было в пределах страницы </summary>
      private void RecheckOffset(ref Thickness offset, SizeDouble sizeWinMosaic) {
         var sizePage = GetPageSize();

         if (offset.Left < (MinIndent - sizeWinMosaic.Width)) { // правый край мозаики пересёк левую сторону страницы/экрана?
            offset.Left = MinIndent - sizeWinMosaic.Width; // привязываю к левой стороне страницы/экрана
         } else {
            if (offset.Left > (sizePage.Width - MinIndent)) // левый край мозаики пересёк правую сторону страницы/экрана?
               offset.Left = sizePage.Width - MinIndent; // привязываю к правой стороне страницы/экрана
         }

         if (offset.Top < (MinIndent - sizeWinMosaic.Height)) { // нижний край мозаики пересёк верхнюю сторону страницы/экрана?
            offset.Top = MinIndent - sizeWinMosaic.Height; // привязываю к верхней стороне страницы/экрана
         } else {
            if (offset.Top > (sizePage.Height - MinIndent)) // вержний край мозаики пересёк нижнюю сторону страницы/экрана?
               offset.Top = sizePage.Height - MinIndent; // привязываю к нижней стороне страницы/экрана
         }
      }

      private void InvalidateCells(IEnumerable<BaseCell> modifiedCells) {
         using (new Tracer()) {
            if (double.IsNaN(_canvasVirtualControl.Width) || double.IsNaN(_canvasVirtualControl.Height))
               return;
            //if ((_canvasVirtualControl.Size.Width == 0) || (_canvasVirtualControl.Size.Height == 0))
            //   return;
            if (!modifiedCells.Any())
               return;

            if (_alreadyPainted && ReferenceEquals(MosaicField.Matrix, modifiedCells)) {
               return;
            } else {
               System.Diagnostics.Debug.Assert(!_alreadyPainted);
            }

            _toRepaint.UnionWith(modifiedCells);

#if DEBUG
            var size = new SizeDouble(_canvasVirtualControl.Width, _canvasVirtualControl.Height); // double values
          //var size = _canvasVirtualControl.Size;                                                // int values
            var tmp = new Windows.Foundation.Rect(0, 0, size.Width, size.Height);
#endif
            foreach (var cell in modifiedCells) {
               var rc = cell.getRcOuter();
#if DEBUG
               var containsLT = tmp.Contains(rc.PointLT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left()) && tmp.Top.HasMinDiff(rc.Top()));
               var containsLB = tmp.Contains(rc.PointLB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left()) && tmp.Top.HasMinDiff(rc.Bottom()));
               var containsRT = tmp.Contains(rc.PointRT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Top()));
               var containsRB = tmp.Contains(rc.PointRB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Bottom()));
               bool intersect = (tmp != Windows.Foundation.Rect.Empty);
               //LoggerSimple.Put($"intersect={intersect}; containsLT={containsLT}; containsLB={containsLB}; containsRT={containsRT}; containsRB={containsRB}");
               System.Diagnostics.Debug.Assert(intersect && containsLT && containsRT && containsLB && containsRB);
               if (!(intersect && containsLT && containsRT && containsLB && containsRB))
                  return;
#endif
               _canvasVirtualControl.Invalidate(rc.ToWinRect());
            }
         }
      }

      ISet<BaseCell> _toRepaint = new HashSet<BaseCell>();
      bool _alreadyPainted = false;
      private void OnRegionsInvalidated(CanvasVirtualControl sender, CanvasRegionsInvalidatedEventArgs ev) {
         using (new Tracer()) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _canvasVirtualControl));
            if (!_toRepaint.Any())
               return;

            _alreadyPainted = true;
            var invalidatedRegions = ev.InvalidatedRegions;
            foreach (var region in invalidatedRegions) {
               using (var ds = sender.CreateDrawingSession(region)) {
                  Repaint(ds, region);

#if DEBUG
                  if (_mouseDevicePosition_AreaChanging.HasValue) {
                     var p = ToCanvasPoint(_mouseDevicePosition_AreaChanging.Value);
                     if (region.Contains(p.ToWinPoint())) {
                        ds.DrawEllipse(new System.Numerics.Vector2((float)p.X, (float)p.Y), 3, 3, Windows.UI.Colors.Red, 2);
                     }
                  }
#endif
               }
            }
            _alreadyPainted = false;
            System.Diagnostics.Debug.Assert(!_toRepaint.Any());
         }
      }

      private void Repaint(CanvasDrawingSession ds, Windows.Foundation.Rect region) {
         using (new Tracer()) {
            var p = new PaintableWin2D(ds);
            var pc = MosaicField.PaintContext;
            // paint all cells
            var sizeMosaic = MosaicField.SizeField;
            var cellPaint = MosaicField.CellPaint;
#if true
            var toRepaintAfter = new HashSet<BaseCell>();
            foreach (var cell in _toRepaint) {
               var tmp = new Windows.Foundation.Rect(region.X, region.Y, region.Width, region.Height);
               tmp.Intersect(cell.getRcOuter().ToWinRect());
               var intersected = (tmp != Windows.Foundation.Rect.Empty);
               if (intersected) {
                  cellPaint.Paint(cell, p, pc);
               } else {
                  toRepaintAfter.Add(cell);
               }
            }
            _toRepaint.Clear();
            _toRepaint = toRepaintAfter;
#else
            foreach (var cell in MosaicField.Matrix) {
               var cell = MosaicField.getCell(i, j);
               var tmp = new Windows.Foundation.Rect(region.X, region.Y, region.Width, region.Height);
               tmp.Intersect(cell.getRcOuter().ToWinRect());
               var intersected = (tmp != Windows.Foundation.Rect.Empty);
               if (intersected)
                  cellPaint.Paint(cell, p, pc);
            }
#endif
         }
      }


      private PointDouble ToCanvasPoint(Windows.Foundation.Point pagePoint) {
         var point = TransformToVisual(_canvasVirtualControl).TransformPoint(pagePoint).ToFmPointDouble();
         //var o = GetOffset();
         //var point2 = new PointDouble(pagePoint.X - o.Left, pagePoint.Y - o.Top);
         //System.Diagnostics.Debug.Assert(point == point2);
         return point;
      }

      static string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) { return callerName; }

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

}
