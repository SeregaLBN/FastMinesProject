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

   }

}
