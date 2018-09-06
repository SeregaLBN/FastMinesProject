package fmg.android.mosaic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import java.util.function.Consumer

import fmg.android.utils.*
import fmg.common.geom.RectDouble
import fmg.common.geom.RegionDouble
import fmg.common.geom.SizeDouble
import fmg.core.mosaic.MosaicDrawModel
import fmg.core.mosaic.MosaicView
import fmg.core.mosaic.cells.BaseCell
import fmg.core.types.EClose
import fmg.core.types.EOpen
import fmg.core.types.EState
import fmg.android.utils.StaticInitializer

/** MVC: view. Abstract android implementation
 * @param TImage platform specific view/image/picture or other display context/canvas/window/panel
 * @param TImageInner image type of flag/mine into mosaic field
 * @param TMosaicModel mosaic data model
 */
abstract class MosaicAndroidView<TImage, TImageInner: Any, TMosaicModel : MosaicDrawModel<TImageInner>>
        protected constructor(mosaicModel: TMosaicModel)
    : MosaicView<TImage, TImageInner, TMosaicModel>(mosaicModel)
{

    private val _textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected var _alreadyPainted = false

    init {
        _textPaint.style = Paint.Style.FILL
    }


    protected fun drawAndroid(g: Canvas, modifiedCells: Collection<BaseCell>?, clipRegion: RectDouble?, drawBk: Boolean) {
        assert(!_alreadyPainted)
        _alreadyPainted = true

        val model = model

        // save
        g.save()

        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStroke.style = Paint.Style.STROKE
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL

        // 1. background color
        val bkClr = model.backgroundColor
        if (drawBk) {
            if (!bkClr.isOpaque)
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            if (!bkClr.isTransparent) {
                paintFill.color = bkClr.toColor()
                if (clipRegion == null)
                    g.drawColor(bkClr.toColor())//, PorterDuff.Mode.CLEAR);
                else
                    g.drawRect(clipRegion.x.toFloat(), clipRegion.y.toFloat(), clipRegion.width.toFloat(), clipRegion.height.toFloat(), paintFill)
            }
        }

        // 2. paint cells
        val pen = model.penBorder
        paintStroke.strokeWidth = pen.width.toFloat()
        val padding = model.padding
        val margin = model.margin
        val offset = SizeDouble(margin.left + padding.left,
                margin.top + padding.top)
        val isIconicMode = pen.colorLight == pen.colorShadow
        val bkFill = model.backgroundFill

        val redrawAll = modifiedCells == null || modifiedCells.isEmpty() || modifiedCells.size >= model.matrix.size
        val recheckAll = clipRegion != null // check to redraw all mosaic cells
        val toCheck = if (redrawAll || recheckAll) model.matrix else modifiedCells

        if (MosaicView._DEBUG_DRAW_FLOW) {
            println("> MosaicAndroidView.draw: " + (if (redrawAll) "all" else "cnt=" + modifiedCells!!.size)
                    + "; clipReg=" + clipRegion
                    + "; drawBk=" + drawBk)
        }
        var tmp = 0

        for (cell in toCheck!!) {
            // redraw only when needed...
            if (redrawAll ||
                    modifiedCells != null && modifiedCells.contains(cell) || // ..when the cell is explicitly specified

                    clipRegion != null && cell.rcOuter.moveXY(offset.width, offset.height).intersection(clipRegion))
            // ...when the cells and update region intersect
            {
                ++tmp
                val rcInner = cell.getRcInner(pen.width)
                val poly = RegionDouble.moveXY(cell.region, offset).toPolygon()

                //if (!isIconicMode)
                run {
                    g.save()
                    // ограничиваю рисование только границами своей фигуры
                    g.clipPath(poly)
                }

                run {
                    // 2.1. paint component


                    // 2.1.1. paint cell background
                    //if (!isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
                    run {
                        val bkClrCell = cell.getBackgroundFillColor(bkFill.mode,
                                bkClr,
                                bkFill.colors)
                        if (!drawBk || bkClrCell != bkClr) {
                            paintFill.color = bkClrCell.toColor()
                            g.drawPath(poly, paintFill)
                        }
                    }

                    //g.setColor(java.awt.Color.MAGENTA);
                    //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);

                    val paintImage = Consumer<TImageInner> { img ->
                        if (img is android.graphics.Bitmap) {
                            val x = (rcInner.x + offset.width ).toFloat()
                            val y = (rcInner.y + offset.height).toFloat()
                            g.drawBitmap(img as android.graphics.Bitmap, x, y, null)
                        } else {
                            throw RuntimeException("Unsupported image type " + img.javaClass.simpleName)
                        }
                    }

                    // 2.1.2. output pictures
                    if (model.imgFlag != null &&
                            cell.state.status == EState._Close &&
                            cell.state.close == EClose._Flag) {
                        paintImage.accept(model.imgFlag)
                    } else if (model.imgMine != null &&
                            cell.state.status == EState._Open &&
                            cell.state.open == EOpen._Mine) {
                        paintImage.accept(model.imgMine)
                    } else
                    // 2.1.3. output text
                    {
                        val szCaption: String?
                        if (cell.state.status == EState._Close) {
                            _textPaint.color = model.colorText.getColorClose(cell.state.close.ordinal).toColor()
                            szCaption = cell.state.close.toCaption()
                            //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                            //szCaption = ""+cell.getDirection(); // debug
                        } else {
                            _textPaint.color = model.colorText.getColorOpen(cell.state.open.ordinal).toColor()
                            szCaption = cell.state.open.toCaption()
                        }
                        if (szCaption != null && szCaption.isNotEmpty()) {
                            rcInner.moveXY(offset.width, offset.height)
                            if (cell.state.isDown)
                                rcInner.moveXY(1.0, 1.0)
                            drawText(g, szCaption, rcInner)
                            //{ // test
                            //   java.awt.Color clrOld = g.getColor(); // test
                            //   g.setColor(java.awt.Color.red);
                            //   g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
                            //   g.setColor(clrOld);
                            //}
                        }
                    }

                }

                // 2.2. paint border
                run {
                    // draw border lines
                    val down = cell.state.isDown || cell.state.status == EState._Open
                    paintStroke.color = (if (down)
                        pen.colorLight
                    else
                        pen.colorShadow).toColor()
                    if (isIconicMode) {
                        g.drawPath(poly, paintStroke)
                    } else {
                        val s = cell.shiftPointBorderIndex
                        val v = cell.attr.getVertexNumber(cell.direction)
                        for (i in 0 until v) {
                            val p1 = cell.region.getPoint(i)
                            val p2 = if (i != v - 1)
                                cell.region.getPoint(i + 1)
                            else
                                cell.region.getPoint(0)
                            if (i == s)
                                paintStroke.color = (if (down)
                                    pen.colorShadow
                                else
                                    pen.colorLight).toColor()
                            g.drawLine((p1.x + offset.width ).toFloat(),
                                       (p1.y + offset.height).toFloat(),
                                       (p2.x + offset.width ).toFloat(),
                                       (p2.y + offset.height).toFloat(), paintStroke)
                        }
                    }

                    // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
                    //g.setColor(java.awt.Color.MAGENTA);
                    //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
                }

                //if (!isIconicMode)
                run { g.restore() }
            }
        }

        /** /
         * // test
         * {
         * g.setClip(oldShape);
         * //g.setComposite(AlphaComposite.SrcOver);
         *
         * // test padding
         * g.setStroke(new BasicStroke(5));
         * Color clr = Color.DarkRed.clone();
         * clr.setA(120);
         * g.setColor(Cast.toColor(clr));
         * g.drawRect((int)padding.left,
         * (int)padding.top,
         * (int)(size.width  - padding.getLeftAndRight()),
         * (int)(size.height - padding.getTopAndBottom()));
         *
         * // test margin
         * g.setStroke(new BasicStroke(3));
         * clr = Color.DarkGreen.clone();
         * clr.setA(120);
         * g.setColor(Cast.toColor(clr));
         * g.drawRect((int)(padding.left + margin.left),
         * (int)(padding.top  + margin.top),
         * (int)(size.width  - padding.getLeftAndRight() - margin.getLeftAndRight()),
         * (int)(size.height - padding.getTopAndBottom() - margin.getTopAndBottom()));
         * }
         * / */

        if (MosaicView._DEBUG_DRAW_FLOW) {
            println("< MosaicAndroidView.draw: cnt=$tmp")
            println("-------------------------------")
        }

        // restore
        g.restore()

        _alreadyPainted = false
    }

    private fun getStringBounds(text: String): RectDouble {
        val r: RectDouble
        if (true) {
            val r2 = Rect()
            _textPaint.getTextBounds(text, 0, text.length, r2)
            r = RectDouble(r2.left.toDouble(), r2.top.toDouble(), r2.width().toDouble(), r2.height().toDouble())
        } else {
            val textWidth = _textPaint.measureText(text)
            r = RectDouble(textWidth.toDouble(), model.fontInfo.size)
        }
        return r
    }

    private fun drawText(g: Canvas, text: String?, rc: RectDouble) {
        if (text == null || text.trim { it <= ' ' }.isEmpty())
            return
        val bnd = getStringBounds(text)
        //      { // test
        //         java.awt.Color clrOld = g.getColor();
        //         g.setColor(java.awt.Color.BLUE);
        //         g.fillRect((int)rc.x, (int)rc.y, (int)rc.width, (int)rc.height);
        //         g.setColor(clrOld);
        //      }
        g.drawText(text, (rc.x + (rc.width - bnd.width) / 2.0).toFloat(),
                (rc.bottom() - (rc.height - bnd.height) / 2.0).toFloat(), _textPaint)
    }

    override fun onPropertyModelChanged(oldValue: Any?, newValue: Any?, propertyName: String) {
        super.onPropertyModelChanged(oldValue, newValue, propertyName)
        if (MosaicDrawModel.PROPERTY_FONT_INFO == propertyName) {
            val fi = model.fontInfo
            var tf: Typeface
            tf = try {
                Typeface.create(fi.name, if (fi.isBold) Typeface.BOLD else Typeface.NORMAL)
            } catch (ex: Throwable) {
                ex.printStackTrace(System.err)
                Typeface.create(Typeface.DEFAULT, if (fi.isBold) Typeface.BOLD else Typeface.NORMAL)
            }

            _textPaint.typeface = tf
            _textPaint.textSize = fi.size.toFloat()
        }
    }

    companion object {


        init {
            StaticInitializer.init()
        }
    }

}
