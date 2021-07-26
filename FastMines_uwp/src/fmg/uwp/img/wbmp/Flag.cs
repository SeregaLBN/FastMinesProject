using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Uwp.Utils;
using Fmg.Core.Img;

namespace Fmg.Uwp.Img.Wbmp {

    /// <summary> Flag image UWP implementation over <see cref="WriteableBitmap"/> </summary>
    public static class Flag {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Flag image view UWP implementation over <see cref="WriteableBitmap"/> </summary>
        public class WBmpView : ImageView<WriteableBitmap, FlagModel> {

            private WriteableBitmap _bmp;

            public WBmpView()
                : base(new FlagModel())
            { }

            protected override WriteableBitmap CreateImage() {
                var s = Model.Size;
                _bmp = BitmapFactory.New((int)s.Width, (int)s.Height);
                return _bmp;
            }

            protected override void DrawBody() {
                var bmp = _bmp;
                double w = Size.Width  / 100.0;
                double h = Size.Height / 100.0;

                // perimeter figure points
                var p = new[] {
                    new PointDouble(13.5  * w, 90 * h),
                    new PointDouble(17.44 * w, 51 * h),
                    new PointDouble(21.00 * w, 16 * h),
                    new PointDouble(85.00 * w, 15 * h),
                    new PointDouble(81.45 * w, 50 * h)
                };

                bmp.DrawLineAa((int) p[0].X, (int) p[0].Y, (int) p[1].X, (int) p[1].Y, Color.Black.ToWinColor());

                const float tension = 1f; // 0..1
                var clrCurve = Color.Red.ToWinColor();
                bmp.DrawCurve(new[] {
                        p[2],
                        new PointDouble(56.2 * w, 13.3 * h), // new PointDouble(95.0 * w,  0 * h),
                        new PointDouble(54.8 * w, 18.7 * h), // new PointDouble(19.3 * w, 32 * h),
                        p[3]
                    }.PointsAsXyxyxySequence(false).ToArray(), tension, clrCurve);
                bmp.DrawCurve(new[] {
                        p[3],
                        new PointDouble(81.8 * w, 25.0 * h), // new PointDouble(77.80 * w, 32.89 * h),
                        new PointDouble(83.3 * w, 33.5 * h), // new PointDouble(88.05 * w, 22.73 * h),
                        p[4]
                    }.PointsAsXyxyxySequence(false).ToArray(), tension, clrCurve);
                bmp.DrawCurve(new[] {
                        p[4],
                        new PointDouble(51.3 * w, 53.9 * h), // new PointDouble(15.83 * w, 67 * h),
                        new PointDouble(52.7 * w, 48.5 * h), // new PointDouble(91.45 * w, 35 * h),
                        p[1]
                    }.PointsAsXyxyxySequence(false).ToArray(), tension, clrCurve);

                bmp.DrawLineAa((int) p[1].X, (int) p[1].Y, (int) p[2].X, (int) p[2].Y, clrCurve);

            }

            protected override void Disposing() {
                base.Disposing();
                Model.Dispose();
            }

        }

        /// <summary> Flag image controller UWP implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : ImageController<WriteableBitmap, WBmpView, FlagModel> {

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
