using System.Collections.Generic;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.mosaic.win2d;

namespace fmg.uwp.img.win2d {

    /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image.
    /// <br/>
    /// Win2D implementation
    /// </summary>
    public static class MosaicImg {

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image. Base view Win2D implementation </summary>
        /// <typeparam name="TImage">Win2D specific image: <see cref="CanvasBitmap"/> or <see cref="CanvasImageSource"/></typeparam>
        public abstract class Win2DView<TImage> : MosaicWin2DView<TImage, Nothing, MosaicAnimatedModel<Nothing>>
            where TImage : DependencyObject, ICanvasResourceCreator
        {
            protected readonly ICanvasResourceCreator _rc;
            protected CanvasDrawingSession _ds;
            protected bool _useBackgroundColor = true;

            protected Win2DView(ICanvasResourceCreator resourceCreator)
                : base(new MosaicAnimatedModel<Nothing>())
            {
                _rc = resourceCreator;
            }

            protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
                DrawWin2D(_ds, modifiedCells, _useBackgroundColor);
            }

            protected override void DrawBody() {
                //base.DrawBody(); // !hide base implementation

                MosaicAnimatedModel<Nothing> model = Model;

                _useBackgroundColor = true;
                switch (model.RotateMode) {
                case EMosaicRotateMode.fullMatrix:
                    DrawModified(model.Matrix);
                    break;
                case EMosaicRotateMode.someCells:
                    // draw static part
                    DrawModified(model.GetNotRotatedCells());

                    // draw rotated part
                    _useBackgroundColor = false;
                    model.GetRotatedCells(rotatedCells => DrawModified(rotatedCells));
                    break;
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

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image over <see cref="CanvasBitmap"/> </summary>
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
                    _useBackgroundColor = true;
                    _ds = ds;
                    base.DrawBody();
                    _ds = null;
                }
            }

        }

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
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
                    _useBackgroundColor = true;
                    _ds = ds;
                    base.DrawBody();
                    _ds = null;
                }
            }

        }

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image for <see cref="MosaicImg.CanvasBmpView"/> </summary>
        public class CanvasBmpController : MosaicImageController<CanvasBitmap, MosaicImg.CanvasBmpView> {

            public CanvasBmpController(ICanvasResourceCreator resourceCreator)
                : base(new MosaicImg.CanvasBmpView(resourceCreator))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image for <see cref="MosaicImg.CanvasImgSrcView"/> </summary>
        public class CanvasImgSrcController : MosaicImageController<CanvasImageSource, MosaicImg.CanvasImgSrcView> {

            public CanvasImgSrcController(ICanvasResourceCreator resourceCreator)
                : base(new MosaicImg.CanvasImgSrcView(resourceCreator))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
