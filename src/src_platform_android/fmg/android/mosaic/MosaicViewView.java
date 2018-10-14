package fmg.android.mosaic;

import java.util.Collection;
import java.util.HashSet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.android.img.Flag;
import fmg.android.img.Mine;
import fmg.android.utils.Cast;
import fmg.android.utils.ImgUtils;

/** MVC: view. Android implementation over control {@link View} */
public class MosaicViewView extends MosaicAndroidView<View, Bitmap, MosaicDrawModel<Bitmap>> {

    private final Activity _owner;
    private View _control;
    private Flag.ControllerBitmap _imgFlag = new Flag.ControllerBitmap();
    private Mine.ControllerBitmap _imgMine = new Mine.ControllerBitmap();
    private final Collection<BaseCell> _modifiedCells = new HashSet<>();

    public MosaicViewView(Activity owner) {
        super(new MosaicDrawModel<Bitmap>());
        _owner = owner;
        changeSizeImagesMineFlag();
    }

    @Override
    protected View createImage() {
        // will return once created window
        return getControl();
    }

    public View getControl() {
        if (_control == null) {
           _control = new View(_owner/*.getApplicationContext()*/) {

                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);

                    Rect clipBounds = canvas.getClipBounds();

                    MosaicViewView.this.drawAndroid(canvas,
                                                    _modifiedCells.isEmpty()
                                                        ? null
                                                        : _modifiedCells,
                                                    (clipBounds==null)
                                                        ? null
                                                        : Cast.toRectDouble(clipBounds),
                                                    true/*_modifiedCells.isEmpty() || (_modifiedCells.size() == getModel().getMatrix().size())*/);
                    _modifiedCells.clear();
                }

           };
        }
        return _control;
    }

    @Override
    protected void drawModified(Collection<BaseCell> modifiedCells) {
        View control = getControl();

        assert !_alreadyPainted;

        if ((modifiedCells == null) || // mark NULL if all mosaic is changed
            (android.os.Build.VERSION.SDK_INT >= 21))
        {
            _modifiedCells.clear();
            control.invalidate();
        } else {
            _modifiedCells.addAll(modifiedCells);

            double minX=0, minY=0, maxX=0, maxY=0;
            boolean first = true;
            for (BaseCell cell : modifiedCells) {
                RectDouble rc = cell.getRcOuter();
                if (first) {
                    first = false;
                    minX = rc.x;
                    minY = rc.y;
                    maxX = rc.right();
                    maxY = rc.bottom();
                } else {
                    minX = Math.min(minX, rc.x);
                    minY = Math.min(minY, rc.y);
                    maxX = Math.max(maxX, rc.right());
                    maxY = Math.max(maxY, rc.bottom());
                }
            }
            if (_DEBUG_DRAW_FLOW)
                System.out.println("MosaicViewAndroid.draw: repaint={" + (int)minX +","+ (int)minY +","+ (int)(maxX-minX) +","+ (int)(maxY-minY) + "}");
            control.invalidate((int)minX, (int)minY, (int)(maxX-minX), (int)(maxY-minY));
        }
    }

    @Override
    protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
        super.onPropertyChanged(oldValue, newValue, propertyName);
        switch (propertyName) {
        case PROPERTY_IMAGE:
            getImage(); // implicit call draw() -> drawBegin() -> drawModified() -> control.repaint() -> View.paintComponent -> drawAndroid()
            break;
        case PROPERTY_SIZE:
            ViewGroup.LayoutParams lp = _control.getLayoutParams();
            if (lp == null)
                break;
            SizeDouble s = (SizeDouble)newValue;
            if (s == null)
                s = getModel().getSize();
            lp.width  = (int)s.width;
            lp.height = (int)s.height;
            break;
        }
    }

    @Override
    protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
        super.onPropertyModelChanged(oldValue, newValue, propertyName);
        switch (propertyName) {
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
        case MosaicGameModel.PROPERTY_AREA:
            changeSizeImagesMineFlag();
            break;
        }
    }

    /** переустанавливаю заного размер мины/флага для мозаики */
    protected void changeSizeImagesMineFlag() {
        MosaicDrawModel<Bitmap> model = getModel();
        double sq = model.getCellAttr().getSq(model.getPenBorder().getWidth());
        if (sq <= 0) {
            System.err.println("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
        }

        final int max = 30;
        if (sq > max) {
            _imgFlag.getModel().setSize(sq);
            _imgMine.getModel().setSize(sq);
            model.setImgFlag(_imgFlag.getImage());
            model.setImgMine(_imgMine.getImage());
        } else {
            _imgFlag.getModel().setSize(max);
            model.setImgFlag(ImgUtils.zoom(_imgFlag.getImage(), sq, sq));
            _imgMine.getModel().setSize(max);
            model.setImgMine(ImgUtils.zoom(_imgMine.getImage(), sq, sq));
        }
    }

    @Override
    public void close() {
        getModel().close();
        super.close();
        _control = null;
        _imgFlag.close();
        _imgMine.close();
    }

}
