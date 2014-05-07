using System;
using System.Diagnostics;
using System.Threading.Tasks;
using Windows.System;
using Windows.System.Threading;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using Windows.Devices.Input;
using ua.ksn.geom;
using ua.ksn.fmg.controller.win_rt;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt;
using ua.ksn.fmg.controller;
using ua.ksn.fmg.controller.types;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
namespace FastMines {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class MosaicPage : Page {
      /// <summary> мин отступ от краев экрана для мозаики </summary>
      private const int MinIndent = 30;

      private MosaicExt _mosaic;

      public MosaicExt MosaicField {
         get {
            if (_mosaic == null) {
               _mosaic = new MosaicExt();
               ContentRoot.Children.Add(_mosaic.Container);

               _mosaic.OnClick += Mosaic_OnClick;
               _mosaic.OnChangedGameStatus += Mosaic_OnChangedGameStatus;
               _mosaic.OnChangedCounters += Mosaic_OnChangedCounters;
               _mosaic.OnChangedArea += Mosaic_OnChangedArea;
               _mosaic.OnChangedMosaicType += Mosaic_OnChangedMosaicType;
               _mosaic.OnChangedMosaicSize += Mosaic_OnChangedMosaicSize;
            }
            return _mosaic;
         }
      }

      public MosaicPage() {
         this.InitializeComponent();

         this.Loaded += OnPageLoaded;
         this.Unloaded += OnPageUnloaded;
         this.SizeChanged += OnPageSizeChanged;
         //this.PointerMoved += OnPagePointerMoved;
         this.ManipulationMode =
            ManipulationModes.TranslateX |
            ManipulationModes.TranslateY |
            ManipulationModes.Rotate |
            ManipulationModes.Scale |
            ManipulationModes.TranslateInertia;
         this.Tapped += OnPageTapped;

         if (Windows.ApplicationModel.DesignMode.DesignModeEnabled) {
            InvokeLater(async () => {
               await MosaicField.SetParams(new Size(10, 10), EMosaic.eMosaicRhombus1, 3);
               MosaicField.Area = 1500;
               MosaicField.Repaint();
            }, CoreDispatcherPriority.High, true);
         }
      }

      private static Windows.Foundation.IAsyncAction ExecuteOnUIThread(DispatchedHandler action, CoreDispatcherPriority priority) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
      }

      private static Windows.Foundation.IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal, bool bAwait = false) {
         return bAwait
            ? ThreadPool.RunAsync(async delegate { await ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority)
            : ThreadPool.RunAsync(delegate { ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority);
      }

      protected override async void OnNavigatedTo(NavigationEventArgs e) {
         base.OnNavigatedTo(e);

         var initParam = e.Parameter as MosaicPageInitParam;
         Debug.Assert(initParam != null);
         await MosaicField.SetParams(initParam.SizeField, initParam.MosaicTypes, initParam.MinesCount);

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
      async Task SetGame(ESkillLevel skill) {
         //if (isPaused())
         //   ChangePause(e);

         int numberMines;
         Size sizeFld;
         if (skill == ESkillLevel.eCustom) {
            //System.out.println("... dialog box 'Select custom skill level...' ");
            //getCustomSkillDialog().setVisible(!getCustomSkillDialog().isVisible());
            return;
         } else {
            numberMines = skill.GetNumberMines(MosaicField.MosaicType);
            sizeFld = skill.DefaultSize();
         }

         await MosaicField.SetParams(sizeFld, MosaicField.MosaicType, numberMines);

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

      /// <summary> Поиск больше-меньше </summary>
      /// <param name="baseMin">стартовое значение для поиска</param>
      /// <param name="baseDelta">начало дельты приращения</param>
      /// <param name="comparable">ф-ция сравнения</param>
      /// <returns>что найдено</returns>
      private static int Finder(int baseMin, int baseDelta, Func<int, int> comparable) {
         double res = baseMin;
         double d = baseDelta;
         bool deltaUp = true, lastSmall = true;
         do {
            if (deltaUp)
               d *= 2;
            else
               d /= 2;

            if (lastSmall)
               res += d;
            else
               res -= d;

            int z = comparable((int)res);
            if (z == 0)
               return (int)res;
            lastSmall = (z < 0);
            deltaUp = deltaUp && lastSmall;
         } while (d > 1);
         return (int)res;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором ... удобно... </summary>
      /// <param name="mosaicSizeField">интересуемый размер поля мозаики</param>
      /// <returns>макс площадь ячейки</returns>
      private int CalcMaxArea(Size mosaicSizeField) {
         var sizePage = Window.Current.Bounds;
         return (int) (sizePage.Width/3 * sizePage.Height/3);
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается на странице </summary>
      /// <param name="mosaicSizeField">интересуемый размер поля мозаики</param>
      /// <returns>макс площадь ячейки</returns>
      private int CalcOptimalArea(Size mosaicSizeField) {
         var sizePage = Window.Current.Bounds.ToFmRect().size();
         return Finder(ua.ksn.fmg.controller.Mosaic.AREA_MINIMUM, ua.ksn.fmg.controller.Mosaic.AREA_MINIMUM,
            area => {
               var sizeMosaic = MosaicField.CalcWindowSize(mosaicSizeField, area);
               var sizeWnd = CalcSize(sizeMosaic);
               if ((sizeWnd.width == sizePage.width) &&
                   (sizeWnd.height == sizePage.height))
                  return 0;
               if ((sizeWnd.width <= sizePage.width) &&
                   (sizeWnd.height <= sizePage.height))
                  return -1;
               return +1;
            });
      }

      /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается на странице </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <returns>max размер поля мозаики</returns>
      public Size CalcMaxMosaicSize(int area) {
         var sizePage = Window.Current.Bounds.ToFmRect().size(); // Window.Current.Content.RenderSize
         var result = new Size();
         Finder(1, 10, newWidth => {
            result.width = newWidth;
            var sizeMosaic = MosaicField.CalcWindowSize(result, area);
            var sizeWnd = CalcSize(sizeMosaic);
            if (sizeWnd.width == sizePage.width)
               return 0;
            if (sizeWnd.width <= sizePage.width)
               return -1;
            return +1;
         });
         Finder(1, 10, newHeight => {
            result.height = newHeight;
            var sizeMosaic = MosaicField.CalcWindowSize(result, area);
            var sizeWnd = CalcSize(sizeMosaic);
            if (sizeWnd.width == sizePage.height)
               return 0;
            if (sizeWnd.height <= sizePage.height)
               return -1;
            return +1;
         });
         return result;
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

      private Windows.Foundation.Point? _mouseDevicePosition_AreaChanging = null;
      int Area { get
         {
            return MosaicField.Area;
         }
         set {
            value = Math.Min(value, CalcMaxArea(MosaicField.SizeField)); // recheck

            var curArea = MosaicField.Area;
            if (curArea == value)
               return;

            MosaicField.Area = value;
         }
      }

      /// <summary> Zoom + </summary>
      void AreaInc(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
         _mouseDevicePosition_AreaChanging = mouseDevicePosition;
         Area = (int)(Area * 1.01 * zoomPower);
      }

      /// <summary> Zoom - </summary>
      void AreaDec(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
         _mouseDevicePosition_AreaChanging = mouseDevicePosition;
         Area = (int)(Area * 0.99 / zoomPower);
      }

      /// <summary> Zoom minimum </summary>
      void AreaMin() {
         Area = 0;
      }

      /// <summary> Zoom maximum </summary>
      void AreaMax() {
         var maxArea = CalcMaxArea(MosaicField.SizeField);
         if (maxArea == Area)
            return;
         Area = maxArea;
      }

      void AreaOptimal() {
         Area = CalcOptimalArea(MosaicField.SizeField);
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

      private void Mosaic_OnClick(Mosaic source, bool leftClick, bool down) { }
      private void Mosaic_OnChangedGameStatus(Mosaic source, EGameStatus oldValue) {}
      private void Mosaic_OnChangedCounters(Mosaic source) {}
      private void Mosaic_OnChangedArea(Mosaic source, int oldArea) {
         Debug.WriteLine("Mosaic_OnChangedArea");
         Debug.Assert(ReferenceEquals(MosaicField, source));
         //ChangeSizeImagesMineFlag();

         //MosaicField.Container.Margin = new Thickness();

         var m = MosaicField.Container.Margin;
         if (_mouseDevicePosition_AreaChanging.HasValue) {
            var devicePos = _mouseDevicePosition_AreaChanging.Value;

            var oldWinSize = MosaicField.CalcWindowSize(MosaicField.SizeField, oldArea);
            var newWinSize = MosaicField.CalcWindowSize(MosaicField.SizeField, Area);

            // точка над игровым полем со старой площадью ячеек
            var point = new PointDouble(devicePos.X - m.Left, devicePos.Y - m.Top);
            var percent = new Tuple<double, double>(point.x*100/oldWinSize.width, point.y*100/oldWinSize.height);

            // таже точка над игровым полем, но с учётом zoom'а (новой площади)
            point = new PointDouble(newWinSize.width*percent.Item1/100, newWinSize.height*percent.Item2/100);

            // смещаю игровое поле так, чтобы точка была на том же месте экрана
            m.Left = devicePos.X - point.x;
            m.Top = devicePos.Y - point.y;

            m = CheckMosaicMargin(m, newWinSize);
         } else {
            var sizeWinMosaic = MosaicField.WindowSize;
            var sizePage = Window.Current.Bounds;
            m.Left = (sizePage.Width - sizeWinMosaic.width)/2;
            m.Top = (sizePage.Height - sizeWinMosaic.height)/2;
         }
         MosaicField.Container.Margin = m;
      }
      private void Mosaic_OnChangedMosaicType(Mosaic source, EMosaic oldMosaic) {
         Debug.WriteLine("Mosaic_OnChangedMosaicType");
         Debug.Assert(ReferenceEquals(MosaicField, source));
         (source as MosaicExt).ChangeFontSize();
         //ChangeSizeImagesMineFlag();
      }

      private void Mosaic_OnChangedMosaicSize(Mosaic source, Size oldSize) {
         Debug.WriteLine("Mosaic_OnChangedMosaicSize");
         Debug.Assert(ReferenceEquals(MosaicField, source));
      }

      private static double? _baseWheelDelta;
      protected override void OnPointerWheelChanged(PointerRoutedEventArgs e) {
         //Debug.WriteLine("virt_OnPointerWheelChanged");
         var wheelDelta = e.GetCurrentPoint(this).Properties.MouseWheelDelta;
         if (!_baseWheelDelta.HasValue)
            _baseWheelDelta = Math.Abs(wheelDelta);

         var wheelPower = 1 + ((Math.Abs(wheelDelta)/_baseWheelDelta.Value) - 1)/10;

         if (wheelDelta > 0)
            AreaInc(wheelPower, e.GetCurrentPoint(null).Position);
         else
            AreaDec(wheelPower, e.GetCurrentPoint(null).Position);

         e.Handled = true;
         base.OnPointerWheelChanged(e);
      }

      private void OnPagePointerMoved(object sender, PointerRoutedEventArgs e) {
         var ptr = e.Pointer;
         if (!ptr.IsInContact)
            return;
         Debug.WriteLine("OnPointerMoved: " + e.Pointer.PointerDeviceType);
         if (ptr.PointerDeviceType == PointerDeviceType.Mouse) {
            var ptrPt = e.GetCurrentPoint(this);
            if (ptrPt.Properties.IsLeftButtonPressed) {
               //eventLog.Text += "\nLeft button: " + ptrPt.PointerId;
            }
            if (ptrPt.Properties.IsMiddleButtonPressed) {
               //eventLog.Text += "\nWheel button: " + ptrPt.PointerId;
            }
            if (ptrPt.Properties.IsRightButtonPressed) {
               //eventLog.Text += "\nRight button: " + ptrPt.PointerId;
            }
            //Debug.WriteLine("cursorPos=[{0}]; ContactRect=[{1}; {2}]", ptrPt.RawPosition, ptrPt.Properties.ContactRect, ptrPt.Properties.ContactRectRaw);
            //Debug.WriteLine(" ContactRect=[{0}; {1}]", ptrPt.Properties.ContactRect, ptrPt.Properties.ContactRectRaw);
         }
      }

      private void OnPageTapped(object sender, TappedRoutedEventArgs e) {
         Debug.WriteLine("OnTapped: ");
      }

      protected override void OnManipulationStarting(ManipulationStartingRoutedEventArgs e) {
         Debug.WriteLine("> OnManipulationStarting");
         base.OnManipulationStarting(e);
         Debug.WriteLine("> OnManipulationStarting");
      }

      private bool _turnX;
      private bool _turnY;
      private DateTime _dtInertiaStarting;

      protected override void OnManipulationStarted(ManipulationStartedRoutedEventArgs e) {
         Debug.WriteLine("> OnManipulationStarted");
         _turnX = _turnY = false;
         _dtInertiaStarting = DateTime.MinValue;
         base.OnManipulationStarted(e);
         Debug.WriteLine("< OnManipulationStarted");
      }

      protected override void OnManipulationInertiaStarting(ManipulationInertiaStartingRoutedEventArgs e) {
         _dtInertiaStarting = DateTime.Now;
         base.OnManipulationInertiaStarting(e);
      }

      protected override void OnManipulationDelta(ManipulationDeltaRoutedEventArgs ev) {
         Debug.WriteLine("> OnManipulationDelta: Scale={0}; Expansion={1}, Rotation={2}", ev.Delta.Scale, ev.Delta.Expansion, ev.Delta.Rotation);
         ev.Handled = true;
         if (Math.Abs(1 - ev.Delta.Scale) > 0.009) {
            if (ev.Delta.Scale > 0)
               AreaInc(ev.Delta.Scale, ev.Position);
            else
               AreaDec(2 + ev.Delta.Scale, ev.Position);
         } else {
            var margin = MosaicField.Container.Margin;
            var deltaTrans = ev.Delta.Translation;
            var applyDelta = true;
#region Compound motion
            if (_turnX)
               deltaTrans.X *= -1;
            if (_turnY)
               deltaTrans.Y *= -1;

            if (ev.IsInertial) {
               //var сoefFading = Math.Max(0.05, 1 - 0.32 * (DateTime.Now - _dtInertiaStarting).TotalSeconds);
               var сoefFading = Math.Max(0, 1 - 0.32 * (DateTime.Now - _dtInertiaStarting).TotalSeconds);
               Debug.WriteLine("  OnManipulationDelta: inertial coeff fading = " + сoefFading);
               deltaTrans.X *= сoefFading;
               deltaTrans.Y *= сoefFading;
            }

            var sizeWinMosaic = MosaicField.WindowSize;
            var sizePage = Window.Current.Bounds.ToFmRect().toSize();
            if ((margin.Left + sizeWinMosaic.width + deltaTrans.X) < MinIndent) {
               // правый край мозаики пересёк левую сторону страницы/экрана
               if (ev.IsInertial)
                  _turnX = !_turnX; // разворачиавю по оси X
               else
                  margin.Left = MinIndent - sizeWinMosaic.width; // привязываю к левой стороне страницы/экрана
               applyDelta = ev.IsInertial;
            } else
            if ((margin.Left + deltaTrans.X) > (sizePage.width - MinIndent)) {
               // левый край мозаики пересёк правую сторону страницы/экрана
               if (ev.IsInertial)
                  _turnX = !_turnX; // разворачиавю по оси X
               else
                  margin.Left = sizePage.width - MinIndent; // привязываю к правой стороне страницы/экрана
               applyDelta = ev.IsInertial;
            }
            if ((margin.Top + sizeWinMosaic.height + deltaTrans.Y) < MinIndent) {
               // нижний край мозаики пересёк верхнюю сторону страницы/экрана
               if (ev.IsInertial)
                  _turnY = !_turnY; // разворачиавю по оси Y
               else
                  margin.Top = MinIndent - sizeWinMosaic.height; // привязываю к верхней стороне страницы/экрана
               applyDelta = ev.IsInertial;
            } else
            if ((margin.Top + deltaTrans.Y) > (sizePage.height - MinIndent)) {
               // вержний край мозаики пересёк нижнюю сторону страницы/экрана
               if (ev.IsInertial)
                  _turnY = !_turnY; // разворачиавю по оси Y
               else
                  margin.Top = sizePage.height - MinIndent; // привязываю к нижней стороне страницы/экрана
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
         base.OnManipulationDelta(ev);
         Debug.WriteLine("< OnManipulationDelta");
      }

      protected override void OnManipulationCompleted(ManipulationCompletedRoutedEventArgs e) {
#if DEBUG
         var pnt1 = this.TransformToVisual(MosaicField.Container).TransformPoint(e.Position);
         //var pnt2 = ContentRoot.TransformToVisual(Mosaic.Container).TransformPoint(e.Position);
         //var content = Window.Current.Content;
         Debug.WriteLine("> OnManipulationCompleted: Pos=[{0} / {1}]; Container=[{2}]; Cumulative.Translation=[{3}]",
            e.Position, pnt1, (e.Container == null) ? "null" : e.Container.GetType().ToString(),
            e.Cumulative.Translation);
#endif
         //e.Handled = true;
         base.OnManipulationCompleted(e);
         Debug.WriteLine("< OnManipulationCompleted");
      }

      protected override void OnPointerPressed(PointerRoutedEventArgs ev) {
         Debug.WriteLine("> OnPointerPressed: ");
         var props = ev.GetCurrentPoint(this).Properties;

         // Ignore button chords with the left, right, and middle buttons
         if (!props.IsLeftButtonPressed && !props.IsRightButtonPressed && !props.IsMiddleButtonPressed) {
            // If back or foward are pressed (but not both) navigate appropriately
            var backPressed = props.IsXButton1Pressed;
            if (backPressed) {
               ev.Handled = true;
               GoBack();
            }
         }
         base.OnPointerPressed(ev);
         Debug.WriteLine("< OnPointerPressed: ");
      }

      protected override void OnKeyUp(KeyRoutedEventArgs e) {
         Debug.WriteLine("> OnKeyUp: virtKey=" + e.Key);
         base.OnKeyUp(e);
         Debug.WriteLine("< OnKeyUp: ");
      }

      private async void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs e) {
         Debug.WriteLine("< OnKeyUp_CoreWindow: virtKey="+e.VirtualKey);
         e.Handled = true;
         switch (e.VirtualKey) {
            case VirtualKey.F2:
               await MosaicField.GameNew();
               break;
            case VirtualKey.Number1:
               await SetGame(ESkillLevel.eBeginner);
               break;
            case VirtualKey.Number2:
               await SetGame(ESkillLevel.eAmateur);
               break;
            case VirtualKey.Number3:
               await SetGame(ESkillLevel.eProfi);
               break;
            case VirtualKey.Number4:
               await SetGame(ESkillLevel.eCrazy);
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
               e.Handled = false;
               break;
         }
         Debug.WriteLine("> OnKeyUp_CoreWindow: ");
      }

      private void OnClickBttnBack(object sender, RoutedEventArgs e) {
         GoBack();
      }
      private async void OnClickBttnNewGame(object sender, RoutedEventArgs e) {
         await MosaicField.GameNew();
      }

      private async void OnClickBttnSkillBeginner(object sender, RoutedEventArgs e) {
         await SetGame(ESkillLevel.eBeginner);
      }
      private async void OnClickBttnSkillAmateur(object sender, RoutedEventArgs e) {
         await SetGame(ESkillLevel.eAmateur);
      }
      private async void OnClickBttnSkillProfi(object sender, RoutedEventArgs e) {
         await SetGame(ESkillLevel.eProfi);
      }
      private async void OnClickBttnSkillCrazy(object sender, RoutedEventArgs e) {
         await SetGame(ESkillLevel.eCrazy);
      }

      /// <summary> Перепроверить Margin поля мозаики так, что бы при нём поле мозаки было в пределах страницы </summary>
      private Thickness CheckMosaicMargin(Thickness? newMargin = null, Size? sizeWinMosaic = null) {
         var margin = newMargin.HasValue ? newMargin.Value : MosaicField.Container.Margin;
         if (!sizeWinMosaic.HasValue)
            sizeWinMosaic = MosaicField.WindowSize;
         var sizePage = Window.Current.Bounds.ToFmRect().toSize();

         if ((margin.Left + sizeWinMosaic.Value.width) < MinIndent) {
            // правый край мозаики пересёк левую сторону страницы/экрана
            margin.Left = MinIndent - sizeWinMosaic.Value.width; // привязываю к левой стороне страницы/экрана
         } else
            if (margin.Left > (sizePage.width - MinIndent)) {
               // левый край мозаики пересёк правую сторону страницы/экрана
               margin.Left = sizePage.width - MinIndent; // привязываю к правой стороне страницы/экрана
            }
         if ((margin.Top + sizeWinMosaic.Value.height) < MinIndent) {
            // нижний край мозаики пересёк верхнюю сторону страницы/экрана
            margin.Top = MinIndent - sizeWinMosaic.Value.height; // привязываю к верхней стороне страницы/экрана
         } else
            if (margin.Top > (sizePage.height - MinIndent)) {
               // вержний край мозаики пересёк нижнюю сторону страницы/экрана
               margin.Top = sizePage.height - MinIndent; // привязываю к нижней стороне страницы/экрана
            }

         return margin;
      }
   }
}