using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.bmp;
using fmg.uwp.res.img;

namespace Test.FastMines.Uwp.MosaicsImg.WBmp {

   public sealed partial class MainPage : Page {

      public MainPage() {
         InitializeComponent();

         var hsv = new HSV(DemoImg.BackgroundColor);
         hsv.a = 180;
         DemoImg.PropertyChanged += (o, ev) => {
            switch (ev.PropertyName) {
            case "RotateAngle":
               hsv.h = DemoImg.RotateAngle;
               DemoImg.BackgroundColor = hsv.toColor();
               break;
            }
         };
      }

      //public MosaicsGroupImg DemoImg { get; } = new MosaicsGroupImg(EMosaicGroup.ePentagons, 200) {
      //   Rotate = true,
      //   RedrawInterval = 50,
      //   RotateAngleDelta = 5,
      //   PolarLights = true,
      //   //OnlySyncDraw = true
      //};
      //public MosaicsSkillImg DemoImg { get; } = new MosaicsSkillImg(ESkillLevel.eCrazy, 200) {
      //   Rotate = true,
      //   RedrawInterval = 50,
      //   RotateAngleDelta = 5,
      //   //OnlySyncDraw = true
      //};
      public fmg.uwp.res.img.MosaicsImg DemoImg { get; } = new fmg.uwp.res.img.MosaicsImg(EMosaic.eMosaicPenrousePeriodic1, new Matrisize(5,6), 200) {
         RotateMode = fmg.uwp.res.img.MosaicsImg.ERotateMode.SomeCells,
         Rotate = true,
         RedrawInterval = 50,
         RotateAngleDelta = 5,
         //OnlySyncDraw = true
      };
   }

}
