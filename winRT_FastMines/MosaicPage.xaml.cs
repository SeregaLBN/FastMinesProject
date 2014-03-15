using System;
using System.Collections.Generic;
using System.Diagnostics;
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
// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
using ua.ksn.geom;
using Size = Windows.Foundation.Size;

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

         //this.PointerWheelChanged += MosaicPage_PointerWheelChanged; // see OnPointerWheelChanged
      }

      protected override void OnNavigatedTo(NavigationEventArgs e) {
         base.OnNavigatedTo(e);

         var initParam = e.Parameter as MosaicPageInitParam;
         System.Diagnostics.Debug.Assert(initParam != null);
         var area = 3000; // TODO пересчитать...
         System.Diagnostics.Debug.Assert(_mosaic == null, "Mosaic already initialized 8[ ]");
         Mosaic = new MosaicExt(initParam.SizeField, initParam.MosaicTypes, initParam.MinesCount, area);
         ContentRoot.Children.Add(Mosaic.Container);
         Mosaic.Repaint();

         Mosaic.OnClick += Mosaic_OnClick;
         Mosaic.OnChangeGameStatus += Mosaic_OnChangeGameStatus;
         Mosaic.OnChangeCounters += Mosaic_OnChangeCounters;
         Mosaic.OnChangeArea += Mosaic_OnChangeArea;
         Mosaic.OnChangeMosaicType += Mosaic_OnChangeMosaicType;
      }

      /** узнать размер окна проекта при указанном размере окна мозаики */
      //Size CalcSize(Size sizeMosaicInPixel) {
      //   Dimension sizeWin = this.getSize();

      //   if ((sizeWin.height == 0) && (sizeWin.width == 0) && !this.isVisible()) {
      //      throw new RuntimeException("Invalid method call.  Нельзя высчитать размер окна, когда оно даже не выведено на экран...");
      //      //			Dimension dummy = Toolkit.getDefaultToolkit().getScreenSize(); // заглушка
      //      //			dummy.height++; dummy.width++;
      //      //			return dummy;
      //   }

      //   Dimension currSizeMosaicInPixel = getMosaic().getContainer().getSize();
      //   return new Size(
      //         sizeWin.width + (sizeMosaicInPixel.width - currSizeMosaicInPixel.width),
      //         sizeWin.height + (sizeMosaicInPixel.height - currSizeMosaicInPixel.height));
      //}

      /** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в текущее разрешение экрана
       * @param mosaicSizeField - интересуемый размер поля мозаики
       * @return макс площадь ячейки
       */
      int CalcMaxArea(Size mosaicSizeField) {
         return 7000;

         //Size sizeScreen = Cast.toSize(Toolkit.getDefaultToolkit().getScreenSize());
         //int result = Mosaic.AREA_MINIMUM;
         //result++;
         //Size sizeMosaic = Mosaic.CalcWindowSize(mosaicSizeField, result);
         //Size sizeWnd = CalcSize(sizeMosaic);
         //while ((sizeWnd.width <= sizeScreen.width) &&
         //      (sizeWnd.height <= sizeScreen.height)) {
         //   result++;
         //   sizeMosaic = getMosaic().CalcWindowSize(mosaicSizeField, result);
         //   sizeWnd = CalcSize(sizeMosaic);
         //}
         //result--;
         //return result;
      }

      /**
       * узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана
       * @param area - интересуемая площадь ячеек мозаики
       * @return max размер поля мозаики
       */
      //public Size CalcMaxMosaicSize(int area) {
      //   Size sizeScreen = Cast.toSize(Toolkit.getDefaultToolkit().getScreenSize());
      //   Size result = new Size(1, 1);

      //   result.width++;
      //   Size sizeMosaic = getMosaic().CalcWindowSize(result, area);
      //   Size sizeWnd = CalcSize(sizeMosaic);
      //   while (sizeWnd.width <= sizeScreen.width) {
      //      result.width++;
      //      sizeMosaic = getMosaic().CalcWindowSize(result, area);
      //      sizeWnd = CalcSize(sizeMosaic);
      //   }
      //   result.width--;

      //   result.height++;
      //   sizeMosaic = getMosaic().CalcWindowSize(result, area);
      //   sizeWnd = CalcSize(sizeMosaic);
      //   while (sizeWnd.height <= sizeScreen.height) {
      //      result.height++;
      //      sizeMosaic = getMosaic().CalcWindowSize(result, area);
      //      sizeWnd = CalcSize(sizeMosaic);
      //   }
      //   result.height--;

      //   return result;
      //}

      void setArea(int newArea) {
         newArea = Math.Min(newArea, CalcMaxArea(Mosaic.SizeField.ToWinSize())); // recheck

         int curArea = Mosaic.Area;
         if (curArea == newArea)
            return;

         Mosaic.Area = newArea;
      }

      /** Zoom + */
      void AreaInc() {
         setArea((int)(Mosaic.Area * 1.05));
      }
      /** Zoom - */
      void AreaDec() {
         setArea((int)(Mosaic.Area * 0.95));
      }
      /** Zoom minimum */
      void AreaMin() {
         setArea(0);
      }
      /** Zoom maximum */
      //void AreaMax() {
      //   int maxArea = CalcMaxArea(Mosaic.SizeField.);
      //   if (maxArea == getMosaic().getArea())
      //      return;
      //   setArea(maxArea);

      //   //		{
      //   //			// Если до вызова AreaMax() меню окна распологалось в две строки, то после
      //   //			// отработки этой ф-ции меню будет в одну строку, т.е., последующий вызов
      //   //			// GetMaximalArea() будет возвращать ещё бОльший результат.
      //   //			// Поэтому надо снова установить максимальное значение плошади ячеек.
      //   //			if (maxArea < CalcMaxArea())
      //   //				AreaMax(); // меню было в две  строки
      //   //			else;          // меню было в одну строку
      //   //		}
      //}

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