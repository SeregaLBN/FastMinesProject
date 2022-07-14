package fmg.android.img;

import fmg.core.img.LogoModel;

/** Mine image on the playing field */
@Deprecated
public final class Mine {
    private Mine() {}

    /** Mine image controller implementation for {@link Logo.BitmapView} */
    public static class BitmapController extends Logo.BitmapController {
        public BitmapController() { LogoModel.toMineModel(getModel()); }
    }

}
