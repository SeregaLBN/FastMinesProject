package fmg.android.img

import fmg.core.img.LogoModel

/** Mine image on the playing field  */
class Mine {

    /** Mine image controller implementation for [Logo.Bitmap] */
    class ControllerBitmap : Logo.ControllerBitmap() {
        init {
            LogoModel.toMineModel(model)
        }
    }

}
