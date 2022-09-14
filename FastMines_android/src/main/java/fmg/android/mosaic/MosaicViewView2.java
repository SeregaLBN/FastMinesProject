package fmg.android.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import fmg.android.app.DrawableView;
import fmg.android.img.Flag2;
import fmg.android.img.Logo2;
import fmg.android.img.MosaicImg2;
import fmg.common.geom.SizeDouble;
import fmg.core.img.MosaicDrawContext;
import fmg.core.mosaic.IMosaicView2;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;

/** MVC: view. Android implementation over control {@link DrawableView} */
public class MosaicViewView2 implements IMosaicView2<DrawableView>, AutoCloseable {

    private final MosaicModel2 model;
    private final Flag2.FlagAndroidBitmapController imgFlag;
    private final Logo2.LogoAndroidBitmapController imgMine;
    private Context context;
    private DrawableView control;
    private final Collection<BaseCell> modifiedCells = new HashSet<>();
    private boolean valid = false;
    private boolean drawBk = true;
    private Bitmap lastImg;
    private Canvas lastCanvas;

    public MosaicViewView2(Context context,
                           MosaicModel2 model,
                           Flag2.FlagAndroidBitmapController imgFlag,
                           Logo2.LogoAndroidBitmapController imgMine)
    {
        this.context = context;
        this.model = model;
        this.imgFlag = imgFlag;
        this.imgMine = imgMine;
    }

    @Override
    public DrawableView getImage() {
        // will return once created window
        return getControl();
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
            this.control.drawMethod = this::draw;
    }

    private void draw(Canvas canvas) {
        try {
            draw2(canvas);
        } finally {
            valid = true;
            drawBk = true;
            modifiedCells.clear();
        }
    }

    private void draw2(Canvas canvas) {
//        Rect clipBounds = new Rect();
//        canvas.getClipBounds(clipBounds);

        SizeDouble size = model.getSize();
        if ((lastImg == null) ||
            (lastImg.getWidth()  != (int)size.width) ||
            (lastImg.getHeight() != (int)size.height))
        {
            if (lastImg != null)
                lastImg.recycle();

            lastImg = Bitmap.createBitmap((int)size.width, (int)size.height, Bitmap.Config.ARGB_8888);
            lastCanvas = null;
            modifiedCells.clear(); // redraw all
        }

        var drawContext = new MosaicDrawContext<>(
            model,
            drawBk,
            model::getBackgroundColor,
            modifiedCells.isEmpty()
                ? model::getMatrix
                : () -> modifiedCells,
            imgMine::getImage,
            imgFlag::getImage);

        if (lastCanvas == null)
            lastCanvas = new Canvas(lastImg);

        MosaicImg2.draw(lastCanvas, drawContext);
        canvas.drawBitmap(lastImg, 0, 0, null);
    }

    public void onModelChanged(String property) {
        switch (property) {
        case MosaicModel2.PROPERTY_MOSAIC_TYPE:
        case MosaicModel2.PROPERTY_SIZE_FIELD:
            lastImg = null;
            break;
        default:
            // none
        }
    }

    @Override
    public void invalidate() {
        valid = false;
        drawBk = true;
        this.modifiedCells.clear(); // all matrix

        if (control != null)
            control.invalidate();
    }

    @Override
    public void invalidate(Collection<BaseCell> modifiedCells) {
        Objects.requireNonNull(modifiedCells);
        if (modifiedCells.isEmpty())
            throw new IllegalArgumentException("Required not empty");

        valid = false;
        drawBk = false;
        this.modifiedCells.addAll(modifiedCells);

        if (control != null)
            control.invalidate();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void reset() {
        valid = false;
        drawBk = true;
        this.modifiedCells.clear();
    }

    @Override
    public void close() {
        setControl(null);
    }

}
