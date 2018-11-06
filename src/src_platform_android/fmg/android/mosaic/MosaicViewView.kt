package fmg.android.mosaic

import java.util.HashSet

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

import fmg.android.utils.*
import fmg.common.geom.SizeDouble
import fmg.core.img.IImageView
import fmg.core.mosaic.MosaicView
import fmg.core.mosaic.MosaicDrawModel
import fmg.core.mosaic.MosaicGameModel
import fmg.core.mosaic.cells.BaseCell
import fmg.android.img.Flag
import fmg.android.img.Mine

/** MVC: view. Android implementation over control [View]  */
class MosaicViewView(private val _owner: Activity) : MosaicAndroidView<View, Bitmap, MosaicDrawModel<Bitmap>>(MosaicDrawModel()) {

    private var _control: View? = null
    private val _imgFlag = Flag.ControllerBitmap()
    private val _imgMine = Mine.ControllerBitmap()
    private val _modifiedCells = HashSet<BaseCell>()

    val control: View
        get() {
            if (_control == null) {
                _control = object : View(_owner) {

                    override fun onDraw(canvas: Canvas) {
                        super.onDraw(canvas)

                        val clipBounds = canvas.clipBounds

                        this@MosaicViewView.drawAndroid(canvas,
                                toDrawCells(clipBounds?.toRectDouble()),
                                true/*_modifiedCells.isEmpty() || (_modifiedCells.size() == getModel().getMatrix().size())*/)
                        _modifiedCells.clear()
                    }

                }
            }
            return _control!!
        }

    init {
        changeSizeImagesMineFlag()
    }

    override fun createImage(): View {
        // will return once created window
        return control
    }

    override fun drawModified(modifiedCells: Collection<BaseCell>?) {
        val control = control

        assert(!_alreadyPainted)

        if ((modifiedCells == null) || // mark NULL if all mosaic is changed
            (android.os.Build.VERSION.SDK_INT >= 21))
        {
            _modifiedCells.clear()
            control.invalidate()
        } else {
            _modifiedCells.addAll(modifiedCells)

            var minX = 0.0
            var minY = 0.0
            var maxX = 0.0
            var maxY = 0.0
            var first = true
            for (cell in modifiedCells) {
                val rc = cell.rcOuter
                if (first) {
                    first = false
                    minX = rc.x
                    minY = rc.y
                    maxX = rc.right()
                    maxY = rc.bottom()
                } else {
                    minX = Math.min(minX, rc.x)
                    minY = Math.min(minY, rc.y)
                    maxX = Math.max(maxX, rc.right())
                    maxY = Math.max(maxY, rc.bottom())
                }
            }
            if (MosaicView._DEBUG_DRAW_FLOW)
                println("MosaicViewAndroid.draw: repaint={" + minX.toInt() + "," + minY.toInt() + "," + (maxX - minX).toInt() + "," + (maxY - minY).toInt() + "}")

            var model = Model;
            var padding = model.Padding;
            var margin = model.Margin;
            var offset = SizeDouble(margin.left + padding.left,
                                    margin.top  + padding.top);
            control.invalidate((minX + offset.width).toInt(), (minY + offset.height).toInt(), (maxX - minX).toInt(), (maxY - minY).toInt())
        }
    }

    override fun onPropertyChanged(oldValue: Any?, newValue: Any?, propertyName: String) {
        super.onPropertyChanged(oldValue, newValue, propertyName)
        when (propertyName) {
            IImageView.PROPERTY_IMAGE -> image // implicit call draw() -> drawBegin() -> drawModified() -> control.repaint() -> View.paintComponent -> drawAndroid()
            IImageView.PROPERTY_SIZE -> {
                val lp = _control!!.layoutParams
                if (lp != null) {
                    var s: SizeDouble? = newValue as SizeDouble?
                    if (s == null)
                        s = model.size
                    lp.width = s!!.width.toInt()
                    lp.height = s.height.toInt()
                }
            }
        }
    }

    override fun onPropertyModelChanged(oldValue: Any?, newValue: Any?, propertyName: String) {
        super.onPropertyModelChanged(oldValue, newValue, propertyName)
        when (propertyName) {
            MosaicGameModel.PROPERTY_MOSAIC_TYPE, MosaicGameModel.PROPERTY_AREA -> changeSizeImagesMineFlag()
        }
    }

    /** переустанавливаю заного размер мины/флага для мозаики  */
    protected fun changeSizeImagesMineFlag() {
        val model = model
        var sq = model.cellAttr.getSq(model.penBorder.width)
        if (sq <= 0) {
            System.err.println("Error: too thick pen! There is no area for displaying the flag/mine image...")
            sq = 3 // ат балды...
        }

        val max = 30
        if (sq > max) {
            _imgFlag.model.setSize(sq.toDouble())
            _imgMine.model.setSize(sq.toDouble())
            model.imgFlag = _imgFlag.image
            model.setImgMine(_imgMine.image)
        } else {
            _imgFlag.model.setSize(max.toDouble())
            model.imgFlag = _imgFlag.image.zoom(sq.toInt(), sq.toInt())
            _imgMine.model.setSize(max.toDouble())
            model.setImgMine(_imgMine.image.zoom(sq.toInt(), sq.toInt()))
        }
    }

    override fun close() {
        model.close()
        super.close()
        _control = null
        _imgFlag.close()
        _imgMine.close()
    }

}
