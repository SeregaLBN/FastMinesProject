package fmg.jfx.mosaic;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import fmg.common.Logger;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicModel;
import fmg.jfx.img.Flag;
import fmg.jfx.img.Logo;

/** MVC: controller. JavaFX implementation */
public class MosaicCanvasController extends MosaicController<Canvas, MosaicCanvasView> {

    private final Flag.FlagJfxImageController imgFlag;
    private final Logo.LogoJfxImageController imgMine;

    public MosaicCanvasController() {
        imgFlag = new Flag.FlagJfxImageController();
        imgMine = new Logo.LogoJfxImageController();
        imgMine.asMine();

        var m = new MosaicModel(true);
        var v = new MosaicCanvasView(m, imgFlag, imgMine);
        init(m, v);
    }

    public Canvas getViewCanvas() {
        return view.getImage();
    }

    private EventHandler<MouseEvent> mouseHandler = ev -> {
        PointDouble clickPoint = new PointDouble(ev.getX(), ev.getY());

        var event = ev.getEventType();
        if (event == MouseEvent.MOUSE_PRESSED) {
            if (ev.isPrimaryButtonDown())
                MosaicCanvasController.this.mousePressed(clickPoint, true);
            if (ev.isSecondaryButtonDown())
                MosaicCanvasController.this.mousePressed(clickPoint, false);
        }
        else
        if (event == MouseEvent.MOUSE_RELEASED) {
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
        if (Boolean.FALSE.equals(newPropertyValue))
            MosaicCanvasController.this.mouseFocusLost();
    };

    @Override
    protected boolean checkNeedRestoreLastGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION, "Restore last game?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Question");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && (result.get() == ButtonType.OK);
    }

    @Override
    protected void subscribeToViewControl() {
        Canvas control = getViewCanvas();
        control.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
        control.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseHandler);
        control.focusedProperty().addListener(focusHandler);
    }

    @Override
    protected void unsubscribeToViewControl() {
        Canvas control = getViewCanvas();
        control.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
        control.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseHandler);
        control.focusedProperty().removeListener(focusHandler);
    }

    /** переустанавливаю заного размер мины/флага для мозаики */
    @Override
    protected void onChangeCellSquareSize() {
        double sq = model.getCellSquareSize();
        if (sq <= 0) {
            Logger.error("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
        }

        model.getFontInfo().setSize(sq);

        final int max = 30;
        if (sq < max)
            sq = max;
        imgFlag.getModel().setSize(new SizeDouble(sq, sq));
        imgMine.getModel().setSize(new SizeDouble(sq, sq));
    }

}
