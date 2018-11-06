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
        public abstract class MosaicImgView<TImage> : MosaicWin2DView<TImage, Nothing, MosaicAnimatedModel<Nothing>>
            where TImage : DependencyObject, ICanvasResourceCreator
        {
            protected readonly ICanvasResourceCreator _rc;
            protected CanvasDrawingSession _ds;
            protected bool _useBackgroundColor = true;

            protected MosaicImgView(ICanvasResourceCreator resourceCreator)
                : base(new MosaicAnimatedModel<Nothing>())
            {
                _rc = resourceCreator;
            }

            protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
                DrawWin2D(_ds, modifiedCells, null, _useBackgroundColor);
            }

            protected override void DrawBody() {
                //base.DrawBody(); // !hide base implementation

                MosaicAnimatedModel<Nothing> model = Model;

                _useBackgroundColor = true;
                switch (model.RotateMode) {
                case MosaicAnimatedModel<Nothing>.ERotateMode.fullMatrix:
                    DrawModified(model.Matrix);
                    break;
                case MosaicAnimatedModel<Nothing>.ERotateMode.someCells:
                    // draw static part
                    DrawModified(model.GetNotRotatedCells());

                    // draw rotated part
                    _useBackgroundColor = false;
                    model.GetRotatedCells(rotatedCells => DrawModified(rotatedCells));
                    break;
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

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image over <see cref="CanvasBitmap"/> </summary>
        public class CanvasBmp : MosaicImgView<CanvasBitmap> {

            public CanvasBmp(ICanvasResourceCreator resourceCreator)
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
        public class CanvasImgSrc : MosaicImgView<CanvasImageSource> {

            public CanvasImgSrc(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
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

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image for <see cref="MosaicImg.CanvasBmp"/> </summary>
        public class ControllerBitmap : MosaicImageController<CanvasBitmap, MosaicImg.CanvasBmp> {

            public ControllerBitmap(ICanvasResourceCreator resourceCreator)
               : base(new MosaicImg.CanvasBmp(resourceCreator))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

        /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image for <see cref="MosaicImg.CanvasImgSrc"/> </summary>
        public class ControllerImgSrc : MosaicImageController<CanvasImageSource, MosaicImg.CanvasImgSrc> {

            public ControllerImgSrc(ICanvasResourceCreator resourceCreator)
               : base(new MosaicImg.CanvasImgSrc(resourceCreator))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
