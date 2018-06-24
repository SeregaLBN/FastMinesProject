package fmg.jfx.draw.img;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ATestDrawing;
import fmg.core.img.ATestDrawing.CellTilingInfo;
import fmg.core.img.ATestDrawing.CellTilingResult;
import fmg.core.img.IImageController;
import fmg.jfx.utils.Cast;

/** @see {@link MosaicSkillImg#main}, {@link MosaicGroupImg#main}, {@link MosaicsImg#main} */
public final class TestDrawing extends Application {

   static final int margin = 10;

   static Supplier<List<IImageController<?,?,?>>> funcGetImages;
   Canvas canvas;

   @Override
   public void start(Stage primaryStage) {
    //setUserAgentStylesheet(STYLESHEET_MODENA);


      ATestDrawing td = new ATestDrawing("JFX") {};

      List<IImageController<?,?,?>> images = funcGetImages.get();
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

            SizeDouble imgSize = ctr[0].imageSize;
            if ((imgSize.width <= 0) || (imgSize.height <= 0))
               return;

            GraphicsContext gc = canvas.getGraphicsContext2D();
          //gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Cast.toColor(Color.Gray.brighter()));
            gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

          //gc.setStroke(Cast.toColor(Color.Black));
          //gc.setLineWidth(1);
            gc.strokeRect(rc[0].x, rc[0].y, rc[0].width, rc[0].height);

            Function<IImageController<?,?,?>, CellTilingInfo> callback = ctr[0].itemCallback;
            images.forEach(imgController -> {
               CellTilingInfo cti = callback.apply(imgController);
               PointDouble offset = cti.imageOffset;

               Object imgObj = imgController.getImage();
//               if (imgObj instanceof Canvas) {
//                  Canvas canvasImg = (Canvas)imgObj;
//                  imgObj = ImgUtils.toImage(canvasImg);
//               } // else // no else!
               if (imgObj instanceof Image) {
                  Image img = (Image)imgObj;
                  gc.drawImage(img, offset.x, offset.y);
               } //else
//                  throw new IllegalArgumentException("Not supported image type is " + imgObj.getClass().getName());
            });
         }
      };
      timer.start();

      Group group;
      Scene scene = new Scene(group = new Group(canvas = new Canvas(300, 300)));

      Runnable onCellTilingHandler = () -> {
         double w = canvas.getWidth();
         double h = canvas.getHeight();
         rc[0] = new RectDouble(margin, margin, w-margin*2, h-margin*2); // inner rect where drawing images as tiles
         ctr[0] = td.cellTiling(rc[0], images, testTransparent[0]);

         SizeDouble imgSize = ctr[0].imageSize;
         if (imgSize.height < 1 || imgSize.width < 1)
            return;

         Function<IImageController<?,?,?>, CellTilingInfo> callback = ctr[0].itemCallback;
         for (IImageController<?, ?, ?> img : images) {
            img.getModel().setSize(imgSize);

            Object imgObj = img.getImage();
            if (imgObj instanceof Canvas) {
               Canvas imgCanvas = (Canvas)imgObj;
               CellTilingInfo cti = callback.apply(img);
               PointDouble offset = cti.imageOffset;
               imgCanvas.relocate(offset.x, offset.y);
            }
         }
      };
      ChangeListener<Number> onSizeWListener = (observable, oldValue, newValue) -> {
         double w = (double)newValue;
         canvas.setWidth(w);
         onCellTilingHandler.run();
      };
      ChangeListener<Number> onSizeHListener = (observable, oldValue, newValue) -> {
         double h = (double)newValue;
         canvas.setHeight(h);
         onCellTilingHandler.run();
      };
      scene. widthProperty().addListener(onSizeWListener);
      scene.heightProperty().addListener(onSizeHListener);

      EventHandler<MouseEvent> mouseHandler = ev -> {
         testTransparent[0] = td.bl();
         images.forEach(img -> {
            td.applyRandom(img, testTransparent[0]);
            onCellTilingHandler.run();
         });
      };
      primaryStage.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);

      PropertyChangeListener propertyChangeListener = ev -> {
       //System.out.println("propertyChangeListener: " + ev.getSource().getClass().getSimpleName());
         if (IImageController.PROPERTY_IMAGE.equals(ev.getPropertyName())) {
            IImageController<?,?,?> imgCntrllr = (IImageController<?,?,?>)ev.getSource();
            Object imgObj = imgCntrllr.getImage();
            if (imgObj instanceof Canvas) {
               Canvas imgCanvas = (Canvas)imgObj;
               if (!group.getChildren().contains(imgCanvas))
                  group.getChildren().add(imgCanvas);
            }
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
         scene. widthProperty().removeListener(onSizeWListener);
         scene.heightProperty().removeListener(onSizeHListener);
         primaryStage.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
      });

      primaryStage.setTitle(td.getTitle(images));
      primaryStage.setScene(scene);
      primaryStage.setMinHeight(125);
      primaryStage.setMinWidth(100);
      primaryStage.show();
   }

   static void testApp(Supplier<List<IImageController<?,?,?>>> funcGetImages) {
      TestDrawing.funcGetImages = funcGetImages;
      launch();
   }

}
