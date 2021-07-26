using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.Img.Wbmp {

    /// <summary> Main logo image UWP implementation over <see cref="WriteableBitmap"/> </summary>
    public class Logo {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Main logo image. View UWP implementation over <see cref="WriteableBitmap"/> </summary>
        public class WBmpView : ImageView<WriteableBitmap, LogoModel> {

            private WriteableBitmap _bmp;

            public WBmpView()
                : base(new LogoModel())
            { }

            protected override WriteableBitmap CreateImage() {
                var s = Model.Size;
                _bmp = new WriteableBitmap((int)s.Width, (int)s.Height);
                return _bmp;
            }

            protected override void DrawBody() {
                var img = Image;
                LogoModel lm = Model;

                {
                    var bkClr = lm.BackgroundColor;
                    if (!bkClr.IsTransparent)
                        img.Clear(bkClr.ToWinColor());
                }

                IList<PointDouble> rays = lm.Rays;
                IList<PointDouble> inn  = lm.Inn;
                IList<PointDouble> oct  = lm.Oct;

                // paint owner rays
                for (var i = 0; i < 8; i++) {
                    img.FillQuad(
                        (int)rays[i].X, (int)rays[i].Y,
                        (int)oct[i].X, (int)oct[i].Y,
                        (int)inn[i].X, (int)inn[i].Y,
                        (int)oct[(i + 5) % 8].X, (int)oct[(i + 5) % 8].Y,
                        lm.Palette[i].ToColor().Darker().ToWinColor()
                    );
                }

                // paint star perimeter
                for (var i = 0; i < 8; i++) {
                    var p1 = rays[(i + 7) % 8];
                    var p2 = rays[i];
                    // TODO need usage:
                    //var zoomAverage = (ZoomX + ZoomY) / 2;
                    //var penWidth = Model.BorderWidth*zoomAverage;
                    img.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, lm.Palette[i].ToColor().ToWinColor());
                }

                double w = img.PixelWidth;
                double h = img.PixelHeight;
                // paint inner gradient triangles
                for (var i = 0; i < 8; i++) {
                    img.FillTriangle(
                        (int)inn[(i + 0) % 8].X, (int)inn[(i + 0) % 8].Y,
                        (int)inn[(i + 3) % 8].X, (int)inn[(i + 3) % 8].Y,
                        (int)(w / 2), (int)(h / 2),
                        ((i & 1) == 0)
                            ? lm.Palette[(i + 6) % 8].ToColor().Brighter().ToWinColor()
                            : lm.Palette[(i + 6) % 8].ToColor().Darker().ToWinColor());
                }
            }

            protected override void Disposing() {
                base.Disposing();
                Model.Dispose();
            }

        }

        /// <summary> Logo image controller UWP implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : LogoController<WriteableBitmap, WBmpView> {

            public WBmpController()
                : base(new WBmpView())
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
