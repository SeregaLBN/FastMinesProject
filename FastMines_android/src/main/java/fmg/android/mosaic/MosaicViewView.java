package fmg.android.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import fmg.android.app.DrawableView;
import fmg.android.img.Flag;
import fmg.android.img.Logo;
import fmg.android.img.MosaicImg;
import fmg.common.geom.SizeDouble;
import fmg.core.img.MosaicDrawContext;
import fmg.core.img.PropertyConst;
import fmg.core.mosaic.IMosaicView;
import fmg.core.mosaic.MosaicModel;
import fmg.core.mosaic.cells.BaseCell;

/** MVC: view. Android implementation over control {@link DrawableView} */
public class MosaicViewView implements IMosaicView<DrawableView>, AutoCloseable {

    private final MosaicModel model;
    private final Flag.FlagAndroidBitmapController imgFlag;
    private final Logo.LogoAndroidBitmapController imgMine;
    private Context context;
    private DrawableView control;
    private final Collection<BaseCell> modifiedCells = new HashSet<>();
    private boolean valid = false;
    private boolean drawBk = true;
    private Bitmap lastImg;
    private Canvas lastCanvas;

    public MosaicViewView(Context context,
                          MosaicModel model,
                          Flag.FlagAndroidBitmapController imgFlag,
                          Logo.LogoAndroidBitmapController imgMine)
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

        MosaicImg.draw(lastCanvas, drawContext);
        canvas.drawBitmap(lastImg, 0, 0, null);
    }

    public void onModelChanged(String property) {
        switch (property) {
        case PropertyConst.PROPERTY_MOSAIC_TYPE:
        case PropertyConst.PROPERTY_SIZE_FIELD:
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
        modifiedCells.clear();

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
        modifiedCells.clear();
    }

    @Override
    public void close() {
        setControl(null);
    }

}
