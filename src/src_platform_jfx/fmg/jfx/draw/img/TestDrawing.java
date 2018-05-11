package fmg.jfx.draw.img;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.core.img.ATestDrawing;
import fmg.core.img.ATestDrawing.CellTilingInfo;
import fmg.core.img.ATestDrawing.CellTilingResult;
import fmg.core.img.ImageController;
import fmg.jfx.Cast;
import fmg.jfx.utils.ImgUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
public final class TestDrawing extends Application {

   static final int margin = 10;

   static Supplier<List<ImageController<?,?,?>>> funcGetImages;
   Canvas canvas;

   @Override
   public void start(Stage primaryStage) {

      ATestDrawing td = new ATestDrawing("JFX") {};

      List<ImageController<?,?,?>> images = funcGetImages.get();
      boolean[] testTransparent = { td.bl() };
      final RectDouble[] rc = { new RectDouble() };
      final CellTilingResult[] ctr = { new CellTilingResult() };

      AnimationTimer timer = new AnimationTimer() {

         @Override
         public void handle(long now) {
            if (rc[0] == null)
               return;

            if ((rc[0].width <= 0) || (rc[0].height <= 0))
               return;

            Size imgSize = ctr[0].imageSize;
            if ((imgSize.width <= 0) || (imgSize.height <= 0))
               return;

            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setLineWidth(1);
            gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            gc.setStroke(Cast.toColor(Color.DarkGray));
            gc.strokeRect(rc[0].x, rc[0].y, rc[0].width, rc[0].height);

            images.forEach(imgController -> {

               Function<ImageController<?,?,?>, CellTilingInfo> callback = ctr[0].itemCallback;
               CellTilingInfo cti = callback.apply(imgController);
               PointDouble offset = cti.imageOffset;

               Object imgObj = imgController.getImage();
               if (imgObj instanceof Canvas) {
                  Canvas canvasImg = (Canvas)imgObj;
                  imgObj = ImgUtils.toImage(canvasImg);
               } // else // no else!
               if (imgObj instanceof Image) {
                  Image img = (Image)imgObj;
                  gc.drawImage(img, offset.x, offset.y);
               } else
                  throw new IllegalArgumentException("Not supported image type is " + imgObj.getClass().getName());
            });
         }
      };
      timer.start();

      Runnable onCellTilingHandler = () -> {
         double w = canvas.getWidth();
         double h = canvas.getHeight();
         rc[0] = new RectDouble(margin, margin, w-margin*2, h-margin*2); // inner rect where drawing images as tiles
         ctr[0] = td.cellTiling(rc[0], images, testTransparent[0]);

         Size imgSize = ctr[0].imageSize;
         if (imgSize.height < 1 || imgSize.width < 1)
            return;
         for (ImageController<?, ?, ?> img : images)
            img.getModel().setSize(imgSize);
      };
      ChangeListener<Number> onSizeListener = (observable, oldValue, newValue) -> {
         double w = primaryStage.widthProperty().doubleValue();
         double h = primaryStage.heightProperty().doubleValue();
         if (Double.isNaN(w) || Double.isNaN(h))
            return;
         w -= 2*margin; // ???
         h -= 4*margin; // ???
         canvas.setWidth(w);
         canvas.setHeight(h);
         onCellTilingHandler.run();
      };
      primaryStage.widthProperty().addListener(onSizeListener);
      primaryStage.heightProperty().addListener(onSizeListener);

      EventHandler<MouseEvent> mouseHandler = ev -> {
         testTransparent[0] = td.bl();
         images.forEach(img -> {
            td.applyRandom(img, testTransparent[0]);
            onCellTilingHandler.run();
         });
      };
      primaryStage.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);

      PropertyChangeListener propertyChangeListener = ev -> {
         if (ImageController.PROPERTY_IMAGE.equals(ev.getPropertyName())) {
          //  jPanel.repaint();
         }
      };

      images.forEach(img -> {
         img.addListener(propertyChangeListener);
         td.applyRandom(img, testTransparent[0]);
      });


      primaryStage.setOnCloseRequest(event -> {
         images.forEach(img -> {
            img.removeListener(propertyChangeListener);
            img.close();
         });
         primaryStage.widthProperty().removeListener(onSizeListener);
         primaryStage.heightProperty().removeListener(onSizeListener);
         primaryStage.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
      });

      primaryStage.setTitle(td.getTitle(images));
      primaryStage.setScene(new Scene(new Group(canvas = new Canvas(300, 300))));
      primaryStage.show();
   }

   static void testApp(Supplier<List<ImageController<?,?,?>>> funcGetImages) {
      TestDrawing.funcGetImages = funcGetImages;
      launch();
   }

}
