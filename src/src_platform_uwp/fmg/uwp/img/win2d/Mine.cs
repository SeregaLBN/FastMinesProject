using Microsoft.Graphics.Canvas;
using fmg.core.img;

namespace fmg.uwp.img.win2d {

    /// <summary> Mine image on the playing field. Win2D implementation </summary>
    public sealed class Mine {

        /// <summary> Mine image controller implementation for <see cref="Logo.CanvasBmp"/> </summary>
        public class ControllerBitmap : Logo.ControllerBitmap {
            public ControllerBitmap(ICanvasResourceCreator resourceCreator)
                : base(resourceCreator)
            {
                LogoModel.ToMineModel(Model);
            }
        }

        /// <summary> Mine image controller implementation for <see cref="Logo.CanvasImgSrc"/> </summary>
        public class ControllerImgSrc : Logo.ControllerImgSrc {
            public ControllerImgSrc(ICanvasResourceCreator resourceCreator)
                : base(resourceCreator)
            {
                LogoModel.ToMineModel(Model);
            }
        }

    }

}
