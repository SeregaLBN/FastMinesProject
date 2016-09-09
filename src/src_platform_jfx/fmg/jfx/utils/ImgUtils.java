package fmg.jfx.utils;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** вспомогательный класс для преобразований картинок */
public final class ImgUtils {

   public static Image toImage(Canvas self) {
      SnapshotParameters params = new SnapshotParameters();
      params.setFill(Color.TRANSPARENT);
      return self.snapshot(params, null);
   }

}
