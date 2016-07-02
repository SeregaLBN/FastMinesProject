using System;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.draw.img.wbmp;

namespace Test.FastMines.Uwp.Images.WBmp {

   public sealed partial class DemoPage : Page {

      public Logo            DemoImg1 { get; } = new Logo();
      public MosaicsSkillImg DemoImg2 { get; } = new MosaicsSkillImg(ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)]);
      public MosaicsGroupImg DemoImg3 { get; } = new MosaicsGroupImg(EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)]);
      public MosaicsImg      DemoImg4 { get; } = new MosaicsImg(EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)));

      private static readonly Random Rnd = new Random(Guid.NewGuid().GetHashCode());
      private static int R(int max) => Rnd.Next(max);
      private static bool Bl => (R(2) == 1); // random bool
      private static int Np => (Bl ? -1 : +1); // negative or positive

      public DemoPage() {
         InitializeComponent();

         ApplyRandom(DemoImg1);
         ApplyRandom(DemoImg2);
         ApplyRandom(DemoImg3);
         ApplyRandom(DemoImg4);
      }

      private static void ApplyRandom<T>(RotatedImg<T, WriteableBitmap> img) {
         img.SizeInt = 175 + R(50);

         img.Rotate = true;
         img.RotateAngleDelta = (3 + R(5)) * Np;
         img.RedrawInterval = 50;
         img.BorderWidth = Bl ? 1 : 2;

         var plrImg = img as PolarLightsImg<T, WriteableBitmap>;
         if (plrImg != null) {
            plrImg.PolarLights = true;
         }

         var logoImg = img as Logo;
         if (logoImg != null) {
            var vals = (Logo.ERotateMode[])Enum.GetValues(typeof(Logo.ERotateMode));
            logoImg.RotateMode = vals[R(vals.Length)];
            logoImg.UseGradient = Bl;
         }

         var mosaicsImg = img as MosaicsImg;
         if (mosaicsImg != null) {
            mosaicsImg.RotateMode = Bl ? MosaicsImg.ERotateMode.FullMatrix : MosaicsImg.ERotateMode.SomeCells;
         }

         if (Bl) {
            // test transparent
            var bkClr = new HSV(ColorExt.RandomColor(Rnd)) { a = (byte)(50 + R(10)) };
            img.PropertyChanged += (o, ev) => {
               if ("RotateAngle" == ev.PropertyName) {
                  bkClr.h = img.RotateAngle;
                  img.BackgroundColor = bkClr.ToColor();
               }
            };
         } else {
            img.BackgroundColor = ColorExt.RandomColor(Rnd).Brighter();
         }
      }

   }

}
