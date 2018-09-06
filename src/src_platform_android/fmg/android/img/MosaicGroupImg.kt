package fmg.android.img

import java.util.stream.Stream

import fmg.common.Color
import fmg.common.Pair
import fmg.common.geom.PointDouble
import fmg.core.img.MosaicGroupController
import fmg.core.img.MosaicGroupModel
import fmg.core.types.EMosaicGroup

/**
 * Representable [EMosaicGroup] as image
 * <br></br>
 * Android impl
 *
 * @param TImage Android specific image: [android.graphics.Bitmap])
 */
internal abstract class MosaicGroupImg<TImage>
    /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
    protected constructor(group: EMosaicGroup?) : MosaicSkillOrGroupView<TImage, MosaicGroupModel>(MosaicGroupModel(group))
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

    /** MosaicsGroup image view implementation over [android.graphics.Bitmap]  */
    internal class Bitmap(group: EMosaicGroup?) : MosaicGroupImg<android.graphics.Bitmap>(group) {

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

    /** MosaicsGroup image controller implementation for [Bitmap]  */
    class ControllerBitmap(group: EMosaicGroup?) : MosaicGroupController<android.graphics.Bitmap, Bitmap>(group == null, MosaicGroupImg.Bitmap(group)) {

        override fun close() {
            view.close()
            super.close()
        }

    }

}
