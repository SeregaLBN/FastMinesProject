package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.function.Consumer;

import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Image view implementation over {@link android.graphics.Bitmap} */
public class AndroidBitmapView<TModel extends IImageModel> implements IImageView<Bitmap>, AutoCloseable {

    private final TModel model;
    private final Consumer<Canvas> draw;
    private boolean valid;
    private Bitmap bmp;
    private Canvas canvas;

    public AndroidBitmapView(TModel model, Consumer<Canvas> draw) {
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
        if ((bmp == null) ||
            (bmp.getWidth()  != (int)s.width) ||
            (bmp.getHeight() != (int)s.height))
        {
            if (bmp != null)
                bmp.recycle();

            bmp = Bitmap.createBitmap((int)s.width, (int)s.height, Bitmap.Config.ARGB_8888);
            canvas = null;
            valid = false;
        }
        if (canvas == null) {
            canvas = new Canvas(bmp);
            valid = false;
        }

        if (!valid) {
            draw.accept(canvas);
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
