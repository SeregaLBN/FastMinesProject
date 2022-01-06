package fmg.jfx.mosaic;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import fmg.common.geom.PointDouble;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicDrawModel;

/** MVC: controller. JavaFX implementation */
public class MosaicCanvasController extends MosaicController<Canvas, Image, MosaicCanvasView, MosaicDrawModel<Image>> {

    public MosaicCanvasController() {
        super(new MosaicCanvasView());
        subscribeToViewControl();
    }

    public Canvas getViewCanvas() {
        return getView().getImage();
    }

    private EventHandler<MouseEvent> mouseHandler = ev -> {
        PointDouble clickPoint = new PointDouble(ev.getX(), ev.getY());

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
        getView().close();
    }

}
