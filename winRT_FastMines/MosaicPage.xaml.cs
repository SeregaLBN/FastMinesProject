using System;
using System.Diagnostics;
using Windows.System;
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
            MosaicField.setParams(new Size(10, 10), EMosaic.eMosaicRhombus1, 3);
            MosaicField.Area = 1500;
            MosaicField.Repaint();
         }
      }

      //private static Windows.Foundation.IAsyncAction ExecuteOnUIThread(Windows.UI.Core.DispatchedHandler action, CoreDispatcherPriority priority) {
      //   return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
      //}

      //private static Windows.Foundation.IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal, bool bAwait = false) {
      //   return bAwait
      //      ? ThreadPool.RunAsync(async delegate { await ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority)
      //      : ThreadPool.RunAsync(delegate { ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority);
      //}

      protected override void OnNavigatedTo(NavigationEventArgs e) {
         base.OnNavigatedTo(e);

         var initParam = e.Parameter as MosaicPageInitParam;
         System.Diagnostics.Debug.Assert(initParam != null);
         MosaicField.setParams(initParam.SizeField, initParam.MosaicTypes, initParam.MinesCount);

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
         Size sizeFld;
         if (skill == ESkillLevel.eCustom) {
            //System.out.println("... dialog box 'Select custom skill level...' ");
            //getCustomSkillDialog().setVisible(!getCustomSkillDialog().isVisible());
            return;
         } else {
            numberMines = skill.GetNumberMines(MosaicField.MosaicType);
            sizeFld = skill.DefaultSize();
         }

         MosaicField.setParams(sizeFld, MosaicField.MosaicType, numberMines);

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
      void AreaInc(double zoomPower = 1.3) {
         Area = (int)(Area * 1.01 * zoomPower);
      }

      /// <summary> Zoom - </summary>
      void AreaDec(double zoomPower = 1.3) {
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
         MosaicField.Area = CalcOptimalArea(MosaicField.SizeField);
      }

      private void Mosaic_OnClick(Mosaic source, bool leftClick, bool down) { }
      private void Mosaic_OnChangedGameStatus(Mosaic source, EGameStatus oldValue) {}
      private void Mosaic_OnChangedCounters(Mosaic source) {}
      private void Mosaic_OnChangedArea(Mosaic source, int oldArea) {
         //ChangeSizeImagesMineFlag();

         var sizeWinMosaic = MosaicField.WindowSize;
         var sizePage = Window.Current.Bounds;
         var m = MosaicField.Container.Margin;
         m.Left = (sizePage.Width - sizeWinMosaic.width) / 2;
         m.Top = (sizePage.Height - sizeWinMosaic.height) / 2;
         MosaicField.Container.Margin = m;

         //MosaicField.Container.Margin = new Thickness();
      }
      private void Mosaic_OnChangedMosaicType(Mosaic source, EMosaic oldMosaic) {
         (source as MosaicExt).ChangeFontSize();
         //ChangeSizeImagesMineFlag();
      }

      private static double? _baseWheelDelta;
      protected override void OnPointerWheelChanged(PointerRoutedEventArgs e) {
         //Debug.WriteLine("virt_OnPointerWheelChanged");
         var wheelDelta = e.GetCurrentPoint(this).Properties.MouseWheelDelta;
         if (!_baseWheelDelta.HasValue)
            _baseWheelDelta = Math.Abs(wheelDelta);

         var wheelPower = 1 + ((Math.Abs(wheelDelta)/_baseWheelDelta.Value) - 1)/10;

         if (wheelDelta > 0)
            AreaInc(wheelPower);
         else
            AreaDec(wheelPower);

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
               AreaInc(ev.Delta.Scale);
            else
               AreaDec(2 + ev.Delta.Scale);
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

            const int minIndent = 30;
            var sizeWinMosaic = MosaicField.WindowSize;
            var sizePage = Window.Current.Bounds.ToFmRect().toSize();
            if ((margin.Left + sizeWinMosaic.width + deltaTrans.X) < minIndent) {
               // правый край мозаики пересёк левую сторону страницы/экрана
               if (ev.IsInertial)
                  _turnX = !_turnX; // разворачиавю по оси X
               else
                  margin.Left = minIndent - sizeWinMosaic.width; // привязываю к левой стороне страницы/экрана
               applyDelta = ev.IsInertial;
            } else
            if ((margin.Left + deltaTrans.X) > (sizePage.width - minIndent)) {
               // левый край мозаики пересёк правую сторону страницы/экрана
               if (ev.IsInertial)
                  _turnX = !_turnX; // разворачиавю по оси X
               else
                  margin.Left = sizePage.width - minIndent; // привязываю к правой стороне страницы/экрана
               applyDelta = ev.IsInertial;
            }
            if ((margin.Top + sizeWinMosaic.height + deltaTrans.Y) < minIndent) {
               // нижний край мозаики пересёк верхнюю сторону страницы/экрана
               if (ev.IsInertial)
                  _turnY = !_turnY; // разворачиавю по оси Y
               else
                  margin.Top = minIndent - sizeWinMosaic.height; // привязываю к верхней стороне страницы/экрана
               applyDelta = ev.IsInertial;
            } else
            if ((margin.Top + deltaTrans.Y) > (sizePage.height - minIndent)) {
               // вержний край мозаики пересёк нижнюю сторону страницы/экрана
               if (ev.IsInertial)
                  _turnY = !_turnY; // разворачиавю по оси Y
               else
                  margin.Top = sizePage.height - minIndent; // привязываю к нижней стороне страницы/экрана
               applyDelta = ev.IsInertial;
            }
#endregion
            if (applyDelta) {
               margin.Left += deltaTrans.X;
               margin.Top += deltaTrans.Y;
            }
            MosaicField.Container.Margin = margin;
         }
         base.OnManipulationDelta(ev);
         Debug.WriteLine("< OnManipulationDelta");
      }

      protected override void OnManipulationCompleted(ManipulationCompletedRoutedEventArgs e) {
         var pnt1 = this.TransformToVisual(MosaicField.Container).TransformPoint(e.Position);
         //var pnt2 = ContentRoot.TransformToVisual(Mosaic.Container).TransformPoint(e.Position);
         //var content = Window.Current.Content;
         Debug.WriteLine("> OnManipulationCompleted: Pos=[{0} / {1}]; Container=[{2}]; Cumulative.Translation=[{3}]", e.Position, pnt1, e.Container.GetType(), e.Cumulative.Translation);
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

      protected override async void OnKeyUp(KeyRoutedEventArgs e)
      {
         Debug.WriteLine("> vrtOnKeyUp: ");
         base.OnKeyUp(e);
         Debug.WriteLine("< vrtOnKeyUp: ");
      }
      private async void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs e) {
         Debug.WriteLine("< OnKeyUp_CoreWindow: ");
         switch (e.VirtualKey) {
            case VirtualKey.F2:
               e.Handled = true;
               await MosaicField.GameNew();
               break;
            case VirtualKey.Number1:
               SetGame(ESkillLevel.eBeginner);
               e.Handled = true;
               break;
            case VirtualKey.Number2:
               SetGame(ESkillLevel.eAmateur);
               e.Handled = true;
               break;
            case VirtualKey.Number3:
               SetGame(ESkillLevel.eProfi);
               e.Handled = true;
               break;
            case VirtualKey.Number4:
               SetGame(ESkillLevel.eCrazy);
               e.Handled = true;
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

      private void OnClickBttnSkillBeginner(object sender, RoutedEventArgs e) {
         SetGame(ESkillLevel.eBeginner);
      }
      private void OnClickBttnSkillAmateur(object sender, RoutedEventArgs e) {
         SetGame(ESkillLevel.eAmateur);
      }
      private void OnClickBttnSkillProfi(object sender, RoutedEventArgs e) {
         SetGame(ESkillLevel.eProfi);
      }
      private void OnClickBttnSkillCrazy(object sender, RoutedEventArgs e) {
         SetGame(ESkillLevel.eCrazy);
      }
   }
}