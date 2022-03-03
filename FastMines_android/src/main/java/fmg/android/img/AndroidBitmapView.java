package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.function.BiConsumer;

import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel2;
import fmg.core.img.IImageView2;

/** Image view implementation over {@link android.graphics.Bitmap} */
public class AndroidBitmapView<TModel extends IImageModel2> implements IImageView2<Bitmap>, AutoCloseable {

    private final TModel model;
    private final BiConsumer<Canvas, TModel> draw;
    private boolean valid;
    private Bitmap bmp;
    private Canvas canvas;

    public AndroidBitmapView(TModel model, BiConsumer<Canvas , TModel> draw) {
        this.model = model;
        this.draw = draw;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public Bitmap getImage() {
        SizeDouble s = model.getSize();
        if (bmp == null) {
            bmp = android.graphics.Bitmap.createBitmap((int)s.width, (int)s.height, android.graphics.Bitmap.Config.ARGB_8888);
            canvas = null;
            valid = false;
        } else {
            if (!DoubleExt.hasMinDiff(canvas.getWidth() , s.width) ||
                !DoubleExt.hasMinDiff(canvas.getHeight(), s.height))
            {
                bmp.recycle();
                bmp = android.graphics.Bitmap.createBitmap((int)s.width, (int)s.height, android.graphics.Bitmap.Config.ARGB_8888);
                canvas = null;
                valid = false;
            }
        }
        if (canvas == null) {
            canvas = new Canvas(bmp);
            valid = false;
        }

        if (!valid) {
            draw.accept(canvas, model);
            valid = true;
        }
        return bmp;
    }

    @Override
    public void reset() {
//        if (bmp != null)
//            bmp.recycle();
        //bmp = null;
        //canvas = null;
        valid = false;
    }

    @Override
    public void close() {
        if (bmp != null)
            bmp.recycle();
        bmp = null;
        canvas = null;
        valid = false;
    }

}
