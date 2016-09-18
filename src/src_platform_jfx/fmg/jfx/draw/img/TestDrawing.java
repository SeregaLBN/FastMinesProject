package fmg.jfx.draw.img;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.core.img.ATestDrawing;
import fmg.core.img.ATestDrawing.CellTilingInfo;
import fmg.core.img.ATestDrawing.CellTilingResult;
import fmg.core.img.StaticImg;
import fmg.jfx.Cast;
import fmg.jfx.utils.ImgUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
public final class TestDrawing extends Application {
   static final int SIZE = 300;
   static final int margin = 10;

   static Function<Random, List<?>> funcGetImages;
   Canvas canvas;

   @Override
   public void start(Stage primaryStage) {

      ATestDrawing td = new ATestDrawing() { };

      RectDouble rc = new RectDouble(margin, margin, SIZE-margin*2, SIZE-margin*2); // inner rect where drawing images as tiles
      List<?> images = funcGetImages.apply(td.getRandom());
      boolean testTransparent = td.bl();
      CellTilingResult ctr = td.cellTiling(rc, images, testTransparent);
      Size imgSize = ctr.imageSize;
      Function<? /* image */, CellTilingInfo> callback = ctr.itemCallback;

      AnimationTimer timer = new AnimationTimer() {
         @Override
         public void handle(long now) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setLineWidth(1);
//            gc.setFill(javafx.scene.paint.Color.WHITE);
//            gc.fillRect(rc.x, rc.y, rc.width, rc.height);
          //gc.clearRect(rc.x, rc.y, rc.width, rc.height);
            gc.setStroke(Cast.toColor(Color.DarkGray));
            gc.strokeRect(rc.x, rc.y, rc.width, rc.height);
          //gc.setGlobalBlendMode(BlendMode.SRC_OVER); // see https://bugs.openjdk.java.net/browse/JDK-8092156

            images.stream()
               .map(x -> (Object)x)
               .forEach(imgObj -> {

                  @SuppressWarnings("unchecked")
                  Function<Object, CellTilingInfo> callback2 = (Function<Object, CellTilingInfo>)callback;
                  CellTilingInfo cti = callback2.apply(imgObj);
                  PointDouble offset  = cti.imageOffset;

                  if (imgObj instanceof StaticImg) {
                     StaticImg<?> simg = (StaticImg<?>)imgObj;
                     simg.setSize(imgSize);
                     imgObj = simg.getImage();
                  } else
                  if (imgObj instanceof Flag) {
                     Flag<?> simg = (Flag<?>)imgObj;
                     simg.setSize(imgSize);
                     imgObj = simg.getImage();
                  }
                  if (imgObj instanceof Canvas) {
                     Canvas canvasImg = (Canvas)imgObj;
                     imgObj = ImgUtils.toImage(canvasImg);
                  }
                  if (imgObj instanceof Image) {
                     Image img = (Image)imgObj;
                     gc.drawImage(img, offset.x, offset.y);
                  } else
                     throw new IllegalArgumentException("Not supported image type is " + imgObj.getClass().getName());
               });
         }
      };
      timer.start();

      images.stream()
         .filter(x -> x instanceof StaticImg)
         .map(x -> (StaticImg<?>)x)
         .forEach(img -> td.applyRandom(img, testTransparent) );

      primaryStage.setOnCloseRequest(event -> {
         images.stream()
            .filter(x -> x instanceof StaticImg)
            .map(x -> (StaticImg<?>)x)
            .forEach(img -> img.close() );
      });

      primaryStage.setTitle(td.getTitle(images));
      primaryStage.setScene(
                      new Scene(
                          new Group(canvas = new Canvas(SIZE, SIZE)),
                          SIZE, SIZE));
      primaryStage.show();
   }

   static void testApp(Function<Random, List<?>> funcGetImages) {
      TestDrawing.funcGetImages = funcGetImages;
      launch();
   }

}
