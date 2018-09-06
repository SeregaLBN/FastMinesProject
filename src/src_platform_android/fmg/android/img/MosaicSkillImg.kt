package fmg.android.img

import java.util.stream.Stream

import fmg.common.Color
import fmg.common.Pair
import fmg.common.geom.PointDouble
import fmg.core.img.MosaicSkillController
import fmg.core.img.MosaicSkillModel
import fmg.core.types.ESkillLevel

/**
 * Representable [ESkillLevel] as image
 * <br></br>
 * Android impl
 *
 * @param TImage Android specific image: [android.graphics.Bitmap]
 */
internal abstract class MosaicSkillImg<TImage>
    /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
    protected constructor(skill: ESkillLevel?) : MosaicSkillOrGroupView<TImage, MosaicSkillModel>(MosaicSkillModel(skill))
{

    override val coords: Stream<Pair<Color, Stream<PointDouble>>>
        get() = model.coords

    override fun close() {
        model.close()
        super.close()
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** MosaicsSkill image view implementation over [android.graphics.Bitmap]  */
    class Bitmap(skill: ESkillLevel?) : MosaicSkillImg<android.graphics.Bitmap>(skill) {

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

    /** MosaicsSkill image controller implementation for [Bitmap]  */
    class ControllerBitmap(skill: ESkillLevel?) : MosaicSkillController<android.graphics.Bitmap, Bitmap>(skill == null, MosaicSkillImg.Bitmap(skill)) {

        override fun close() {
            view.close()
            super.close()
        }

    }

}
