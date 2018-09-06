package fmg.android.img

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import java.util.stream.Collectors
import java.util.stream.Stream

import fmg.common.Color
import fmg.common.Pair
import fmg.common.geom.PointDouble
import fmg.core.img.AnimatedImageModel
import fmg.core.img.MosaicGroupModel
import fmg.core.img.MosaicSkillModel
import fmg.core.img.WithBurgerMenuView
import fmg.android.utils.*
import fmg.android.utils.StaticInitializer

/**
 * MVC: view. Abstract Android representable [fmg.core.types.ESkillLevel] or [fmg.core.types.EMosaicGroup] as image
 * @param TImage platform specific view/image/picture or other display context/canvas/window/panel
 * @param TImageModel [MosaicSkillModel] or [MosaicGroupModel]
 */
internal abstract class MosaicSkillOrGroupView<TImage, TImageModel : AnimatedImageModel>
        protected constructor(imageModel: TImageModel)
    : WithBurgerMenuView<TImage, TImageModel>(imageModel)
{

    /** get paint information of drawing basic image model  */
    protected abstract val coords: Stream<Pair<Color, Stream<PointDouble>>>


    protected fun draw(g: Canvas) {
        val m = model

        run {
            // fill background
            val bkClr = m.backgroundColor
            if (!bkClr.isOpaque)
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            if (!bkClr.isTransparent)
                g.drawColor(bkClr.toColor())
        }

        val bw = m.borderWidth.toFloat()
        val needDrawPerimeterBorder = !m.borderColor.isTransparent && bw > 0
        var paintStroke: Paint? = null
        if (needDrawPerimeterBorder) {
            paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
            paintStroke.style = Paint.Style.STROKE
            paintStroke.strokeWidth = bw
            paintStroke.color = m.borderColor.toColor()
        }
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL

        val shapes = coords
        val paintStroke2 = paintStroke
        shapes.forEach { pair ->
            val poly = pair.second.collect(Collectors.toList()).toPolygon()
            if (!pair.first.isTransparent) {
                paintFill.color = pair.first.toColor()
                g.drawPath(poly, paintFill)
            }

            // draw perimeter border
            if (needDrawPerimeterBorder) {
                g.drawPath(poly, paintStroke2!!)
            }
        }

        // draw burger menu
        burgerMenuModel.coords
                .forEach { li ->
                    paintFill.strokeWidth = li.penWidht.toFloat()
                    paintFill.color = li.clr.toColor()
                    g.drawLine(li.from.x.toFloat(), li.from.y.toFloat(), li.to.x.toFloat(), li.to.y.toFloat(), paintFill)
                }
    }

    companion object {

        init {
            StaticInitializer.init()
        }
    }

}
