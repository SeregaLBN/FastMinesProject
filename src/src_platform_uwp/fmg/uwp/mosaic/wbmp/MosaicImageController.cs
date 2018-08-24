using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;

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

   }

}
