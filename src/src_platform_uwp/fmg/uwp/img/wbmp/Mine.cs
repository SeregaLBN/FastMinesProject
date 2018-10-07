using fmg.core.img;

namespace fmg.uwp.img.wbmp {

    /// <summary> Mine image on the playing field </summary>
    public sealed  class Mine {

        /// <summary> Mine image controller implementation for <see cref="Logo.Canvas"/> </summary>
        public class Controller : Logo.Controller {
            public Controller() { LogoModel.ToMineModel(Model); }
        }

    }

}
