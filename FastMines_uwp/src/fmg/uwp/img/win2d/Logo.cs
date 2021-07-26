using System.Linq;
using System.Collections.Generic;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Geometry;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Utils.Win2d;
using Fmg.Uwp.Mosaic.Win2d;

namespace Fmg.Uwp.Img.Win2d {

    /// <summary> Main logo image. Win2D implementation </summary>
    public static class Logo {

        /// <summary> Main logo image. Base view Win2D implementation </summary>
        /// <typeparam name="TImage">Win2D specific image: <see cref="CanvasBitmap"/> or <see cref="CanvasImageSource"/></typeparam>
        public abstract class Win2DView<TImage> : ImageView<TImage, LogoModel>
            where TImage : DependencyObject, ICanvasResourceCreator
        {

            protected readonly ICanvasResourceCreator _rc;

            protected Win2DView(ICanvasResourceCreator resourceCreator)
                : base(new LogoModel())
            {
                _rc = resourceCreator;
            }

            protected void Draw(CanvasDrawingSession ds, bool fillBk) {
                ICanvasResourceCreator rc = Image;
                LogoModel lm = Model;

                if (fillBk)
                    ds.Clear(lm.BackgroundColor.ToWinColor());

                IList<PointDouble> rays = lm.Rays;
                IList<PointDouble> inn = lm.Inn;
                IList<PointDouble> oct = lm.Oct;

                var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);

                HSV[] hsvPalette = lm. Palette;
                Windows.UI.Color[] palette = hsvPalette
                    .Select(hsv => hsv.ToWinColor())
                    .ToArray();

                // paint owner rays
                for (var i = 0; i < 8; i++) {
                    using (var geom = rc.BuildLines(rays[i], oct[i], inn[i], oct[(i + 5) % 8])) {
                        if (!lm.UseGradient) {
                            ds.FillGeometry(geom, hsvPalette[i].ToColor().Darker().ToWinColor());
                        } else {
                            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                            // over linear gragients

                            using (var br = rc.CreateGradientPaintBrush(rays[i], palette[(i+1)%8], inn[i], palette[(i+6)%8])) {
                                ds.FillGeometry(geom, br);
                            }

                            var p1 = oct[i];
                            var p2 = oct[(i + 5) % 8];
                            var p = new PointDouble((p1.X + p2.X) / 2, (p1.Y + p2.Y) / 2); // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

                            Windows.UI.Color clr;// = new Color(255,255,255,0); //  Fmg.Common.Color.Transparent.ToWinColor();
                            if (true) {
                                HSV c1 = hsvPalette[(i + 1) % 8];
                                HSV c2 = hsvPalette[(i + 6) % 8];
                                double diff = c1.h - c2.h;
                                HSV cP = new HSV(c1.ToColor());
                                cP.h += diff / 2; // цвет в точке p (пересечений линий...)
                                cP.a = 0;
                                clr = cP.ToColor().ToWinColor();
                            }

                            using (var br = rc.CreateGradientPaintBrush(oct[i], palette[(i + 3) % 8], p, clr)) {
                                using (var geom2 = rc.BuildLines(rays[i], oct[i], inn[i])) {
                                    ds.FillGeometry(geom2, br);
                                }
                            }
                            using (var br = rc.CreateGradientPaintBrush(oct[(i + 5) % 8], palette[(i + 0) % 8], p, clr)) {
                                using (var geom2 = rc.BuildLines(rays[i], oct[(i + 5) % 8], inn[i])) {
                                    ds.FillGeometry(geom2, br);
                                }
                            }
                        }
                    }
                }

                // paint star perimeter
                var zoomAverage = (lm.ZoomX + lm.ZoomY) / 2;
                var penWidth = Model.BorderWidth*zoomAverage;
                if (penWidth > 0.1)
                    using (var css = new CanvasStrokeStyle {
                        StartCap = CanvasCapStyle.Round,
                        EndCap = CanvasCapStyle.Round
                    }) {
                        for (var i = 0; i < 8; i++) {
                            var p1 = rays[(i + 7) % 8];
                            var p2 = rays[i];
                            ds.DrawLine(p1.ToVector2(), p2.ToVector2(), palette[i], (float)penWidth, css);
                        }
                    }

                // paint inner gradient triangles
                for (var i = 0; i < 8; i++) {
                    using (var geom = rc.BuildLines(inn[(i + 0) % 8], inn[(i + 3) % 8], center)) {
                        if (lm.UseGradient) {
                            var p1 = inn[(i + 0) % 8];
                            var p2 = inn[(i + 3) % 8];
                            var p = new PointDouble((p1.X + p2.X) / 2, (p1.Y + p2.Y) / 2); // center line of p1-p2
                            using (var br = rc.CreateGradientPaintBrush(p, palette[(i + 6) % 8], center, ((i & 1) == 1) ? Colors.Black : Colors.White)) {
                                ds.FillGeometry(geom, br);
                            }
                        } else {
                            ds.FillGeometry(geom, ((i & 1) == 1)
                                ? hsvPalette[(i + 6) % 8].ToColor().Brighter().ToWinColor()
                                : hsvPalette[(i + 6) % 8].ToColor().Darker().ToWinColor());
                        }
                    }
                }
            }

            protected override void Disposing() {
                base.Disposing();
                Model.Dispose();
            }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Logo image view implementation over <see cref="CanvasBitmap"/> </summary>
        public class CanvasBmpView : Win2DView<CanvasBitmap> {

            public CanvasBmpView(ICanvasResourceCreator resourceCreator)
                : base(resourceCreator)
            { }

            protected override CanvasBitmap CreateImage() {
                var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                var s = Model.Size;
                return new CanvasRenderTarget(_rc, (float)s.Width, (float)s.Height, dpi);
            }

            protected override void DrawBody() {
                using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
                    Draw(ds, true);
                }
            }

        }

        /// <summary> Logo image view implementation over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
        public class CanvasImgSrcView : Win2DView<CanvasImageSource> {

            public CanvasImgSrcView(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
                : base(resourceCreator)
            { }

            protected override CanvasImageSource CreateImage() {
                var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                var s = Model.Size;
                return new CanvasImageSource(_rc, (float)s.Width, (float)s.Height, dpi);
            }

            protected override void DrawBody() {
                using (var ds = Image.CreateDrawingSession(Model.BackgroundColor.ToWinColor())) {
                    Draw(ds, false);
                }
            }

        }

        /// <summary> Logo image controller implementation for <see cref="Logo.CanvasBmpView"/> </summary>
        public class CanvasBmpController : LogoController<CanvasBitmap, Logo.CanvasBmpView> {

            public CanvasBmpController(ICanvasResourceCreator resourceCreator)
                : base(new Logo.CanvasBmpView(resourceCreator))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

        /// <summary> Logo image controller implementation for <see cref="Logo.CanvasImgSrcView"/> </summary>
        public class CanvasImgSrcController : LogoController<CanvasImageSource, Logo.CanvasImgSrcView> {

            public CanvasImgSrcController(ICanvasResourceCreator resourceCreator)
                : base(new Logo.CanvasImgSrcView(resourceCreator))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
