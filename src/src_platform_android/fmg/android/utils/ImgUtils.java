package fmg.android.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** вспомогательный класс для преобразований картинок */
public final class ImgUtils {

   /** change size Bitmap */
   public static Bitmap zoom(Bitmap bmp, int newWidth, int newHeight) {
      if (bmp == null) return null;
      return Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);
   }

}
