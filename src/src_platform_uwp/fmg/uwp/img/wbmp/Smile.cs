using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.img.wbmp {

    /// <summary> Smile image over UWP <see cref="WriteableBitmap"/> </summary>
    public static class Smile {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Smile image view UWP implementation over <see cref="WriteableBitmap"/> </summary>
        public class WBmpView : ImageView<WriteableBitmap, SmileModel> {

            private WriteableBitmap _bmp;

            public WBmpView(SmileModel.EFaceType faceType)
                : base(new SmileModel(faceType))
            { }

            protected override WriteableBitmap CreateImage() {
                var s = Model.Size;
                _bmp = BitmapFactory.New((int)s.Width, (int)s.Height);
                return _bmp;
            }

            protected override void DrawBody() {
                double w = Size.Width;
                double h = Size.Height;

                void fillEllipse(double x, double y, double w1, double h1, Color fillColor) {
                    _bmp.FillEllipse((int)(x*w), (int)(y*h), (int)((x + w1)*w), (int)((y + h1)*h), fillColor.ToWinColor());
                }

                // рисую затемненный круг
                Color yellowBorder = new Color(0xFF, 0x6C, 0x0A);
                fillEllipse(0, 0, 1, 1, yellowBorder);

                // глаза
                var clr = Color.Black;
                fillEllipse(0.270, 0.170, 0.150, 0.300, clr);
                fillEllipse(0.580, 0.170, 0.150, 0.300, clr);

                // @TODO:  not implemented...
            }

            protected override void Disposing() {
                Model.Dispose();
                base.Disposing();
            }

        }

        /// <summary> Smile image controller implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : ImageController<WriteableBitmap, WBmpView, SmileModel> {

            public WBmpController(SmileModel.EFaceType faceType)
                : base(new WBmpView(faceType)) { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
