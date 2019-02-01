package fmg.jfx.app;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import fmg.core.mosaic.MosaicView;
import fmg.jfx.mosaic.MosaicCanvasController;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        MosaicView._DEBUG_DRAW_FLOW = true;
        MosaicCanvasController ctrllr = new MosaicCanvasController();

        stage.setScene(new Scene(new Group(ctrllr.getViewCanvas())));
        stage.setOnCloseRequest(event -> ctrllr.close());
        stage.setTitle("FastMines simple");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
