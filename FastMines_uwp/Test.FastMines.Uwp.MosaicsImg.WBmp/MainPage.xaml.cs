using Windows.UI.Xaml.Controls;
using fmg.common;
using fmg.data.controller.types;
using fmg.uwp.res.img;

namespace Test.FastMines.Uwp.MosaicsImg.WBmp {

   public sealed partial class MainPage : Page {
      public MainPage() {
         InitializeComponent();
         var hsv = new HSV(SkillImg.BackgroundColor);
         SkillImg.PropertyChanged += (o, ev) => {
            switch (ev.PropertyName) {
            case "Image":
               //hsv.h = SkillImg.RotateAngle;
               //SkillImg.BackgroundColor = hsv.toColor();
               break;
            }
         };
      }

      public MosaicsSkillImg SkillImg => new MosaicsSkillImg(ESkillLevel.eCrazy, 200) {
         Rotate = true,
         RedrawInterval = 50,
         RotateAngleDelta = 5
      };

   }

}
