using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;

namespace fmg.uwp.mosaic.xaml {

    /// <summary> MVC: controller. Xaml shapes implementation </summary>
    public class MosaicXamlController : MosaicFrameworkElementController<Panel, ImageSource, MosaicXamlView> {

        public MosaicXamlController(Panel control = null)
            : base(new MosaicXamlView(control))
        { }

        public override Panel Control => View.Control;

        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }

    }

}
