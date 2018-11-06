using System;
using System.Collections.Generic;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.img.win2d {

    /// <summary> Representable <see cref="EMosaicGroup"/> as image. Win2D implementation </summary>
    public static class MosaicGroupImg {

        /// <summary> Representable <see cref="EMosaicGroup"/> as image: common implementation part </summary>
        /// <typeparam name="TImage">Win2D specific image: <see cref="CanvasBitmap"/> or <see cref="CanvasImageSource"/></typeparam>
        public abstract class MosaicGroupImgView<TImage> : MosaicSkillOrGroupView<TImage, MosaicGroupModel>
            where TImage : DependencyObject, ICanvasResourceCreator
        {
            /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
            protected MosaicGroupImgView(EMosaicGroup? group, ICanvasResourceCreator resourceCreator)
                : base(new MosaicGroupModel(group), resourceCreator)
            { }

            protected override IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords { get { return Model.Coords; } }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Representable <see cref="EMosaicGroup"/> as image over <see cref="CanvasBitmap"/> </summary>
        public class CanvasBmp : MosaicGroupImgView<CanvasBitmap> {

            public CanvasBmp(EMosaicGroup? group, ICanvasResourceCreator resourceCreator)
                : base(group, resourceCreator)
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

        /// <summary> Representable <see cref="EMosaicGroup"/> as image over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
        public class CanvasImgSrc : MosaicGroupImgView<CanvasImageSource> {

            public CanvasImgSrc(EMosaicGroup? group, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
                : base(group, resourceCreator)
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

        /// <summary> Representable <see cref="EMosaicGroup"/> as image for <see cref="MosaicGroupImg.CanvasBmp"/> </summary>
        public class ControllerBitmap : MosaicGroupController<CanvasBitmap, MosaicGroupImg.CanvasBmp> {

            public ControllerBitmap(EMosaicGroup? group, ICanvasResourceCreator resourceCreator)
                : base(group == null, new MosaicGroupImg.CanvasBmp(group, resourceCreator))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

        /// <summary> Representable <see cref="EMosaicGroup"/> as image for <see cref="MosaicGroupImg.CanvasImgSrc"/> </summary>
        public class ControllerImgSrc : MosaicGroupController<CanvasImageSource, MosaicGroupImg.CanvasImgSrc> {

            public ControllerImgSrc(EMosaicGroup? group, ICanvasResourceCreator resourceCreator)
                : base(group == null, new MosaicGroupImg.CanvasImgSrc(group, resourceCreator))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
