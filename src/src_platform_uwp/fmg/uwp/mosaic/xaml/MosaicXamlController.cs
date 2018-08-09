using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.core.types;

namespace fmg.uwp.mosaic.xaml {

   /// <summary> MVC: controller. Xaml shapes implementation </summary>
   public class MosaicXamlController : MosaicFrameworkElementController<Panel, ImageSource, MosaicXamlView> {

      public MosaicXamlController()
         : base(new MosaicXamlView())
      { }

      protected override void Disposing() {
         base.Disposing();
         View.Dispose();
      }

      ////////////// TEST //////////////
      public static MosaicXamlController GetTestData() {
         MosaicXamlView._DEBUG_DRAW_FLOW = true;
         MosaicXamlController ctrllr = new MosaicXamlController();

         if (ThreadLocalRandom.Current.Next(2) == 1) {
            // unmodified controller test
         } else {
            EMosaic mosaicType = EMosaic.eMosaicTrSq1;
            ESkillLevel skill = ESkillLevel.eBeginner;

            ctrllr.Area = 500;
            ctrllr.MosaicType = mosaicType;
            ctrllr.SizeField = skill.GetDefaultSize();
            ctrllr.MinesCount = skill.GetNumberMines(mosaicType);
            ctrllr.GameNew();
         }
         return ctrllr;
      }
      //////////////////////////////////

   }

}
