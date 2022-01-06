using Fmg.Core.Img;

namespace Fmg.Uwp.Img.Wbmp {

    /// <summary> Mine image on the playing field </summary>
    public static class Mine {

        /// <summary> Mine image controller implementation for <see cref="Logo.WBmpView"/> </summary>
        public class WBmpController : Logo.WBmpController {
            public WBmpController() { LogoModel.ToMineModel(Model); }
        }

    }

}
