using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media.Imaging;

namespace fmg.uwp.mosaic.wbmp {

    /// <summary> MVC: controller. UWP <see cref="WriteableBitmap"/> implementation over control <see cref="Image"/> </summary>
    public class MosaicImageController : MosaicFrameworkElementController<Image, WriteableBitmap, MosaicImageView> {

        public MosaicImageController()
            : base(new MosaicImageView())
        { }

        public override Image Control => View.Control;

        protected override void SetBinding() {
            base.SetBinding();
            var control = Control;
            control.SetBinding(Image.SourceProperty, new Binding {
                Source = View,
                Path = new PropertyPath(nameof(View.Image)),
                Mode = BindingMode.OneWay,
                Converter = new InnerImageConverter(View)
            });
        }

        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }

        internal class InnerImageConverter : IValueConverter {
            private readonly MosaicImageView _owner;

            public InnerImageConverter(MosaicImageView owner) { _owner = owner; }

            public object Convert(object value, Type targetType, object parameter, string language) {
                //LoggerSimple.Put("  InnerImageConverter: return InnerImage");
                return _owner.InnerImage;
            }

            public object ConvertBack(object value, Type targetType, object parameter, string language) {
                throw new NotImplementedException();
            }
        }

    }

}
