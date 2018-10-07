package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import fmg.common.geom.SizeDouble;

/** Internal class-wrapper for pair {@link Bitmap} and {@link Canvas} */
class BmpCanvas implements AutoCloseable {

    private Bitmap _bmp;
    private Canvas _canvas;

    public Bitmap createImage(SizeDouble size) {
//      if (_bmp == null)
            _bmp = android.graphics.Bitmap.createBitmap((int)size.width, (int)size.height, android.graphics.Bitmap.Config.ARGB_8888);
//      else
//         _bmp.reconfigure((int)size.width, (int)size.height, android.graphics.Bitmap.Config.ARGB_8888);
        _canvas = null;
        return _bmp;
    }

    public Canvas getCanvas() {
        if ((_canvas == null) && (_bmp != null))
            _canvas = new Canvas(_bmp);
        return _canvas;
    }

    @Override
    public void close() {
        if (_bmp == null)
            return;
        _bmp.recycle();
        _bmp = null;
        _canvas = null;
    }

}
