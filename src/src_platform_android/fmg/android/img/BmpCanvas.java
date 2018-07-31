package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Shader;

import java.util.Arrays;
import java.util.List;

import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;
import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.ImageView;
import fmg.core.img.LogoController;
import fmg.core.img.LogoModel;

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
