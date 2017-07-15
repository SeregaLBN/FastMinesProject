using System;
using System.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg {

   public sealed partial class CustomSkillPage : Page {

      public MosaicInitData MosaicData { get; private set; }
      public SolidColorBrush BorderColorStartBttn;
      private bool _closed;


      public CustomSkillPage() {
         this.InitializeComponent();
         MosaicData = new MosaicInitData();

         this.Loaded   += OnPageLoaded;
         this.Unloaded += OnPageUnloaded;
      }

      private void OnPageLoaded(object sender, RoutedEventArgs e) {
         this.Loaded -= OnPageLoaded;
         var maxSizeField = CalcMaxMosaicSize(MosaicInitData.AREA_MINIMUM);
         SliderWidth .Maximum = maxSizeField.m;
         SliderHeight.Maximum = maxSizeField.n;

         MosaicData.PropertyChanged += OnMosaicDataPropertyChanged;

         {
            HSV hsv = new HSV(StaticImgConsts.DefaultForegroundColor) {
               s = 80,
               v = 70,
               a = 170
            };
            BorderColorStartBttn = new SolidColorBrush(hsv.ToColor().ToWinColor());

            Action run = () => {
               //if (gridMosaics.SelectedItem == null)
               //   return;
               hsv.h += 10;
               BorderColorStartBttn.Color = hsv.ToColor().ToWinColor();
            };
            run.RepeatNoWait(TimeSpan.FromMilliseconds(100), () => _closed);
         }
      }

      private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
         this.Loaded -= OnPageUnloaded;
         MosaicData.PropertyChanged -= OnMosaicDataPropertyChanged;
         _closed = true;
      }

      private void StartNewGame() {
         //Frame frame = this.Frame;
         Frame frame = Window.Current.Content as Frame;
         System.Diagnostics.Debug.Assert(frame != null);

         frame.Navigate(typeof(MosaicPage2), MosaicData);

         //Window.Current.Content = new MosaicPage();
         //// Ensure the current window is active
         //Window.Current.Activate();
      }

      private void OnClickBttnStartGame(object sender, RoutedEventArgs ev) {
         LoggerSimple.Put("OnClickBttnStartGame");
         StartNewGame();
      }

      private void OnSliderValueChangedSizeFieldWidth(object sender, Windows.UI.Xaml.Controls.Primitives.RangeBaseValueChangedEventArgs ev) {
         MosaicData.SizeField = new Matrisize(Convert.ToInt32(ev.NewValue), MosaicData.SizeField.n);
         ChangeSlideMinesMax();
      }

      private void OnSliderValueChangedSizeFieldHeight(object sender, Windows.UI.Xaml.Controls.Primitives.RangeBaseValueChangedEventArgs ev) {
         MosaicData.SizeField = new Matrisize(MosaicData.SizeField.m, Convert.ToInt32(ev.NewValue));
         ChangeSlideMinesMax();
      }

      private void OnMosaicDataPropertyChanged(object sender, System.ComponentModel.PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case nameof(MosaicData.MosaicType):
            ChangeSlideMinesMax();
            break;
         }
      }

      private void ChangeSlideMinesMax() {
         int max = MosaicData.SizeField.m * MosaicData.SizeField.n - GetNeighborNumber();
         SliderMines.Maximum = max;
         if (SliderMines.Value > max)
            SliderMines.Value = max;

         //radioGroup.clearSelection();
      }

      private int GetNeighborNumber() {
         var attr = MosaicHelper.CreateAttributeInstance(MosaicData.MosaicType);
         int max = Enumerable.Range(0, attr.GetDirectionCount())
               .Select(i => attr.getNeighborNumber(i))
               .Max();
         return max + 1; // +thisCell
      }

      /// <summary> узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <returns>max размер поля мозаики</returns>
      public Matrisize CalcMaxMosaicSize(double area) {
         var sizeMosaic = CalcMosaicWindowSize(ScreenResolutionHelper.GetDesktopSize());
         return MosaicHelper.FindSizeByArea(MosaicData.MosaicType, area, sizeMosaic);
      }
      /// <summary> узнать размер окна мозаики при указанном размере окна проекта </summary>
      SizeDouble CalcMosaicWindowSize(Size sizeMainWindow) {
         var mosaicMargin = GetMosaicMargin();
         SizeDouble res = new SizeDouble(
               sizeMainWindow.Width - mosaicMargin.LeftAndRight,
               sizeMainWindow.Height - mosaicMargin.TopAndBottom);
         if (res.Height < 0 || res.Width < 0)
            throw new Exception("Bad algorithm... :(");
         return res;
      }
      /// <summary> get margin around mosaic control </summary>
      Bound GetMosaicMargin() {
         // @TODO: not implemented...
         return new Bound();
      }


   }

}
