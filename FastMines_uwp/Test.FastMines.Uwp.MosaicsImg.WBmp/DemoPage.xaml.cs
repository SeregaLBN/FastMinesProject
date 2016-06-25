using System;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.res.img;

namespace Test.FastMines.Uwp.MosaicsImg.WBmp {

   public sealed partial class DemoPage : Page {

      public DemoPage() {
         InitializeComponent();

         ModifyBk(DemoImg1);
         ModifyBk(DemoImg2);
         ModifyBk(DemoImg3);
      }

      private static readonly Random Rnd = new Random(Guid.NewGuid().GetHashCode());
      private static int R(int max) => Rnd.Next(max);
      private static bool Bl => (R(2) == 1); // random bool
      private static int Np => (Bl ? -1 : +1); // negative or positive

      private static void ModifyBk<T>(StaticImg<T, WriteableBitmap> demoImg) {
         var hsv = new HSV(demoImg.BackgroundColor) {a = (byte)(170 + R(10)) };
         demoImg.PropertyChanged += (o, ev) => {
            switch (ev.PropertyName) {
            case "RotateAngle":
               hsv.h = demoImg.RotateAngle;
               demoImg.BackgroundColor = hsv.ToColor();
               break;
            }
         };
      }

      public MosaicsGroupImg DemoImg1 { get; } = new MosaicsGroupImg(EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)]) {
         SizeInt = 175 + R(50),
         Rotate = true,
         RedrawInterval = 30 + R(40),
         RotateAngleDelta = (3 + R(4)) * Np,
         PolarLights = true,
         //OnlySyncDraw = true
      };
      public MosaicsSkillImg DemoImg2 { get; } = new MosaicsSkillImg(ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)]) {
         SizeInt = 175 + R(50),
         Rotate = true,
         RedrawInterval = 30 + R(40),
         RotateAngleDelta = (3 + R(4)) * Np,
         //OnlySyncDraw = true
      };
      public fmg.uwp.res.img.MosaicsImg DemoImg3 { get; } = new fmg.uwp.res.img.MosaicsImg(EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3))) {
         SizeInt = 175 + R(50),
         RotateMode = Bl ? fmg.uwp.res.img.MosaicsImg.ERotateMode.SomeCells : fmg.uwp.res.img.MosaicsImg.ERotateMode.FullMatrix,
         Rotate = true,
         RedrawInterval = 30 + R(40),
         RotateAngleDelta = (3 + R(4)) * Np,
         //OnlySyncDraw = true
      };

   }

}
