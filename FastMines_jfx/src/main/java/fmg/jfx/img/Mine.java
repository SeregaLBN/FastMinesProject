
package fmg.jfx.img;

import fmg.core.img.LogoModel;

/** Mine image on the playing field */
@Deprecated
public final class Mine {
    private Mine() {}

    /** Mine image controller implementation for {@link Logo.CanvasView} */
    public static class CanvasController extends Logo.CanvasController {
        public CanvasController() { LogoModel.toMineModel(getModel()); }
    }

    /** Mine image controller implementation for {@link Logo.ImageJfxView} */
    public static class ImageJfxController extends Logo.ImageJfxController {
        public ImageJfxController() { LogoModel.toMineModel(getModel()); }
    }

}
