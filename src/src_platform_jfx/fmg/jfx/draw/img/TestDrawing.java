package fmg.jfx.draw.img;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;
import fmg.core.img.PolarLightsImg;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;
import fmg.jfx.Cast;
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

   private static Random rnd = new Random(UUID.randomUUID().hashCode());
   private static int r(int max){ return rnd.nextInt(max); }
   private static boolean bl() { return (r(2) == 1); } // random bool
   private static int np() { return (bl() ? -1 : +1); } // negative or positive

   static void applyRandom(StaticImg<?> img) {
      int maxSize = (int)(SIZE/2.0 * 1.2);
      int minSize = (int)(maxSize * 0.8);
      img.setSize(new Size(minSize+r(maxSize-minSize), minSize+r(maxSize-minSize)));

      if (img instanceof RotatedImg) {
         RotatedImg<?> rImg = (RotatedImg<?>)img;
         rImg.setRotate(true);
         rImg.setRotateAngleDelta((3 + r(5)) * np());
         rImg.setRedrawInterval(50);
         rImg.setBorderWidth(bl() ? 1 : 2);
      }

      if (img instanceof PolarLightsImg) {
         PolarLightsImg<?> plImg = (PolarLightsImg<?>)img;
         plImg.setPolarLights(true);
      }

      if (img instanceof Logo) {
         Logo<?> logoImg = (Logo<?>)img;
         logoImg.setRotateMode(Logo.ERotateMode.values()[r(Logo.ERotateMode.values().length)]);
         logoImg.setUseGradient(bl());
      }

//      if (img instanceof MosaicsImg) {
//         MosaicsImg<?> mosaicsImg = (MosaicsImg<?>)img;
//         mosaicsImg.setRotateMode(MosaicsImg.ERotateMode.values()[r(MosaicsImg.ERotateMode.values().length)]);
//      }

      if (bl()) {
         // test transparent
         HSV bkClr = new HSV(Color.RandomColor(rnd)); bkClr.a = 50 + r(10);
         img.addListener(ev -> {
            if (RotatedImg.PROPERTY_ROTATE_ANGLE.equals(ev.getPropertyName())) {
               bkClr.h = img.getRotateAngle();
               img.setBackgroundColor(bkClr.toColor());
            }
         });
      } else {
         img.setBackgroundColor(Color.RandomColor(rnd).brighter());
      }
   }

   static Function<Pair<Size, Random>, List<?>> funcGetImages;
   Canvas canvas;

   @Override
   public void start(Stage primaryStage) {

      Rect rc = new Rect(margin, margin, SIZE-margin*2, SIZE-margin*2); // inner rect where drawing images as tiles
      List<?> images = funcGetImages.apply(new Pair<>(rc.size(), rnd));
      int len = images.size();
      int cols = (int)Math.round( Math.sqrt(len)  + 0.4999999999); // columns
      int rows = (int)Math.round(len/(double)cols + 0.4999999999);
      int dx = rc.width  / cols; // cell tile width
      int dy = rc.height / rows; // cell tile height

      int pad = 2; // cell padding
      int w = Math.min(dx, dy) - 2*pad; // dx - 2*pad;
      int h = Math.min(dx, dy) - 2*pad; // dy - 2*pad;

      AnimationTimer timer = new AnimationTimer() {
         @Override
         public void handle(long now) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setLineWidth(1);
            gc.setStroke(Cast.toColor(Color.Red));
          //gc.clearRect(rc.x, rc.y, rc.width, rc.height);
            gc.strokeRect(rc.x, rc.y, rc.width, rc.height);

            for (int i=0; i<cols; ++i)
               for (int j=0; j<rows; ++j) {
                  int pos = cols*j+i;
                  if (pos >= len)
                     break;

                  Object obj = images.get(pos);
                  if (obj instanceof StaticImg) {
                     @SuppressWarnings("resource")
                     StaticImg<?> simg = (StaticImg<?>)obj;
                     simg.setSize(new Size(w, h));
                     obj = simg.getImage();
                  }
                  if (obj instanceof Canvas) {
                     Canvas canvasImg = (Canvas)obj;
                     Image img = Logo.Canvas.castToImage(canvasImg);
                     gc.drawImage(img, margin+i*dx+pad, margin+j*dy+pad);
                  } else
                     throw new IllegalArgumentException("Not supported image type is " + obj.getClass().getName());
               }
         }
      };
      timer.start();

      images.stream()
         .filter(x -> x instanceof StaticImg)
         .map(x -> (StaticImg<?>)x)
         .forEach(img -> applyRandom(img) );

      primaryStage.setOnCloseRequest(event -> {
         images.stream()
            .filter(x -> x instanceof StaticImg)
            .map(x -> (StaticImg<?>)x)
            .forEach(img -> img.close() );
      });

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
