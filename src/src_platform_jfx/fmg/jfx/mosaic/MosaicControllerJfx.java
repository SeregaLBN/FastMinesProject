package fmg.jfx.mosaic;

import java.util.Optional;

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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import fmg.common.geom.PointDouble;
import fmg.core.mosaic.AMosaicController;
import fmg.core.mosaic.AMosaicView;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** MVC: controller. JavaFX implementation */
public class MosaicControllerJfx extends AMosaicController<Canvas, Image, MosaicViewJfx, MosaicDrawModel<Image>> {

   public MosaicControllerJfx() {
      super(new MosaicViewJfx());
      subscribeToViewControl();
   }

   public Canvas getViewCanvas() {
      return getView().getImage();
   }

   private EventHandler<MouseEvent> mouseHandler = ev -> {
      PointDouble clickPoint = new PointDouble(ev.getSceneX(), ev.getSceneY());

      if (ev.getEventType() == MouseEvent.MOUSE_PRESSED) {
         if (ev.isPrimaryButtonDown())
            MosaicControllerJfx.this.mousePressed(clickPoint, true);
         if (ev.isSecondaryButtonDown())
            MosaicControllerJfx.this.mousePressed(clickPoint, false);
      }
      else
      if (ev.getEventType() == MouseEvent.MOUSE_RELEASED) {
         // Получаю этот эвент на отпускание клавиши даже тогда, когда окно проги неактивно..
         // Избегаю срабатывания onClick'a
         if (!((Node)ev.getSource()).getScene().getWindow().isFocused())
            return;

         if (ev.isPrimaryButtonDown())
            MosaicControllerJfx.this.mouseReleased(clickPoint, true);
         if (ev.isSecondaryButtonDown())
            MosaicControllerJfx.this.mouseReleased(clickPoint, false);
      }
   };

   private ChangeListener<Boolean> focusHandler = (observableBoolValue, oldPropertyValue, newPropertyValue) -> {
      if (!newPropertyValue)
         MosaicControllerJfx.this.mouseFocusLost();
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
      super.close();
      unsubscribeToViewControl();
   }

   ////////////// TEST //////////////
   public static class Demo extends Application {

      @Override
      public void start(Stage stage) {
         AMosaicView._DEBUG_DRAW_FLOW = true;
         MosaicControllerJfx ctrllr = new MosaicControllerJfx();
         EMosaic mosaicType = EMosaic.eMosaicSquare1;
         ESkillLevel skill  = ESkillLevel.eBeginner;

         ctrllr.setArea(1500);
         ctrllr.setMosaicType(mosaicType);
         ctrllr.setSizeField(skill.getDefaultSize());
         ctrllr.setMinesCount(skill.getNumberMines(mosaicType));
         ctrllr.gameNew();

         stage.setScene(new Scene(new Group(ctrllr.getViewCanvas())));
         stage.setOnCloseRequest(event -> ctrllr.close());
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
