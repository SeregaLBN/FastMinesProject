package fmg.android.img

import android.graphics.Bitmap
import android.graphics.Canvas

import fmg.common.geom.SizeDouble

/** Internal class-wrapper for pair [Bitmap] and [Canvas]  */
internal class BmpCanvas : AutoCloseable {

    private var _bmp: Bitmap? = null
    private var _canvas: Canvas? = null

    val canvas: Canvas
        get() {
            if (_canvas == null && _bmp != null)
                _canvas = Canvas(_bmp!!)
            return _canvas!!
        }

    fun createImage(size: SizeDouble): Bitmap {
//        if (_bmp == null)
            _bmp = android.graphics.Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
//        else
//            _bmp!!.reconfigure(size.width.toInt(), size.height.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
        _canvas = null
        return _bmp!!
    }

    override fun close() {
        if (_bmp == null)
            return
        _bmp!!.recycle()
        _bmp = null
        _canvas = null
    }

}
