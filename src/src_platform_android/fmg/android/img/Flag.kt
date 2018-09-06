package fmg.android.img

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF

import fmg.core.img.FlagModel
import fmg.core.img.ImageController
import fmg.core.img.ImageView
import fmg.android.utils.StaticInitializer

/** Flag image  */
abstract class Flag<TImage> : ImageView<TImage, FlagModel>(FlagModel()) {

    protected fun draw(g: Canvas) {
        val size = size
        val h = (size.height / 100.0).toFloat()
        val w = (size.width / 100.0).toFloat()

        // perimeter figure points
        val p = arrayOf(PointF(13.50f * w, 90 * h), PointF(17.44f * w, 51 * h), PointF(21.00f * w, 16 * h), PointF(85.00f * w, 15 * h), PointF(81.45f * w, 50 * h))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = Math.max(1f, 7 * (w + h) / 2)
        paint.color = Color.BLACK
        g.drawLine(p[0].x, p[0].y, p[1].x, p[1].y, paint)

        paint.color = Color.RED
        val path = Path()
        path.moveTo(p[2].x, p[2].y)
        path.cubicTo(95.0f * w, 0 * h,
                19.3f * w, 32 * h,
                p[3].x, p[3].y)
        path.cubicTo(77.80f * w, 32.89f * h,
                88.05f * w, 22.73f * h,
                p[4].x, p[4].y)
        path.cubicTo(15.83f * w, 67 * h,
                91.45f * w, 35 * h,
                p[1].x, p[1].y)
        path.lineTo(p[2].x, p[2].y)
        path.close()

        g.drawPath(path, paint)
    }

    override fun close() {
        model.close()
        super.close()
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Flag image view implementation over [android.graphics.Bitmap]  */
    class Bitmap : Flag<android.graphics.Bitmap>() {

        private val wrap = BmpCanvas()

        override fun createImage(): android.graphics.Bitmap {
            return wrap.createImage(model.size)
        }

        override fun drawBody() {
            draw(wrap.canvas)
        }

        override fun close() {
            wrap.close()
        }

    }

    /** Flag image controller implementation for [Bitmap]  */
    class ControllerBitmap : ImageController<android.graphics.Bitmap, Flag.Bitmap, FlagModel>(Flag.Bitmap()) {

        override fun close() {
            view.close()
            super.close()
        }

    }

    companion object {

        init {
            StaticInitializer.init()
        }
    }

}
