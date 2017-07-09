using System;
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

      public CustomSkillPage() {
         this.InitializeComponent();
         MosaicData = new MosaicInitData();

         HSV hsv = new HSV(StaticImgConsts.DefaultForegroundColor);
         hsv.s = 80;
         hsv.v = 70;
         hsv.a = 170;
         BorderColorStartBttn = new SolidColorBrush(hsv.ToColor().ToWinColor());

         Action run = () => {
            //if (gridMosaics.SelectedItem == null)
            //   return;
            hsv.h += 10;
            BorderColorStartBttn.Color = hsv.ToColor().ToWinColor();
         };
         run.RepeatNoWait(TimeSpan.FromMilliseconds(100), () => _closed);
         this.Unloaded += (s, e) => _closed = true;
      }

      public MosaicInitData MosaicData { get; private set; }
      public SolidColorBrush BorderColorStartBttn;
      private bool _closed;

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
      }

      private void OnSliderValueChangedSizeFieldHeight(object sender, Windows.UI.Xaml.Controls.Primitives.RangeBaseValueChangedEventArgs ev) {
         MosaicData.SizeField = new Matrisize(MosaicData.SizeField.m, Convert.ToInt32(ev.NewValue));
      }

   }

}
