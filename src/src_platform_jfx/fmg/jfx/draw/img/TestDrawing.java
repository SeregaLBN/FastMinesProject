package fmg.jfx.draw.img;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;
import fmg.core.img.ITestDrawing;
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
public final class TestDrawing extends Application implements ITestDrawing {
   static final int SIZE = 300;
   static final int margin = 10;

   static Random rnd = new Random(UUID.randomUUID().hashCode());
   @Override
   public Random getRandom() { return rnd; }

   static Function<Pair<Size, Random>, List<?>> funcGetImages;
   Canvas canvas;

   @Override
   public void start(Stage primaryStage) {

      Rect rc = new Rect(margin, margin, SIZE-margin*2, SIZE-margin*2); // inner rect where drawing images as tiles
      List<?> images = funcGetImages.apply(new Pair<>(rc.size(), rnd));
      boolean testTransparent = bl();
      Pair<Size, // image size
           Function<? /* image */, PointDouble /* image offset */>> // Stream mapper
         cellTilings = cellTiling(rc, images, testTransparent);
      Size imgSize = cellTilings.first;
      Function<? /* image */, PointDouble /* image offset */> mapper = cellTilings.second;

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
                  Function<Object, PointDouble> mapper2 = (Function<Object, PointDouble>)mapper;
                  PointDouble offset = mapper2.apply(imgObj);

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
         .forEach(img -> applyRandom(img, testTransparent) );

      primaryStage.setOnCloseRequest(event -> {
         images.stream()
            .filter(x -> x instanceof StaticImg)
            .map(x -> (StaticImg<?>)x)
            .forEach(img -> img.close() );
      });

      primaryStage.setTitle("test paints: " + images.stream()
         .map(i -> i.getClass().getName())
         .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replaceAll("\\$", ".") )
         .collect(Collectors.joining(" & ")));
      primaryStage.setScene(
                      new Scene(
                          new Group(canvas = new Canvas(SIZE, SIZE)),
                          SIZE, SIZE));
      primaryStage.show();
   }

   static void testApp(Function<Pair<Size, Random>, List<?>> funcGetImages) {
      TestDrawing.funcGetImages = funcGetImages;
      launch();
   }

}
