package fmg.android.utils

import android.graphics.Bitmap

/** вспомогательный класс для преобразований картинок  */
//object ImgUtils {

    /** change size Bitmap  */
    fun Bitmap.zoom(newWidth: Int, newHeight: Int): Bitmap? {
        return if (this == null) null else Bitmap.createScaledBitmap(this, newWidth, newHeight, false)
    }

//}
