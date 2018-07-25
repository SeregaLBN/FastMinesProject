package fmg.android.draw.img;

import android.graphics.*;
import fmg.common.geom.SizeDouble;

/** Internal wrapper-image implementation over {@link android.graphics.Bitmap} and {@link android.graphics.Canvas} */
class CanvasBmp implements AutoCloseable {

   private Bitmap _bmp;
   private Canvas _canvas;

   public void create(SizeDouble size) {
      if (_bmp == null)
         _bmp = Bitmap.createBitmap((int)size.width, (int)size.height, Bitmap.Config.ARGB_8888);
      else
         _bmp.reconfigure((int)size.width, (int)size.height, Bitmap.Config.ARGB_8888);
   }

   public Bitmap getBmp() { return _bmp; }
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
