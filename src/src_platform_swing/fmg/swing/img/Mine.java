package fmg.swing.img;

import fmg.core.img.LogoModel;

/** Mine image on the playing field */
public final class Mine {
    private Mine() {}

    /** Mine image controller implementation for {@link Logo.IconView} */
    public static class IconController extends Logo.IconController {
        public IconController() { LogoModel.toMineModel(getModel()); }
    }

    /** Mine image controller implementation for {@link Logo.ImageAwtView} */
    public static class ImageAwtController extends Logo.ImageAwtController {
        public ImageAwtController() { LogoModel.toMineModel(getModel()); }
    }

}
