package fmg.android.img

import java.util.Arrays

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.Shader

import fmg.android.utils.*
import fmg.common.Color
import fmg.common.HSV
import fmg.core.img.ImageView
import fmg.core.img.LogoController
import fmg.core.img.LogoModel
import fmg.android.utils.StaticInitializer

/** Main logos image - base Logo image view implementation  */
abstract class Logo<TImage> protected constructor() : ImageView<TImage, LogoModel>(LogoModel()) {

    protected fun draw(g: Canvas) {
        val lm = this.model

        run {
            // fill background
            val bkClr = lm.backgroundColor
            if (!bkClr.isOpaque)
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            if (!bkClr.isTransparent)
                g.drawColor(bkClr.toColor())
        }

        val rays0 = lm.rays
        val inn0 = lm.inn
        val oct0 = lm.oct

        val rays = rays0.stream().map { p -> p.toPoint() }.toArray<PointF> { size -> arrayOfNulls(size) }
        val inn = inn0.stream().map { p -> p.toPoint() }.toArray<PointF> { size -> arrayOfNulls(size) }
        val oct = oct0.stream().map { p -> p.toPoint() }.toArray<PointF> { size -> arrayOfNulls(size) }
        val center = PointF((size.width / 2.0).toFloat(), (size.height / 2.0).toFloat())

        val hsvPalette = lm.palette
        val palette = Arrays.stream(hsvPalette)
                .map { hsv -> hsv.toColor() }
                .toArray<Color> { size -> arrayOfNulls(size) }

        // paint owner gradient rays
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0..7) {
            if (!lm.isUseGradient) {
                paint.style = Paint.Style.FILL
                paint.color = hsvPalette[i].toColor().darker().toColor()
                fillPolygon(g, paint, rays[i], oct[i], inn[i], oct[(i + 5) % 8])
            } else {
                // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                // over linear gragients

                paint.shader = makeLinearGradient(rays[i], palette[(i + 1) % 8], inn[i], palette[(i + 6) % 8])
                fillPolygon(g, paint, rays[i], oct[i], inn[i], oct[(i + 5) % 8])

                val p1 = oct[i]
                val p2 = oct[(i + 5) % 8]
                val p = PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2) // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

                lateinit var clr: Color // Color(255,255,255,0); //  Cast.toColor(fmg.common.Color.Transparent);
                if (true) {
                    val c1 = hsvPalette[(i + 1) % 8]
                    val c2 = hsvPalette[(i + 6) % 8]
                    val diff = c1.h - c2.h
                    val cP = HSV(c1.toColor())
                    cP.h += diff / 2 // цвет в точке p (пересечений линий...)
                    cP.a = 0
                    clr = cP.toColor()
                }

                paint.shader = makeLinearGradient(oct[i], palette[(i + 3) % 8], p, clr)
                fillPolygon(g, paint, rays[i], oct[i], inn[i])

                paint.shader = makeLinearGradient(oct[(i + 5) % 8], palette[(i + 0) % 8], p, clr)
                fillPolygon(g, paint, rays[i], oct[(i + 5) % 8], inn[i])
            }
        }

        // paint star perimeter
        val zoomAverage = (lm.zoomX + lm.zoomY) / 2
        val penWidth = lm.borderWidth * zoomAverage
        if (penWidth > 0.1) {
            paint.shader = null // reset gradient shader
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = penWidth.toFloat()
            for (i in 0..7) {
                val p1 = rays[(i + 7) % 8]
                val p2 = rays[i]
                paint.color = palette[i].darker().toColor()
                g.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
            }
        }

        // paint inner gradient triangles
        paint.style = Paint.Style.FILL
        for (i in 0..7) {
            if (lm.isUseGradient) {
                val p1 = inn[(i + 0) % 8]
                val p2 = inn[(i + 3) % 8]
                val p = PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2) // center line of p1-p2
                paint.shader = makeLinearGradient(
                        p, palette[(i + 6) % 8],
                        center, if ((i and 1) == 1) Color.Black() else Color.White())
            } else {
                paint.color = if ((i and 1) == 1)
                    hsvPalette[(i + 6) % 8].toColor().brighter().toColor()
                else
                    hsvPalette[(i + 6) % 8].toColor().darker().toColor()
            }
            fillPolygon(g, paint, inn[(i + 0) % 8], inn[(i + 3) % 8], center)
        }
    }

    override fun close() {
        model.close()
        super.close()
    }

    companion object {

        init {
            StaticInitializer.init()
        }

        private fun fillPolygon(g: Canvas, paint: Paint, vararg p: PointF) {
            val path = Path()
            path.fillType = Path.FillType.EVEN_ODD
            path.moveTo(p[0].x, p[0].y)
            for (i in 1 until p.size)
                path.lineTo(p[i].x, p[i].y)
            path.close()
            g.drawPath(path, paint)
        }

        private fun makeLinearGradient(start: PointF, startClr: Color, end: PointF, endClr: Color): Shader {
            return LinearGradient(start.x, start.y, end.x, end.y, startClr.toColor(), endClr.toColor(), Shader.TileMode.CLAMP)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Logo image view implementation over [android.graphics.Bitmap] */
    class Bitmap : Logo<android.graphics.Bitmap>() {

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

    /** Logo image controller implementation for [Logo.Bitmap] */
    open class ControllerBitmap : LogoController<android.graphics.Bitmap, Logo.Bitmap>(Logo.Bitmap()) {

        override fun close() {
            view.close()
            super.close()
        }

    }

}
