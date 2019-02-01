
package fmg.jfx.img;

import fmg.core.img.LogoModel;

/** Mine image on the playing field */
public final class Mine {

    /** Mine image controller implementation for {@link Logo.Canvas} */
    public static class ControllerCanvas extends Logo.ControllerCanvas {
        public ControllerCanvas() { LogoModel.toMineModel(getModel()); }
    }

    /** Mine image controller implementation for {@link Logo.Image} */
    public static class ControllerImage extends Logo.ControllerImage {
        public ControllerImage() { LogoModel.toMineModel(getModel()); }
    }

}
