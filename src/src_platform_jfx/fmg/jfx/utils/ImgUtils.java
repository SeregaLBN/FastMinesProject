package fmg.jfx.utils;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/** вспомогательный класс для преобразований картинок */
public final class ImgUtils {

   public static Image toImage(Canvas self) {
      SnapshotParameters params = new SnapshotParameters();
      params.setFill(Color.TRANSPARENT);
      return self.snapshot(params, null);
   }

   public static Image zoom(Image img, int newWidth, int newHeight) {
      ImageView imageView = new ImageView(img);
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(newWidth);
      imageView.setFitHeight(newHeight);
      SnapshotParameters params = new SnapshotParameters();
      params.setFill(Color.TRANSPARENT);
      return imageView.snapshot(params, null);
  }

}
