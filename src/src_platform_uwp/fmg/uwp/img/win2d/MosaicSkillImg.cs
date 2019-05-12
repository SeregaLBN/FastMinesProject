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

    /// <summary> Representable <see cref="ESkillLevel"/> as image. Win2D implementation </summary>
    public static class MosaicSkillImg {

        /// <summary> Representable <see cref="ESkillLevel"/> as image: common implementation part </summary>
        /// <typeparam name="TImage">Win2D specific image: <see cref="CanvasBitmap"/> or <see cref="CanvasImageSource"/></typeparam>
        public abstract class Win2DView<TImage> : MosaicSkillOrGroupView<TImage, MosaicSkillModel>
            where TImage : DependencyObject, ICanvasResourceCreator
        {
            /// <param name="skill">may be null. if Null - representable image of typeof(ESkillLevel)</param>
            protected Win2DView(ESkillLevel? skill, ICanvasResourceCreator resourceCreator)
                : base(new MosaicSkillModel(skill), resourceCreator)
            { }

            protected override IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords { get { return Model.Coords; } }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Representable <see cref="ESkillLevel"/> as image over <see cref="CanvasBitmap"/> </summary>
        public class CanvasBmpView : Win2DView<CanvasBitmap> {

            public CanvasBmpView(ESkillLevel? skill, ICanvasResourceCreator resourceCreator)
                : base(skill, resourceCreator)
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

        /// <summary> Representable <see cref="ESkillLevel"/> as image over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
        public class CanvasImgSrcView : Win2DView<CanvasImageSource> {

            public CanvasImgSrcView(ESkillLevel? skill, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
                : base(skill, resourceCreator)
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

        /// <summary> Representable <see cref="ESkillLevel"/> as image for <see cref="MosaicSkillImg.CanvasBmpView"/> </summary>
        public class CanvasBmpController : MosaicSkillController<CanvasBitmap, MosaicSkillImg.CanvasBmpView> {

            public CanvasBmpController(ESkillLevel? skill, ICanvasResourceCreator resourceCreator)
                : base(skill == null, new MosaicSkillImg.CanvasBmpView(skill, resourceCreator))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

        /// <summary> Representable <see cref="ESkillLevel"/> as image for <see cref="MosaicSkillImg.CanvasImgSrcView"/> </summary>
        public class CanvasImgSrcController : MosaicSkillController<CanvasImageSource, MosaicSkillImg.CanvasImgSrcView> {

            public CanvasImgSrcController(ESkillLevel? skill, ICanvasResourceCreator resourceCreator)
                : base(skill == null, new MosaicSkillImg.CanvasImgSrcView(skill, resourceCreator))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
