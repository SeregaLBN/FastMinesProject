using System;
using System.Collections.Generic;
using System.Diagnostics;
using Windows.System.Threading;
using System.IO;
using System.Linq;
using Windows.Foundation.Collections;
using Windows.System;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Shapes;
using ua.ksn;
using ua.ksn.fmg.controller.win_rt;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt;
using ua.ksn.fmg.controller;
using ua.ksn.fmg.controller.types;
using ua.ksn.geom;
using System.Threading.Tasks;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
namespace FastMines {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class MosaicPage : Page {
      private MosaicExt _mosaic;

      public MosaicExt Mosaic {
         get {
            if (_mosaic == null)
               _mosaic = new MosaicExt();
            return _mosaic;
         }
         private set { _mosaic = value; }
      }

      public MosaicPage() {
         this.InitializeComponent();

         this.Loaded += MosaicPage_OnLoaded;
         this.Unloaded += MosaicPage_OnUnloaded;
         this.SizeChanged += MosaicPage_SizeChanged;

         //this.PointerWheelChanged += MosaicPage_PointerWheelChanged; // see OnPointerWheelChanged
      }

      private static Windows.Foundation.IAsyncAction ExecuteOnUIThread(Windows.UI.Core.DispatchedHandler action, CoreDispatcherPriority priority) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
      }

      private static Windows.Foundation.IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal, bool bAwait = false) {
         return bAwait
            ? ThreadPool.RunAsync(async delegate { await ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority)
            : ThreadPool.RunAsync(delegate { ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority);
      }

      protected override void OnNavigatedTo(NavigationEventArgs e) {
         base.OnNavigatedTo(e);

         var initParam = e.Parameter as MosaicPageInitParam;
         System.Diagnostics.Debug.Assert(initParam != null);
         InvokeLater(async () => {
            //await Task.Delay(2);
            System.Diagnostics.Debug.Assert(_mosaic == null, "Mosaic already initialized 8[ ]");
            if (_mosaic != null)
               throw new Exception("Mosaic already initialized 8[ ]");
            Mosaic = new MosaicExt(initParam.SizeField, initParam.MosaicTypes, initParam.MinesCount, 0);
            ContentRoot.Children.Add(Mosaic.Container);
            Mosaic.Repaint();
            Mosaic.Container.SizeChanged += delegate { Mosaic.Area = CalcMaxArea(initParam.SizeField); };
            Mosaic.OnClick += Mosaic_OnClick;
            Mosaic.OnChangeGameStatus += Mosaic_OnChangeGameStatus;
            Mosaic.OnChangeCounters += Mosaic_OnChangeCounters;
            Mosaic.OnChangeArea += Mosaic_OnChangeArea;
            Mosaic.OnChangeMosaicType += Mosaic_OnChangeMosaicType;
         }, CoreDispatcherPriority.Normal, false);
      }

      /// <summary> узнать размер окна проекта при указанном размере окна мозаики </summary>
      Size CalcSize(Size sizeMosaicInPixel) {
         var sizeWin = this.RenderSize.ToFmSize();
         if ((sizeWin.height == 0) && (sizeWin.width == 0) /* && !this.IsVisible*/ ) {
#if DEBUG
            throw new Exception("Invalid method call.  Нельзя высчитать размер окна, когда оно даже не выведено на экран...");
#else
            // in WinRT current page size olways equals screen size
            sizeWin = Window.Current.Bounds.ToFmRect().size();
#endif
         }

         var currSizeMosaicInPixel = Mosaic.Container.RenderSize.ToFmSize();
         if ((currSizeMosaicInPixel.height == 0) && (currSizeMosaicInPixel.width == 0)) {
#if DEBUG
            throw new InvalidOperationException();
#else
            currSizeMosaicInPixel = ContentRoot.RenderSize.ToFmSize();
            if ((currSizeMosaicInPixel.height == 0) && (currSizeMosaicInPixel.width == 0)) {
               currSizeMosaicInPixel.width = sizeWin.width - (int)(ContentRoot.Margin.Left + ContentRoot.Margin.Right);
               currSizeMosaicInPixel.height = sizeWin.height - (int)(ContentRoot.Margin.Top + ContentRoot.Margin.Bottom);
            }
#endif
         }

         return new Size(
               sizeWin.width + (sizeMosaicInPixel.width - currSizeMosaicInPixel.width),
               sizeWin.height + (sizeMosaicInPixel.height - currSizeMosaicInPixel.height));
      }

      /// <summary> Поиск больше-меньше </summary>
      /// <param name="baseMin">стартовое значение для поиска</param>
      /// <param name="baseDelta">начало дельты приращения</param>
      /// <param name="comparable">ф-ция сравнения</param>
      /// <returns>что найдено</returns>
      static int Finder(int baseMin, int baseDelta, Func<int, int> comparable) {
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

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в текущее разрешение экрана </summary>
      /// <param name="mosaicSizeField">интересуемый размер поля мозаики</param>
      /// <returns>макс площадь ячейки</returns>
      private int CalcMaxArea(Size mosaicSizeField) {
         var sizeScreen = Window.Current.Bounds.ToFmRect().size();
         return Finder(ua.ksn.fmg.controller.Mosaic.AREA_MINIMUM, ua.ksn.fmg.controller.Mosaic.AREA_MINIMUM,
            area => {
               var sizeMosaic = Mosaic.CalcWindowSize(mosaicSizeField, area);
               var sizeWnd = CalcSize(sizeMosaic);
               if ((sizeWnd.width == sizeScreen.width) &&
                   (sizeWnd.height == sizeScreen.height))
                  return 0;
               if ((sizeWnd.width <= sizeScreen.width) &&
                   (sizeWnd.height <= sizeScreen.height))
                  return -1;
               return +1;
            });
      }

      /// <summary> узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <returns>max размер поля мозаики</returns>
      public Size CalcMaxMosaicSize(int area) {
         var sizeScreen = Window.Current.Bounds.ToFmRect().size();
         var result = new Size();
         Finder(1, 10, newWidth => {
            result.width = newWidth;
            var sizeMosaic = Mosaic.CalcWindowSize(result, area);
            var sizeWnd = CalcSize(sizeMosaic);
            if (sizeWnd.width == sizeScreen.width)
               return 0;
            if (sizeWnd.width <= sizeScreen.width)
               return -1;
            return +1;
         });
         Finder(1, 10, newHeight => {
            result.height = newHeight;
            var sizeMosaic = Mosaic.CalcWindowSize(result, area);
            var sizeWnd = CalcSize(sizeMosaic);
            if (sizeWnd.width == sizeScreen.height)
               return 0;
            if (sizeWnd.height <= sizeScreen.height)
               return -1;
            return +1;
         });
         return result;
      }

      int Area { get
         {
            return Mosaic.Area;
         }
         set {
            value = Math.Min(value, CalcMaxArea(Mosaic.SizeField)); // recheck

            var curArea = Mosaic.Area;
            if (curArea == value)
               return;

            Mosaic.Area = value;
         }
      }

      /// <summary> Zoom + </summary>
      void AreaInc() {
         Area = (int)(Area * 1.05);
      }

      /// <summary> Zoom - </summary>
      void AreaDec() {
         Area = (int)(Area * 0.95);
      }

      /// <summary> Zoom minimum </summary>
      void AreaMin() {
         Area = 0;
      }

      /// <summary> Zoom maximum </summary>
      void AreaMax() {
         var maxArea = CalcMaxArea(Mosaic.SizeField);
         if (maxArea == Area)
            return;
         Area = maxArea;
      }

      private void OnPointerPressed(CoreWindow sender, PointerEventArgs args) {
         var properties = args.CurrentPoint.Properties;

         // Ignore button chords with the left, right, and middle buttons
         if (properties.IsLeftButtonPressed || properties.IsRightButtonPressed ||
             properties.IsMiddleButtonPressed)
            return;

         // If back or foward are pressed (but not both) navigate appropriately
         var backPressed = properties.IsXButton1Pressed;
         if (backPressed)
            GoBack(args);

      }

      private void GoBack(ICoreWindowEventArgs args) {
         if (this.Frame != null && this.Frame.CanGoBack) {
            args.Handled = true;
            this.Frame.GoBack();
         }
      }

      private void MosaicPage_OnLoaded(object sender, RoutedEventArgs e) {
         Window.Current.CoreWindow.PointerPressed += OnPointerPressed;
      }

      private void MosaicPage_OnUnloaded(object sender, RoutedEventArgs e) {
         Window.Current.CoreWindow.PointerPressed -= OnPointerPressed;
      }

      private void MosaicPage_SizeChanged(object sender, RoutedEventArgs e) {
         var qqq = e;
      }

      private void Mosaic_OnClick(Mosaic source, bool leftClick, bool down) { }
      private void Mosaic_OnChangeGameStatus(Mosaic source, EGameStatus oldValue) {}
      private void Mosaic_OnChangeCounters(Mosaic source) {}
      private void Mosaic_OnChangeArea(Mosaic source, int oldArea) {
         //ChangeSizeImagesMineFlag();
      }
      private void Mosaic_OnChangeMosaicType(Mosaic source, EMosaic oldMosaic) {
         (source as MosaicExt).ChangeFontSize();
         //ChangeSizeImagesMineFlag();
      }

      protected override void OnPointerWheelChanged(PointerRoutedEventArgs e) {
         base.OnPointerWheelChanged(e);
         //e.Handled = true;
         if (e.GetCurrentPoint(this).Properties.MouseWheelDelta > 0)
            AreaInc();
         else
            AreaDec();
      }
   }
}