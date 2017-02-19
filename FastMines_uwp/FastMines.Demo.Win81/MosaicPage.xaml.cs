using System;
using System.ComponentModel;
using System.Diagnostics;
using Windows.System;
using Windows.Devices.Input;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.mosaic;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.core.types.click;
using fmg.data.controller.types;
using fmg.uwp.mosaic;
using fmg.uwp.mosaic.xaml;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
namespace FastMines {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class MosaicPage : Page {
      /// <summary> мин отступ от краев экрана для мозаики </summary>
      private const int MinIndent = 30;

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
            if (_mosaic == null) {
               _mosaic = new Mosaic();
               ContentRoot.Children.Add(_mosaic.Container);

               _mosaic.PropertyChanged += OnMosaicPropertyChanged;
            }
            return _mosaic;
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
               MosaicField.Repaint();
            }, CoreDispatcherPriority.High);
         }
      }

      protected override void OnNavigatedTo(NavigationEventArgs e) {
         base.OnNavigatedTo(e);

         var initParam = e.Parameter as MosaicPageInitParam;
         Debug.Assert(initParam != null);
         MosaicField.SizeField = initParam.SizeField;
         MosaicField.MosaicType = initParam.MosaicTypes;
         MosaicField.MinesCount = initParam.MinesCount;

         MosaicField.PaintContext.BackgroundColor = PaintUwpContextCommon.DefaultBackgroundColor;

         // if () // TODO: check if no tablet
         {
            ToolTipService.SetToolTip(bttnNewGame, new ToolTip {Content = "F2"});
            ToolTipService.SetToolTip(bttnSkillBeginner, new ToolTip {Content = "1"});
            ToolTipService.SetToolTip(bttnSkillAmateur, new ToolTip { Content = "2" });
            ToolTipService.SetToolTip(bttnSkillCrazy, new ToolTip { Content = "3" });
            ToolTipService.SetToolTip(bttnSkillProfi, new ToolTip {Content = "4"});
         }
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
         MosaicField.MosaicType = MosaicField.MosaicType;
         MosaicField.MinesCount = numberMines;

         //if (getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected()) {
         //   AreaMax();
         //   RecheckLocation(false, false);
         //} else
            RecheckLocation(true, true);

         //if (!isMenuEvent(e))
         //   RecheckSelectedMenuSkillLevel();
      }

      /// <summary> узнать размер окна проекта при указанном размере окна мозаики </summary>
      [Obsolete]
      Size CalcSize(Size sizeMosaicInPixel) {
         // под WinRT окно проекта === текущая страница
         // и т.к. нет ни меню, ни заголовка, ни тулбара, ни строки состояния
         // то размер окна равен как есть размеру в пикселях самой мозаики
         return sizeMosaicInPixel;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором ... удобно... </summary>
      /// <param name="mosaicSizeField">интересуемый размер поля мозаики</param>
      /// <returns>макс площадь ячейки</returns>
      private double CalcMaxArea(Matrisize mosaicSizeField) {
         var sizePage = Window.Current.Bounds;
         return sizePage.Width/3 * sizePage.Height/3;
      }

      /// <summary> проверить что находится в рамках экрана	</summary>
      /// <param name="checkArea">заодно проверить что влазит в текущее разрешение экрана</param>
      private void RecheckLocation(bool checkArea, bool pack) {
         if (checkArea) {
            var maxArea = CalcMaxArea(MosaicField.SizeField);
            if (maxArea < Area)
               Area = maxArea;
         }
      }

      double Area {
         get {
            return MosaicField.Area;
         }
         set {
            value = Math.Min(value, CalcMaxArea(MosaicField.SizeField)); // recheck

            var curArea = MosaicField.Area;
            if (curArea.HasMinDiff(value))
               return;

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
         if (maxArea.HasMinDiff(Area))
            return;
         Area = maxArea;
      }

      void AreaOptimal() {
         var sizePage = Window.Current.Bounds.ToFmRectDouble().SizeDouble();
         Area = MosaicHelper.FindAreaBySize(MosaicField.MosaicType, MosaicField.SizeField, ref sizePage);
      }

      private void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case "MosaicType":
            Mosaic_OnChangedMosaicType(sender as Mosaic, ev as PropertyChangedExEventArgs<EMosaic>);
            break;
         case "Area":
            Mosaic_OnChangedArea(sender as Mosaic, ev as PropertyChangedExEventArgs<double>);
            break;
         case "GameStatus":
            Mosaic_OnChangedGameStatus(sender as Mosaic, ev as PropertyChangedExEventArgs<EGameStatus>);
            break;
         case "SizeField":
            Mosaic_OnChangedSizeField(sender as Mosaic, ev as PropertyChangedExEventArgs<Matrisize>);
            break;
         case "MinesCount":
            Mosaic_OnChangedMinesCount(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case "CountFlag":
            Mosaic_OnChangedCountFlag(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case "CountOpen":
            Mosaic_OnChangedCountOpen(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case "CountMinesLeft":
            Mosaic_OnChangedCountMinesLeft(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
            break;
         case "CountClick":
            Mosaic_OnChangedCountClick(sender as Mosaic, ev as PropertyChangedExEventArgs<int>);
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
      }

      private void OnPageSizeChanged(object sender, RoutedEventArgs e) {
         AreaOptimal();
      }

      private void Mosaic_OnClick(ClickResult clickResult) {
         _clickInfo.CellDown = clickResult.CellDown;
         //_clickInfo.IsLeft = clickResult.IsLeft;
         if (clickResult.IsDown)
            _clickInfo.DownHandled = clickResult.IsAnyChanges;
         else
            _clickInfo.UpHandled = clickResult.IsAnyChanges;
         _clickInfo.Released = !clickResult.IsDown;
      }

      private void Mosaic_OnChangedGameStatus(Mosaic sender, PropertyChangedExEventArgs<EGameStatus> ev) {
         Debug.Assert(ReferenceEquals(sender, MosaicField));
         if (sender.GameStatus == EGameStatus.eGSEnd) {
            //this.bottomAppBar.Focus(FocusState.Programmatic);
            bottomAppBar.IsOpen = true;
         }
      }
      private void Mosaic_OnChangedMinesCount(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountFlag(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountOpen(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountMinesLeft(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedCountClick(Mosaic sender, PropertyChangedExEventArgs<int> ev) { }
      private void Mosaic_OnChangedArea(Mosaic sender, PropertyChangedExEventArgs<double> ev) {
         Debug.Assert(ReferenceEquals(sender, MosaicField));
         using (new Tracer()) {
            //ChangeSizeImagesMineFlag();

            //MosaicField.Container.Margin = new Thickness();

            var m = MosaicField.Container.Margin;
            if (_mouseDevicePosition_AreaChanging.HasValue) {
               var devicePos = _mouseDevicePosition_AreaChanging.Value;

               var oldWinSize = MosaicField.GetWindowSize(MosaicField.SizeField, ev.OldValue);
               var newWinSize = MosaicField.GetWindowSize(MosaicField.SizeField, Area);

               // точка над игровым полем со старой площадью ячеек
               var point = new PointDouble(devicePos.X - m.Left, devicePos.Y - m.Top);
               var percent = new Tuple<double, double>(point.X*100/oldWinSize.Width, point.Y*100/oldWinSize.Height);

               // таже точка над игровым полем, но с учётом zoom'а (новой площади)
               point = new PointDouble(newWinSize.Width*percent.Item1/100, newWinSize.Height*percent.Item2/100);

               // смещаю игровое поле так, чтобы точка была на том же месте экрана
               m.Left = devicePos.X - point.X;
               m.Top = devicePos.Y - point.Y;

               m = CheckMosaicMargin(m, newWinSize);
            } else {
               var sizeWinMosaic = MosaicField.WindowSize;
               var sizePage = Window.Current.Bounds;
               m.Left = (sizePage.Width - sizeWinMosaic.Width)/2;
               m.Top = (sizePage.Height - sizeWinMosaic.Height)/2;
            }
            MosaicField.Container.Margin = m;
         }
      }
      private void Mosaic_OnChangedMosaicType(Mosaic sender, PropertyChangedExEventArgs<EMosaic> ev) {
         Debug.Assert(ReferenceEquals(sender, MosaicField));
         using (new Tracer()) {
            //sender.ChangeFontSize();
            //ChangeSizeImagesMineFlag();
         }
      }

      private void Mosaic_OnChangedSizeField(Mosaic sender, PropertyChangedExEventArgs<Matrisize> ev) {
         Debug.Assert(ReferenceEquals(sender, MosaicField));
         using (new Tracer()) {
         }
      }

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

      bool OnClick(Windows.Foundation.Point pos, bool leftClick, bool downHandling, bool upHandling) {
         var margin = MosaicField.Container.Margin;
         //if ((pos.X >= margin.Left) && (pos.Y >= margin.Top)) {
         var point = pos.ToFmPointDouble().Move(-margin.Left, -margin.Top);
         //   var winSize = MosaicField.WindowSize;
         //   if ((point.x <= winSize.width) && (point.y <= winSize.height)) {
            var handled = false;
            if (downHandling) {
               var clickResult = MosaicField.MousePressed(point, leftClick);
               if (clickResult != null) {
                  Mosaic_OnClick(clickResult);
                  handled |= _clickInfo.DownHandled;
               }
            }
            if (upHandling) {
               var clickResult = MosaicField.MouseReleased(point, leftClick);
               Mosaic_OnClick(clickResult);
               handled |= _clickInfo.UpHandled;
            }
            return handled;
         //   }
         //}
         //return false;
      }

      protected override void OnTapped(TappedRoutedEventArgs ev) {
         using (new Tracer("OnTapped", () => "ev.Handled = " + ev.Handled)) {
            //if (!_manipulationStarted) {
            if (ev.PointerDeviceType != PointerDeviceType.Mouse) {
               ev.Handled = OnClick(ev.GetPosition(this), true, false, true);
            }
            if (!ev.Handled)
               base.OnTapped(ev);
         }
      }

      protected override void OnRightTapped(RightTappedRoutedEventArgs ev) {
         using (new Tracer("OnRightTapped", () => "ev.Handled = " + ev.Handled)) {
            if (ev.PointerDeviceType == PointerDeviceType.Mouse)
               ev.Handled = _clickInfo.DownHandled || _clickInfo.UpHandled; // TODO: для избежания появления appBar'ов при установке '?'
            else if (!_manipulationStarted) {

               // 1. release left click in invalid coord
               OnClick(new Windows.Foundation.Point(-1, -1), true, false, true);

               // 2. make right click - up & down
               var pos = ev.GetPosition(this);
               ev.Handled = OnClick(pos, false, true, true);
            }

            if (!ev.Handled)
               base.OnRightTapped(ev);

         }
      }

      protected override void OnPointerPressed(PointerRoutedEventArgs ev) {
         using (new Tracer("OnPointerPressed", () => "ev.Handled = " + ev.Handled)) {

            var pointerPoint = ev.GetCurrentPoint(this);
            //_clickInfo.PointerDevice = pointerPoint.PointerDevice.PointerDeviceType;
            var props = pointerPoint.Properties;
            // Ignore button chords with the left, right, and middle buttons
            if (!props.IsLeftButtonPressed && !props.IsRightButtonPressed && !props.IsMiddleButtonPressed) {
               // If back or foward are pressed (but not both) navigate appropriately
               var backPressed = props.IsXButton1Pressed;
               if (backPressed) {
                  ev.Handled = true;
                  GoBack();
               }
            }

            if (!ev.Handled)
               ev.Handled = OnClick(pointerPoint.Position, props.IsLeftButtonPressed, true, false);

            _clickInfo.DownHandled = ev.Handled;
            if (!ev.Handled)
               base.OnPointerPressed(ev);

         }
      }

      protected override void OnPointerReleased(PointerRoutedEventArgs ev) {
         using (new Tracer("OnPointerReleased", "_manipulationStarted = " + _manipulationStarted, () => "ev.Handled = " + ev.Handled)) {
            var pointerPoint = ev.GetCurrentPoint(this);
            //if (_manipulationStarted)
            if (ev.Pointer.PointerDeviceType == PointerDeviceType.Mouse) {
               var isLeftClick = (pointerPoint.Properties.PointerUpdateKind == PointerUpdateKind.LeftButtonReleased);
               var isRightClick = (pointerPoint.Properties.PointerUpdateKind == PointerUpdateKind.RightButtonReleased);
               Debug.Assert(isLeftClick != isRightClick);
               ev.Handled = OnClick(pointerPoint.Position, isLeftClick, false, true);
            } else {
               AsyncRunner.InvokeFromUiLater(() => {
                  if (!_clickInfo.Released) {
                     LoggerSimple.Put("ã OnPointerReleased: forced left release click...");
                     OnClick(pointerPoint.Position, true, false, true);
                  }
               }, CoreDispatcherPriority.High);
            }

            _clickInfo.UpHandled = ev.Handled;
            if (!ev.Handled)
               base.OnPointerReleased(ev);

         }
      }

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
         using (var tracer = new Tracer("OnManipulationDelta", string.Format("Scale={0}; Expansion={1}, Rotation={2}", ev.Delta.Scale, ev.Delta.Expansion, ev.Delta.Rotation))) {
            ev.Handled = true;
            if (Math.Abs(1 - ev.Delta.Scale) > 0.009) {
#region scale / zoom
               if (ev.Delta.Scale > 0)
                  AreaInc(ev.Delta.Scale, ev.Position);
               else
                  AreaDec(2 + ev.Delta.Scale, ev.Position);
#endregion
            } else {
#region drag
               var needDrag = true;
               var margin = MosaicField.Container.Margin;
#region check possibility dragging
               if (_clickInfo.CellDown != null)
               {
                  var noMarginPoint = new Windows.Foundation.Point(ev.Position.X - margin.Left, ev.Position.Y - margin.Top);
                  //var inCellRegion = _tmpClickedCell.PointInRegion(noMarginPoint.ToFmRect());
                  //this.ContentRoot.Background = new SolidColorBrush(inCellRegion ? Colors.Aquamarine : Colors.DeepPink);
                  var rcOuter = _clickInfo.CellDown.getRcOuter();
                  var sizePage = Window.Current.Bounds.ToFmRect().Size();
                  var delta = Math.Min(sizePage.Width/20, sizePage.Height/20);
                  rcOuter.MoveXY(-delta, -delta);
                  rcOuter.Width  += delta*2;
                  rcOuter.Height += delta*2;
                  needDrag = !rcOuter.Contains(noMarginPoint.ToFmPointDouble());
               }
#endregion

               if (needDrag) {
                  var deltaTrans = ev.Delta.Translation;
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
                  var sizePage = Window.Current.Bounds.ToFmRect().Size();
                  if ((margin.Left + sizeWinMosaic.Width + deltaTrans.X) < MinIndent) {
                     // правый край мозаики пересёк левую сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnX = !_turnX; // разворачиавю по оси X
                     else
                        margin.Left = MinIndent - sizeWinMosaic.Width; // привязываю к левой стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  } else
                  if ((margin.Left + deltaTrans.X) > (sizePage.Width - MinIndent)) {
                     // левый край мозаики пересёк правую сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnX = !_turnX; // разворачиавю по оси X
                     else
                        margin.Left = sizePage.Width - MinIndent; // привязываю к правой стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  }
                  if ((margin.Top + sizeWinMosaic.Height + deltaTrans.Y) < MinIndent) {
                     // нижний край мозаики пересёк верхнюю сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnY = !_turnY; // разворачиавю по оси Y
                     else
                        margin.Top = MinIndent - sizeWinMosaic.Height; // привязываю к верхней стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  } else
                  if ((margin.Top + deltaTrans.Y) > (sizePage.Height - MinIndent)) {
                     // вержний край мозаики пересёк нижнюю сторону страницы/экрана
                     if (ev.IsInertial)
                        _turnY = !_turnY; // разворачиавю по оси Y
                     else
                        margin.Top = sizePage.Height - MinIndent; // привязываю к нижней стороне страницы/экрана
                     applyDelta = ev.IsInertial;
                  }
#endregion
                  if (applyDelta) {
                     margin.Left += deltaTrans.X;
                     margin.Top += deltaTrans.Y;
                  }
#if DEBUG
                  var tmp = margin;
                  margin = CheckMosaicMargin(margin, sizeWinMosaic);
                  System.Diagnostics.Debug.Assert(tmp == margin);
#endif
                  MosaicField.Container.Margin = margin;
               }
#endregion
            }
            base.OnManipulationDelta(ev);
         }
      }

      protected override void OnManipulationCompleted(ManipulationCompletedRoutedEventArgs ev) {
#if DEBUG
         var pnt1 = this.TransformToVisual(MosaicField.Container).TransformPoint(ev.Position);
#else
         var pnt1 = new Windows.Foundation.Point();
#endif
         //var pnt2 = ContentRoot.TransformToVisual(Mosaic.Container).TransformPoint(ev.Position);
         //var content = Window.Current.Content;
         using (new Tracer("OnManipulationCompleted", $"Pos=[{ev.Position} / {pnt1}]; " +
                                                      "Container=[" +
                                                         $"{((ev.Container == null) ? "null" : ev.Container.GetType().ToString())}" +
                                                      $"]; Cumulative.Translation=[{ev.Cumulative.Translation}]"))
         {
            //e.Handled = true;
            base.OnManipulationCompleted(ev);
            MosaicField.MouseFocusLost();
         }
      }
      protected override void OnKeyUp(KeyRoutedEventArgs ev) {
         using (new Tracer("OnKeyUp", "virtKey=" + ev.Key)) {
            base.OnKeyUp(ev);
         }
      }

      private void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs ev) {
         //using (new Tracer("OnKeyUp_CoreWindow", "virtKey=" + ev.Key)) {
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

      private void OnClickBttnBack(object sender, RoutedEventArgs ev) {
         GoBack();
      }
      private void OnClickBttnNewGame(object sender, RoutedEventArgs ev) {
         topAppBar.IsOpen = false;
         bottomAppBar.IsOpen = false;
         MosaicField.GameNew();
      }

      private void OnClickBttnSkillBeginner(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eBeginner);
      }
      private void OnClickBttnSkillAmateur(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eAmateur);
      }
      private void OnClickBttnSkillProfi(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eProfi);
      }
      private void OnClickBttnSkillCrazy(object sender, RoutedEventArgs ev) {
         SetGame(ESkillLevel.eCrazy);
      }

      /// <summary> Перепроверить Margin поля мозаики так, что бы при нём поле мозаки было в пределах страницы </summary>
      private Thickness CheckMosaicMargin(Thickness? newMargin = null, SizeDouble? sizeWinMosaic = null) {
         var margin = newMargin ?? MosaicField.Container.Margin;
         if (!sizeWinMosaic.HasValue)
            sizeWinMosaic = MosaicField.WindowSize;
         var sizePage = Window.Current.Bounds.ToFmRect().Size();

         if ((margin.Left + sizeWinMosaic.Value.Width) < MinIndent) {
            // правый край мозаики пересёк левую сторону страницы/экрана
            margin.Left = MinIndent - sizeWinMosaic.Value.Width; // привязываю к левой стороне страницы/экрана
         } else
            if (margin.Left > (sizePage.Width - MinIndent)) {
               // левый край мозаики пересёк правую сторону страницы/экрана
               margin.Left = sizePage.Width - MinIndent; // привязываю к правой стороне страницы/экрана
            }
         if ((margin.Top + sizeWinMosaic.Value.Height) < MinIndent) {
            // нижний край мозаики пересёк верхнюю сторону страницы/экрана
            margin.Top = MinIndent - sizeWinMosaic.Value.Height; // привязываю к верхней стороне страницы/экрана
         } else
            if (margin.Top > (sizePage.Height - MinIndent)) {
               // вержний край мозаики пересёк нижнюю сторону страницы/экрана
               margin.Top = sizePage.Height - MinIndent; // привязываю к нижней стороне страницы/экрана
            }

         return margin;
      }

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
