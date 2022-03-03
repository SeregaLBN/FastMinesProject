package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import fmg.common.geom.SizeDouble;

/** Internal class-wrapper for pair {@link Bitmap} and {@link Canvas} */
@Deprecated
class BmpCanvas implements AutoCloseable {

    private Bitmap bmp;
    private Canvas canvas;

    public Bitmap createImage(SizeDouble size) {
//      if (_bmp == null)
            bmp = android.graphics.Bitmap.createBitmap((int)size.width, (int)size.height, android.graphics.Bitmap.Config.ARGB_8888);
//      else
//         _bmp.reconfigure((int)size.width, (int)size.height, android.graphics.BitmapView.Config.ARGB_8888);
        canvas = null;
        return bmp;
    }

    public Canvas getCanvas() {
        if ((canvas == null) && (bmp != null))
            canvas = new Canvas(bmp);
        return canvas;
    }

    @Override
    public void close() {
        if (bmp == null)
            return;
        bmp.recycle();
        bmp = null;
        canvas = null;
    }

}
