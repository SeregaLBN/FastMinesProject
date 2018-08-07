using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.core.mosaic;
using fmg.core.types;

namespace fmg.uwp.mosaic.wbmp {

   /// <summary> MVC: controller. UWP <see cref="WriteableBitmap"/> implementation over control <see cref="Image"/> </summary>
   public class MosaicImageController : MosaicFrameworkElementController<Image, WriteableBitmap, MosaicImageView> {

      public MosaicImageController()
         : base(new MosaicImageView())
      { }

      protected override void Disposing() {
         base.Disposing();
         View.Dispose();
      }

      ////////////// TEST //////////////
      public static MosaicImageController GetTestData() {
         MosaicView<Image, WriteableBitmap, MosaicDrawModel<WriteableBitmap>>._DEBUG_DRAW_FLOW = true;
         MosaicImageController ctrllr = new MosaicImageController();

         if (ThreadLocalRandom.Current.Next(2) == 1) {
            // unmodified controller test
         } else {
             EMosaic mosaicType = EMosaic.eMosaicTrSq1;
             ESkillLevel skill  = ESkillLevel.eBeginner;

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
