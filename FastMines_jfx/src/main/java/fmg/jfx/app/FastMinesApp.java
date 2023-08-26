package fmg.jfx.app;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import fmg.jfx.mosaic.MosaicCanvasController;

/** Simple app
 * <p>run from command line
 * <br> <code>

  gradle :FastMines_jfx:run

 */
public class FastMinesApp extends Application {

    @Override
    public void start(Stage stage) {
        var ctrllr = new MosaicCanvasController();

        stage.setScene(new Scene(new Group(ctrllr.getViewCanvas())));
        stage.setOnCloseRequest(event -> ctrllr.close());
        stage.setTitle("FastMines simple");
        stage.show();
    }

    public static void main(String[] args) {
        ProjSettings.init();
        launch(args);
    }

}
