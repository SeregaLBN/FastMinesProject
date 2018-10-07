using System;
using System.Numerics;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.img.win2d {

    /// <summary> Flag image. Win2D implementation </summary>
    public static class Flag {

        /// <summary> Flag image. Base view Win2D implementation </summary>
        /// <typeparam name="TImage">Win2D specific image: <see cref="CanvasBitmap"/> or <see cref="CanvasImageSource"/></typeparam>
        public abstract class FlagImageView<TImage> : ImageView<TImage, FlagModel>
            where TImage : DependencyObject, ICanvasResourceCreator
        {

            protected readonly ICanvasResourceCreator _rc;

            protected FlagImageView(ICanvasResourceCreator resourceCreator)
                : base(new FlagModel())
            {
                _rc = resourceCreator;
            }

            static FlagImageView() {
                StaticInitializer.Init();
            }

            protected void Draw(CanvasDrawingSession ds, bool fillBk) {
                if (fillBk)
                    ds.Clear(Windows.UI.Colors.Transparent);

                var w = (float)Size.Width  / 100.0f;
                var h = (float)Size.Height / 100.0f;

                //ds.DrawRectangle(0, 0, Width, Height, Windows.UI.Colors.Red, 1); // test

                var p = new[] {
                    new Vector2(13.50f * w, 90 * h),
                    new Vector2(17.44f * w, 51 * h),
                    new Vector2(21.00f * w, 16 * h),
                    new Vector2(85.00f * w, 15 * h),
                    new Vector2(81.45f * w, 50 * h)
                };

                using (var cssLine = new CanvasStrokeStyle {
                    StartCap = CanvasCapStyle.Flat,
                    EndCap = CanvasCapStyle.Flat
                }) {
                    ds.DrawLine(p[0], p[1], Colors.Black, Math.Max(1, 7*(w+h)/2), cssLine);

                    var clrRed = Colors.Red;
                    using (var cssCurve = new CanvasStrokeStyle {
                        StartCap = CanvasCapStyle.Triangle,
                        EndCap = CanvasCapStyle.Triangle
                    }) {
                        using (var builder = new CanvasPathBuilder(_rc)) {
                            builder.BeginFigure(p[2]);
                            builder.AddCubicBezier(
                                new Vector2(95.0f * w,  0 * h),
                                new Vector2(19.3f * w, 32 * h),
                                p[3]);
                            builder.AddCubicBezier(
                                new Vector2(77.80f * w, 32.89f * h),
                                new Vector2(88.05f * w, 22.73f * h),
                                p[4]);
                            builder.AddCubicBezier(
                                new Vector2(15.83f * w, 67 * h),
                                new Vector2(91.45f * w, 35 * h),
                                p[1]);
                            builder.AddLine(p[2]);
                            builder.EndFigure(CanvasFigureLoop.Closed);

                            ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clrRed, Math.Max(1, 7*(w+h)/2), cssCurve);
                        }
                    }
                    //ds.DrawLine(p[1], p[2], clrRed, 15, cssLine);
                }
            }

            protected override void Disposing() {
               Model.Dispose();
               base.Disposing();
            }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Flag image view implementation over <see cref="CanvasBitmap"/> </summary>
        public class CanvasBmp : FlagImageView<CanvasBitmap> {

            public CanvasBmp(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
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

        /// <summary> Flag image view implementation over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
        public class CanvasImgSrc : FlagImageView<CanvasImageSource> {

            public CanvasImgSrc(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
                : base(resourceCreator)
            { }

            protected override CanvasImageSource CreateImage() {
                var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                var s = Model.Size;
                return new CanvasImageSource(_rc, (float)s.Width, (float)s.Height, dpi);
            }

            protected override void DrawBody() {
                using (var ds = Image.CreateDrawingSession(Colors.Transparent)) {
                    Draw(ds, false);
                }
            }

        }

        /// <summary> Flag image controller implementation for <see cref="Flag.CanvasBmp"/> </summary>
        public class ControllerBitmap : ImageController<CanvasBitmap, Flag.CanvasBmp, FlagModel> {

            public ControllerBitmap(ICanvasResourceCreator resourceCreator)
                : base(new Flag.CanvasBmp(resourceCreator))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

        /// <summary> Flag image controller implementation for <see cref="Flag.CanvasImgSrc"/> </summary>
        public class ControllerImgSrc : ImageController<CanvasImageSource, Flag.CanvasImgSrc, FlagModel> {

            public ControllerImgSrc(ICanvasResourceCreator resourceCreator)
                : base(new Flag.CanvasImgSrc(resourceCreator))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
