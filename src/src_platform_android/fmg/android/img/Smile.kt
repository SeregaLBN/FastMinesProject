package fmg.android.img

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader

import java.util.function.Consumer

import fmg.core.img.SmileModel
import fmg.core.img.ImageController
import fmg.core.img.ImageView
import fmg.core.img.SmileModel.EFaceType
import fmg.android.utils.StaticInitializer
import fmg.android.utils.toColor

abstract class Smile<TImage> protected constructor(faceType: EFaceType) : ImageView<TImage, SmileModel>(SmileModel(faceType)) {

    protected fun draw(g: Canvas) {
        g.save()

        drawBody(g)
        drawEyes(g)
        drawMouth(g)

        g.restore()
    }

    private fun drawBody(g: Canvas) {
        val size = size

        val sm = this.model
        val type = sm.faceType
        val width  = size.width .toFloat()
        val height = size.height.toFloat()

        if (type == EFaceType.Eyes_OpenDisabled || type == EFaceType.Eyes_ClosedDisabled)
            return

        val yellowBody   = Color.rgb(0xFF, 0xCC, 0x00)
        val yellowGlint  = Color.rgb(0xFF, 0xFF, 0x33)
        val yellowBorder = Color.rgb(0xFF, 0x6C, 0x0A)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL

        run {
            // рисую затемненный круг
            paint.color = yellowBorder
            g.drawOval(0f, 0f, width, height, paint)
        }
        val padX = 0.033f * width
        val padY = 0.033f * height
        val wInt = width - 2 * padX
        val hInt = height - 2 * padY
        val wExt = 1.133f * width
        val hExt = 1.133f * height
        val ellipseInternal = newEllipse(padX, padY, width - padX * 2, height - padY * 2)
        run {
            // поверх него, внутри - градиентный круг
            paint.shader = makeLinearGradient(0f, 0f, yellowBody, width, height, yellowBorder)
            g.drawPath(ellipseInternal, paint)
            paint.setShader(null)
        }
        run {
            // верхний левый блик
            val ellipseExternal = newEllipse(padX, padY, wExt, hExt)
            paint.color = yellowGlint // Color.DARK_GRAY
            g.drawPath(intersectExclude(ellipseInternal, ellipseExternal), paint)

            // test
            //paint.setColor(Color.BLACK);
            //paint.setStyle(Paint.Style.STROKE);
            //g.drawPath(ellipseInternal, paint);
            //g.drawPath(ellipseExternal, paint);
            //paint.setStyle(Paint.Style.FILL);
        }
        run {
            // нижний правый блик
            val ellipseExternal = newEllipse(padX + wInt - wExt, padY + hInt - hExt, wExt, hExt)
            paint.color = yellowBorder.toColor().darker(0.4).toColor()
            g.drawPath(intersectExclude(ellipseInternal, ellipseExternal), paint)

            // test
            //paint.setColor(Color.BLACK);
            //paint.setStyle(Paint.Style.STROKE);
            //g.drawPath(ellipseInternal, paint);
            //g.drawPath(ellipseExternal, paint);
            //paint.setStyle(Paint.Style.FILL);
        }
    }

    private fun drawEyes(g: Canvas) {
        val sm = this.model
        val type = sm.faceType
        val width = sm.size.width.toFloat()
        val height = sm.size.height.toFloat()

        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStroke.style = Paint.Style.STROKE
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL
        when (type) {
            SmileModel.EFaceType.Face_Assistant, SmileModel.EFaceType.Face_SmilingWithSunglasses -> {
                run {
                    // glasses
                    paintStroke.strokeWidth = Math.max(1.0, 0.03 * ((width + height) / 2.0)).toFloat()
                    paintStroke.strokeCap = Paint.Cap.ROUND
                    paintStroke.strokeJoin = Paint.Join.BEVEL
                    paintStroke.color = Color.BLACK
                    g.drawPath(newEllipse(0.200f * width, 0.100f * height, 0.290f * width, 0.440f * height), paintStroke)
                    g.drawPath(newEllipse(0.510f * width, 0.100f * height, 0.290f * width, 0.440f * height), paintStroke)
                    // дужки
                    g.drawLine(     0.746f  * width, 0.148f * height,      0.885f  * width, 0.055f * height, paintStroke)
                    g.drawLine((1 - 0.746f) * width, 0.148f * height, (1 - 0.885f) * width, 0.055f * height, paintStroke)
                    g.drawPath(newArc(      0.864f           * width, 0.047f * height, 0.100f * width, 0.100f * height, 0f, 125f), paintStroke)
                    g.drawPath(newArc((1f - 0.864f - 0.100f) * width, 0.047f * height, 0.100f * width, 0.100f * height, 55f, 125f), paintStroke)
                }
                run {
                    paintFill.color = Color.BLACK
                    g.drawPath(newEllipse(0.270f * width, 0.170f * height, 0.150f * width, 0.300f * height), paintFill)
                    g.drawPath(newEllipse(0.580f * width, 0.170f * height, 0.150f * width, 0.300f * height), paintFill)
                }
            }
            //break; // ! no break
            SmileModel.EFaceType.Face_SavouringDeliciousFood, SmileModel.EFaceType.Face_WhiteSmiling, SmileModel.EFaceType.Face_Grinning -> {
                paintFill.color = Color.BLACK
                g.drawPath(newEllipse(0.270f * width, 0.170f * height, 0.150f * width, 0.300f * height), paintFill)
                g.drawPath(newEllipse(0.580f * width, 0.170f * height, 0.150f * width, 0.300f * height), paintFill)
            }
            SmileModel.EFaceType.Face_Disappointed -> {
                paintStroke.strokeWidth = Math.max(1.0, 0.02 * ((width + height) / 2.0)).toFloat()
                paintStroke.strokeCap = Paint.Cap.ROUND
                paintStroke.strokeJoin = Paint.Join.BEVEL

                val rcHalfLeft = newRectangle(0f, 0f, width / 2.0f, height)
                val rcHalfRght = newRectangle(width / 2.0f, 0f, width, height)

                // глаз/eye
                val areaLeft1 = intersectExclude(newEllipse(0.417f * width, 0.050f * height, 0.384f * width, 0.400f * height), rcHalfLeft)
                val areaRght1 = intersectExclude(newEllipse(0.205f * width, 0.050f * height, 0.384f * width, 0.400f * height), rcHalfRght)
                paintFill.color = Color.RED
                paintStroke.color = Color.BLACK

                g.drawPath(areaLeft1, paintFill)
                g.drawPath(areaRght1, paintFill)
                g.drawPath(areaLeft1, paintStroke)
                g.drawPath(areaRght1, paintStroke)

                // зрачок/pupil
                val areaLeft2 = intersectExclude(newEllipse(0.550f * width, 0.200f * height, 0.172f * width, 0.180f * height), rcHalfLeft)
                val areaRght2 = intersectExclude(newEllipse(0.282f * width, 0.200f * height, 0.172f * width, 0.180f * height), rcHalfRght)
                paintFill.color = Color.BLUE
                paintStroke.color = Color.BLACK

                g.drawPath(areaLeft2, paintFill)
                g.drawPath(areaRght2, paintFill)
                g.drawPath(areaLeft2, paintStroke)
                g.drawPath(areaRght2, paintStroke)

                // веко/eyelid
                var areaLeft3 = intersectExclude(rotate(newEllipse(0.441f * width, -0.236f * height, 0.436f * width, 0.560f * height),
                                                            PointF(0.441f * width, -0.236f * height), 30f), rcHalfLeft)
                var areaRght3 = intersectExclude(rotate(newEllipse(0.128f * width, -0.236f * height, 0.436f * width, 0.560f * height),
                                                            PointF(0.564f * width, -0.236f * height), -30f), rcHalfRght)
                areaLeft3 = intersect(areaLeft1, areaLeft3)
                areaRght3 = intersect(areaRght1, areaRght3)
                paintFill.color = Color.GREEN
                paintStroke.color = Color.BLACK

                g.drawPath(areaLeft3, paintFill)
                g.drawPath(areaRght3, paintFill)
                g.drawPath(areaLeft3, paintStroke)
                g.drawPath(areaRght3, paintStroke)

                // nose
                val nose = newEllipse(0.415f * width, 0.400f * height, 0.170f * width, 0.170f * height)
                paintFill.color = Color.GREEN
                paintStroke.color = Color.BLACK
                g.drawPath(nose, paintFill)
                g.drawPath(nose, paintStroke)
            }
            SmileModel.EFaceType.Eyes_OpenDisabled -> {
                eyeOpened(g, true, true, paintFill)
                eyeOpened(g, false, true, paintFill)
            }
            SmileModel.EFaceType.Eyes_ClosedDisabled -> {
                eyeClosed(g, true, true, paintFill)
                eyeClosed(g, false, true, paintFill)
            }
            SmileModel.EFaceType.Face_EyesOpen -> {
                eyeOpened(g, true, false, paintFill)
                eyeOpened(g, false, false, paintFill)
            }
            SmileModel.EFaceType.Face_WinkingEyeLeft -> {
                eyeClosed(g, true, false, paintFill)
                eyeOpened(g, false, false, paintFill)
            }
            SmileModel.EFaceType.Face_WinkingEyeRight -> {
                eyeOpened(g, true, false, paintFill)
                eyeClosed(g, false, false, paintFill)
            }
            SmileModel.EFaceType.Face_EyesClosed -> {
                eyeClosed(g, true, false, paintFill)
                eyeClosed(g, false, false, paintFill)
            }
            else -> throw UnsupportedOperationException("Not implemented")
        }
    }

    private fun drawMouth(g: Canvas) {
        val sm = this.model
        val type = sm.faceType
        val width = sm.size.width.toFloat()
        val height = sm.size.height.toFloat()

        when (type) {
            SmileModel.EFaceType.Face_Assistant,
            SmileModel.EFaceType.Eyes_OpenDisabled,
            SmileModel.EFaceType.Eyes_ClosedDisabled,
            SmileModel.EFaceType.Face_EyesOpen,
            SmileModel.EFaceType.Face_WinkingEyeLeft,
            SmileModel.EFaceType.Face_WinkingEyeRight,
            SmileModel.EFaceType.Face_EyesClosed        -> return
        }


        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStroke.style = Paint.Style.STROKE
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL
        paintStroke.strokeWidth = Math.max(1.0, 0.044 * ((width + height) / 2.0)).toFloat()
        paintStroke.strokeCap = Paint.Cap.ROUND
        paintStroke.strokeJoin = Paint.Join.BEVEL
        paintStroke.color = Color.BLACK
        paintFill.color = Color.BLACK

        when (type) {
        SmileModel.EFaceType.Face_SavouringDeliciousFood,
        SmileModel.EFaceType.Face_SmilingWithSunglasses,
        SmileModel.EFaceType.Face_WhiteSmiling -> {
                // smile
                val arcSmile = newArc(0.103f * width, -0.133f * height, 0.795f * width, 1.003f * height, 207f, 126f)
                g.drawPath(arcSmile, paintStroke)
                val lip = newEllipse(0.060f * width, 0.475f * height, 0.877f * width, 0.330f * height)
                g.drawPath(intersectExclude(arcSmile, lip), paintFill)

                // test
                //            paintStroke.setColor(Color.GREEN);
                //            g.drawPath(lip, paintStroke);

                // dimples - ямочки на щеках
                //            g.setStroke(strokeNew);
                //            g.setColor(Color.BLACK);
                g.drawPath(newArc(+0.020f * width, 0.420f * height, 0.180f * width, 0.180f * height, (85 + 180).toFloat(), 57f), paintStroke)
                g.drawPath(newArc(+0.800f * width, 0.420f * height, 0.180f * width, 0.180f * height, (38 + 180).toFloat(), 57f), paintStroke)

                // tongue / язык
                if (type == EFaceType.Face_SavouringDeliciousFood) {
                    val tongue = rotate(newEllipse(0.470f * width, 0.406f * height, 0.281f * width, 0.628f * height),
                                            PointF(0.470f * width, 0.406f * height), 40f)
                    paintFill.color = Color.RED
                    val ellipseSmile = newEllipse(0.103f * width, -0.133f * height, 0.795f * width, 1.003f * height)
                    g.drawPath(intersectExclude(tongue, ellipseSmile), paintFill)
                }
            }
            SmileModel.EFaceType.Face_Disappointed -> {
                // smile
                var arcSmile = newArc(0.025f * width, 0.655f * height, 0.950f * width, 0.950f * height, 50f, 80f) // arc as ellipse
                g.drawPath(arcSmile, paintStroke)
                arcSmile = newArc(0.025f * width, 0.655f * height, 0.950f * width, 0.950f * height, 0f, 360f) // arc as ellipse

                // tongue / язык
                var tongue = intersectInclude(newEllipse(0.338f * width, 0.637f * height, 0.325f * width, 0.325f * height), // кончик языка
                                            newRectangle(0.338f * width, 0.594f * height, 0.325f * width, 0.206f * height)) // тело языка
                val hole = intersectExclude(newRectangle(0f, 0f, width, height), arcSmile)
                tongue = intersectExclude(tongue, hole)
                paintFill.color = Color.RED
                g.drawPath(tongue, paintFill)
                paintStroke.color = Color.BLACK
                g.drawPath(tongue, paintStroke)

                //g.drawPath(intersectExclude(newLine(width/2.0f, 0.637f*height, width/2.0f, 0.800f*height), hole), paintStroke); // don't working
                g.drawPath(intersectExclude(newRectangle(width / 2.0f, 0.637f * height, 0.001f, 0.200f * height), hole), paintStroke) // its works

                // test
                //paintStroke.setStrokeWidth(1);
                //paintStroke.setStrokeCap(Paint.Cap.ROUND);
                //paintStroke.setStrokeJoin(Paint.Join.BEVEL);
                //paintStroke.setColor(Color.BLACK);
                //paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
                //g.drawPath(arcSmile, paintStroke);
                //g.drawPath(hole, paintStroke);
            }
            SmileModel.EFaceType.Face_Grinning -> {
                paintFill.shader = makeLinearGradient(0f, 0f, Color.GRAY, width / 2.0f, 0f, Color.WHITE)
                val arcSmile = newArc(0.103f * width, -0.133f * height, 0.795f * width, 1.003f * height, 207f, 126f)
                arcSmile.close()
                g.drawPath(arcSmile, paintFill)
                paintFill.shader = null
                paintStroke.color = Color.BLACK
                g.drawPath(arcSmile, paintStroke)
            }
            else -> throw UnsupportedOperationException("Not implemented")
        }
    }

    private fun eyeOpened(g: Canvas, right: Boolean, disabled: Boolean, paintFill: Paint) {
        val sm = this.model
        val width = sm.size.width.toFloat()
        val height = sm.size.height.toFloat()

        val draw = Consumer<PointF> { offset ->
            val pupil = if (right)
                intersectInclude(intersectInclude(
                               newEllipse((offset.x + 0.273f) * width, (offset.y + 0.166f) * height, 0.180f * width, 0.324f * height),
                        rotate(newEllipse((offset.x + 0.320f) * width, (offset.y + 0.124f) * height, 0.180f * width, 0.273f * height),
                                   PointF((offset.x + 0.320f) * width, (offset.y + 0.124f) * height), 35f)),
                        rotate(newEllipse((offset.x + 0.163f) * width, (offset.y + 0.313f) * height, 0.180f * width, 0.266f * height),
                                   PointF((offset.x + 0.163f) * width, (offset.y + 0.313f) * height), -36f))
            else
                intersectInclude(intersectInclude(
                               newEllipse((offset.x + 0.500f) * width, (offset.y + 0.166f) * height, 0.180f * width, 0.324f * height),
                        rotate(newEllipse((offset.x + 0.486f) * width, (offset.y + 0.227f) * height, 0.180f * width, 0.273f * height),
                                   PointF((offset.x + 0.486f) * width, (offset.y + 0.227f) * height), -35f)),
                        rotate(newEllipse((offset.x + 0.646f) * width, (offset.y + 0.211f) * height, 0.180f * width, 0.266f * height),
                                   PointF((offset.x + 0.646f) * width, (offset.y + 0.211f) * height), 36f))
            if (!disabled) {
                paintFill.color = Color.BLACK
                g.drawPath(pupil, paintFill)
            }
            val hole = rotate(newEllipse((offset.x + if (right) 0.303f else 0.610f) * width, (offset.y + 0.209f) * height, 0.120f * width, 0.160f * height),
                                  PointF((offset.x + if (right) 0.303f else 0.610f) * width, (offset.y + 0.209f) * height), 25f)
            if (!disabled) {
                paintFill.color = Color.WHITE
                g.drawPath(hole, paintFill)
            } else {
                g.drawPath(intersectExclude(pupil, hole), paintFill)
            }
        }
        if (disabled) {
            paintFill.color = Color.WHITE
            draw.accept(PointF(0.034f, 0.027f))
            paintFill.color = Color.GRAY
            draw.accept(PointF())
        } else {
            draw.accept(PointF())
        }
    }

    private fun eyeClosed(g: Canvas, right: Boolean, disabled: Boolean, paintFill: Paint) {
        val sm = this.model
        val width = sm.size.width.toFloat()
        val height = sm.size.height.toFloat()

        val eye = Consumer<Boolean> { isFat ->
            g.drawPath(newEllipse(((if (right) 0.107f else 0.517f) + if (isFat) 0.015f else 0f) * width, 0.248f * height, 0.313f * width, 0.034f * (if (isFat) 2 else 1).toFloat() * height), paintFill)
            g.drawPath(newEllipse(((if (right) 0.230f else 0.640f) + if (isFat) 0.015f else 0f) * width, 0.246f * height, 0.205f * width, 0.065f * (if (isFat) 2 else 1).toFloat() * height), paintFill)
        }
        if (disabled) {
            paintFill.color = Color.WHITE
            eye.accept(true)
        }
        paintFill.color = if (disabled) Color.GRAY else Color.BLACK
        eye.accept(false)
    }

    override fun close() {
        model.close()
        super.close()
    }

    companion object {

        init {
            StaticInitializer.init()
        }

        private fun rotate(shape: Path, rotatePoint: PointF, angle: Float): Path {
            val m = Matrix()
            val res = Path(shape)
            m.postRotate(angle, rotatePoint.x, rotatePoint.y)
            res.transform(m)
            return res
        }

        private fun intersect(s1: Path, s2: Path): Path {
            val res = Path(s1)
            res.op(s2, Path.Op.INTERSECT)
            return res
        }

        private fun intersectExclude(s1: Path, s2: Path): Path {
            val res = Path(s1)
            res.op(s2, Path.Op.DIFFERENCE)
            return res
        }

        private fun intersectInclude(s1: Path, s2: Path): Path {
            val res = Path(s1)
            res.op(s2, Path.Op.UNION)
            return res
        }

        private fun newEllipse(x: Float, y: Float, w: Float, h: Float): Path {
            val p = Path()
            p.addOval(x, y, x + w, y + h, Path.Direction.CW)
            return p
        }

        private fun newLine(startX: Float, startY: Float, endX: Float, endY: Float): Path {
            val p = Path()
            p.moveTo(startX, startY)
            p.lineTo(endX, endY)
            return p
        }

        private fun newRectangle(x: Float, y: Float, w: Float, h: Float): Path {
            val p = Path()
            p.addRect(x, y, x + w, y + h, Path.Direction.CW)
            return p
        }

        private fun newArc(x: Float, y: Float, w: Float, h: Float, start: Float, extent: Float): Path {
            val p = Path()
            p.addArc(x, y, x + w, y + h, 360f - start - extent, extent)
            return p
        }

        private fun makeLinearGradient(startX: Float, startY: Float, startClr: Int, endX: Float, endY: Float, endClr: Int): Shader {
            return LinearGradient(startX, startY, endX, endY, startClr, endClr, Shader.TileMode.CLAMP)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Smile image view implementation over [android.graphics.Bitmap] */
    class Bitmap(faceType: EFaceType) : Smile<android.graphics.Bitmap>(faceType) {

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

    /** Smile image controller implementation for [Smile.Bitmap] */
    class ControllerBitmap(faceType: EFaceType) : ImageController<android.graphics.Bitmap, Smile.Bitmap, SmileModel>(Smile.Bitmap(faceType)) {

        override fun close() {
            view.close()
            super.close()
        }

    }

}
