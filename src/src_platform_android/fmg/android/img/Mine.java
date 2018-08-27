package fmg.android.img;

import java.util.Arrays;
import java.util.List;

import fmg.core.img.IImageController;
import fmg.core.img.LogoModel;

/** Mine image on the playing field */
public final class Mine {

   /** Mine image controller implementation for {@link Logo.Bitmap} */
   public static class ControllerBitmap extends Logo.ControllerBitmap {
      public ControllerBitmap() { LogoModel.toMineModel(getModel()); }
   }

}
