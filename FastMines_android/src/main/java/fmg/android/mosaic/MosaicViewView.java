package fmg.android.mosaic;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import fmg.android.app.DrawableView;
import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.android.img.Flag;
import fmg.android.img.Mine;
import fmg.android.utils.Cast;
import fmg.android.utils.ImgUtils;

/** MVC: view. Android implementation over control {@link DrawableView} */
public class MosaicViewView extends MosaicAndroidView<DrawableView, Bitmap, MosaicDrawModel<Bitmap>> {

    private Context context;
    private DrawableView control;
    private Flag.BitmapController imgFlag = new Flag.BitmapController();
    private Mine.BitmapController imgMine = new Mine.BitmapController();
    private final Rect clipBounds = new Rect();

    public MosaicViewView(Context context) {
        super(new MosaicDrawModel<>());
        this.context = context;
        changeSizeImagesMineFlag();
    }

    @Override
    protected DrawableView createImage() {
        // will return once created window
        return getControl();
    }

    private void onDraw(Canvas canvas) {
        canvas.getClipBounds(clipBounds);

        drawAndroid(canvas,
                (clipBounds==null)
                        ? null
                        : toDrawCells(Cast.toRectDouble(clipBounds)),
                true);
    }

    public DrawableView getControl() {
        if (control == null) {
            if (context == null)
                return null;
            setControl(new DrawableView(context));
        }
        return control;
    }

    public void setControl(DrawableView view) {
        if (this.control != null)
            this.control.drawMethod = null;

        this.context = (view==null) ? null : view.getContext();
        this.control = view;

        if (this.control != null)
            this.control.drawMethod = this::onDraw;
    }

    @Override
    protected void drawModified(Collection<BaseCell> modifiedCells) {
        View control = getControl();
        if (control == null)
            return;

        assert !alreadyPainted;

        control.invalidate();
    }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case PROPERTY_IMAGE:
            getImage(); // implicit call draw() -> drawBegin() -> drawModified() -> control.repaint() -> View.paintComponent -> drawAndroid()
            break;
        case PROPERTY_SIZE:
            ViewGroup.LayoutParams lp = control.getLayoutParams();
            if (lp == null)
                break;
            SizeDouble s = (SizeDouble)ev.getNewValue();
            if (s == null)
                s = getModel().getSize();
            lp.width  = (int)s.width;
            lp.height = (int)s.height;
            break;
        }
    }

    @Override
    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        super.onModelPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
        case MosaicGameModel.PROPERTY_AREA:
            changeSizeImagesMineFlag();
            break;
        }
    }

    /** переустанавливаю заного размер мины/флага для мозаики */
    protected void changeSizeImagesMineFlag() {
        MosaicDrawModel<Bitmap> model = getModel();
        double sq = model.getShape().getSq(model.getPenBorder().getWidth());
        if (sq <= 0) {
            Logger.error("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
        }

        final int max = 30;
        if (sq > max) {
            imgFlag.getModel().setSize(new SizeDouble(sq, sq));
            imgMine.getModel().setSize(new SizeDouble(sq, sq));
            model.setImgFlag(imgFlag.getImage());
            model.setImgMine(imgMine.getImage());
        } else {
            int imgSize = (int)sq;
            if (imgSize < 1) {
                Logger.info("bad image size " + sq);
                imgSize = 1;
            }
            imgFlag.getModel().setSize(new SizeDouble(max, max));
            model.setImgFlag(ImgUtils.zoom(imgFlag.getImage(), imgSize, imgSize));
            imgMine.getModel().setSize(new SizeDouble(max, max));
            model.setImgMine(ImgUtils.zoom(imgMine.getImage(), imgSize, imgSize));
        }
    }

    @Override
    public void close() {
        super.close();
        getModel().close();
        setControl(null);
        imgFlag.close();
        imgMine.close();
    }

}
