using System.ComponentModel;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;

namespace Fmg.Uwp.Mosaic.Wbmp {

    /// <summary> MVC: controller. UWP <see cref="WriteableBitmap"/> implementation over control <see cref="Image"/> </summary>
    public class MosaicImageController : MosaicFrameworkElementController<Image, WriteableBitmap, MosaicImageView> {

        public MosaicImageController()
            : base(new MosaicImageView())
        { }

        protected override void OnPropertyViewChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyViewChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(View.Image):
                Control.Source = View.InnerImage;
                break;
            }
        }

        public override Image Control => View.Control;

        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }

    }

}
