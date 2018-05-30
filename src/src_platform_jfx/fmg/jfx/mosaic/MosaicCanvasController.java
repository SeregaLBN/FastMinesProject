package fmg.jfx.mosaic;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import fmg.common.geom.PointDouble;
import fmg.core.mosaic.AMosaicController;
import fmg.core.mosaic.AMosaicView;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** MVC: controller. JavaFX implementation */
public class MosaicCanvasController extends AMosaicController<Canvas, Image, MosaicCanvasView, MosaicDrawModel<Image>> {

   public MosaicCanvasController() {
      super(new MosaicCanvasView());
      subscribeToViewControl();
   }

   public Canvas getViewCanvas() {
      return getView().getImage();
   }

   private EventHandler<MouseEvent> mouseHandler = ev -> {
      PointDouble clickPoint = new PointDouble(ev.getSceneX(), ev.getSceneY());

      if (ev.getEventType() == MouseEvent.MOUSE_PRESSED) {
         if (ev.isPrimaryButtonDown())
            MosaicCanvasController.this.mousePressed(clickPoint, true);
         if (ev.isSecondaryButtonDown())
            MosaicCanvasController.this.mousePressed(clickPoint, false);
      }
      else
      if (ev.getEventType() == MouseEvent.MOUSE_RELEASED) {
         // Получаю этот эвент на отпускание клавиши даже тогда, когда окно проги неактивно..
         // TODO: на самом деле, под JavaFX, не получал этого эвента..
         // Избегаю срабатывания onClick'a
         if (!((Node)ev.getSource()).getScene().getWindow().isFocused())
            return;

         MouseButton eBttn = ev.getButton();
         if (eBttn == MouseButton.PRIMARY)
            MosaicCanvasController.this.mouseReleased(clickPoint, true);
         else
         if (eBttn == MouseButton.SECONDARY)
            MosaicCanvasController.this.mouseReleased(clickPoint, false);
      }
   };

   private ChangeListener<Boolean> focusHandler = (observableBoolValue, oldPropertyValue, newPropertyValue) -> {
      if (!newPropertyValue)
         MosaicCanvasController.this.mouseFocusLost();
   };

   @Override
   protected boolean checkNeedRestoreLastGame() {
      Alert alert = new Alert(AlertType.CONFIRMATION, "Restore last game?", ButtonType.OK, ButtonType.CANCEL);
      alert.setTitle("Question");
      Optional<ButtonType> result = alert.showAndWait();
      return result.isPresent() && (result.get() == ButtonType.OK);
   }

   private void subscribeToViewControl() {
      Canvas control = getViewCanvas();
      control.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
      control.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseHandler);
      control.focusedProperty().addListener(focusHandler);
   }

   private void unsubscribeToViewControl() {
      Canvas control = getViewCanvas();
      control.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
      control.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseHandler);
      control.focusedProperty().removeListener(focusHandler);
   }

   @Override
   public void close() {
      unsubscribeToViewControl();
      super.close();
   }

   ////////////// TEST //////////////
   public static class Demo extends Application {

      @Override
      public void start(Stage stage) {
         AMosaicView._DEBUG_DRAW_FLOW = true;
         MosaicCanvasController ctrllr = new MosaicCanvasController();

         if (ThreadLocalRandom.current().nextBoolean()) {
            // unmodified controller test
         } else {
            EMosaic mosaicType = EMosaic.eMosaicTrSq1;
            ESkillLevel skill  = ESkillLevel.eBeginner;

            ctrllr.setArea(1500);
            ctrllr.setMosaicType(mosaicType);
            ctrllr.setSizeField(skill.getDefaultSize());
            ctrllr.setMinesCount(skill.getNumberMines(mosaicType));
            ctrllr.gameNew();
         }

         stage.setScene(new Scene(new Group(ctrllr.getViewCanvas())));
         stage.setOnCloseRequest(event -> ctrllr.close());
         stage.setTitle("JFX: Demo " + MosaicCanvasController.class.getSimpleName());
         stage.show();
      }

      public static void run(String[] args) {
          launch(args);
      }
   }
   public static void main(String[] args) {
      Demo.run(args);
   }
   //////////////////////////////////

}
