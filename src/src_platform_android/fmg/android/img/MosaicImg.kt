package fmg.android.img

import fmg.core.img.MosaicAnimatedModel
import fmg.core.mosaic.MosaicImageController
import fmg.core.mosaic.cells.BaseCell
import fmg.core.types.EMosaic
import fmg.android.mosaic.MosaicAndroidView

/**
 * Representable [EMosaic] as image
 * <br>
 * base Android impl
 *
 * @param TImage Android specific image: [android.graphics.Bitmap]
 */
abstract class MosaicImg<TImage> protected constructor() : MosaicAndroidView<TImage, Void, MosaicAnimatedModel<Void>>(MosaicAnimatedModel()) {

    protected var _useBackgroundColor = true

    override fun drawBody() {
        //super.drawBody(); // !hide super implementation

        val model = model

        _useBackgroundColor = true
        when (model.rotateMode) {
            MosaicAnimatedModel.ERotateMode.fullMatrix -> drawModified(model.matrix)
            MosaicAnimatedModel.ERotateMode.someCells -> {
                // draw static part
                drawModified(model.notRotatedCells)

                // draw rotated part
                _useBackgroundColor = false
                model.getRotatedCells { rotatedCells -> drawModified(rotatedCells) }
            }
        }
    }

    override fun close() {
        model.close()
        super.close()
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Mosaic image view implementation over [android.graphics.Bitmap]  */
    class Bitmap : MosaicImg<android.graphics.Bitmap>() {

        private val wrap = BmpCanvas()

        override fun createImage(): android.graphics.Bitmap {
            return wrap.createImage(model.size)
        }

        override fun drawModified(modifiedCells: Collection<BaseCell>) {
            drawAndroid(wrap.canvas, modifiedCells, null, _useBackgroundColor)
        }

        override fun close() {
            wrap.close()
        }

    }

    /** Mosaic image controller implementation for [Bitmap]  */
    open class ControllerBitmap : MosaicImageController<android.graphics.Bitmap, Bitmap>(MosaicImg.Bitmap()) {

        override fun close() {
            view.close()
            super.close()
        }

    }

}
