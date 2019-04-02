using Microsoft.Graphics.Canvas;
using fmg.core.img;

namespace fmg.uwp.img.win2d {

    /// <summary> Mine image on the playing field. Win2D implementation </summary>
    public static class Mine {

        /// <summary> Mine image controller implementation for <see cref="Logo.CanvasBmpView"/> </summary>
        public class CanvasBmpController : Logo.CanvasBmpController {
            public CanvasBmpController(ICanvasResourceCreator resourceCreator)
                : base(resourceCreator)
            {
                LogoModel.ToMineModel(Model);
            }
        }

        /// <summary> Mine image controller implementation for <see cref="Logo.CanvasImgSrcView"/> </summary>
        public class CanvasImgSrcController : Logo.CanvasImgSrcController {
            public CanvasImgSrcController(ICanvasResourceCreator resourceCreator)
                : base(resourceCreator)
            {
                LogoModel.ToMineModel(Model);
            }
        }

    }

}
