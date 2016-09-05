package fmg.jfx;

import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

// onClickExample(primaryStage);
// ellipseExample(primaryStage);
// imageExample(primaryStage);
//        drawShapesExample(primaryStage);
        drawAnimationExample(primaryStage);
    }

    private void onClickExample(Stage stage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(event -> System.out.println("Hello World!"));

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }

    private void ellipseExample(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 300, 250, Color.WHITE);

        Group g = new Group();

        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setColor(Color.color(0.4, 0.4, 0.4));

        Ellipse ellipse = new Ellipse();
        ellipse.setCenterX(50.0f);
        ellipse.setCenterY(50.0f);
        ellipse.setRadiusX(50.0f);
        ellipse.setRadiusY(25.0f);
        ellipse.setEffect(ds);

        g.getChildren().add(ellipse);

        root.getChildren().add(g);
        stage.setScene(scene);
        stage.show();
    }

    private void imageExample(Stage stage) {
        Canvas canvas = new Canvas(300, 250);
        Image img = new Image("file:///home/serega/Downloads/world_600w.jpg", 350, 0, true, true);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.save(); // saves the current state on stack, including the current transform
        gc.rotate(45);
        gc.drawImage(img, 10, 10);
        gc.restore(); // back to original state (before rotation)

        gc.save();
        gc.rotate(-50);
        gc.drawImage(img, 10, 10);
        gc.restore();

        // supplies a tiled background image on which the canvas is drawn.
        StackPane stack = new StackPane();
        stack.setMaxSize(canvas.getWidth(), canvas.getHeight());
        stack.setStyle("-fx-background-image: url('https://www.google.com.ua/images/nav_logo242.png');");
        stack.getChildren().add(
                                canvas);

        // places a resizable padded frame around the canvas.
        StackPane frame = new StackPane();
        frame.setPadding(new Insets(20));
        frame.getChildren().add(stack);

        stage.setScene(new Scene(frame, Color.BURLYWOOD));
        stage.show();
    }

    private void drawShapesExample(Stage stage) {
        stage.setTitle("Graphics in JavaFX");
        Group root = new Group();
        Canvas canvas = new Canvas(650, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        draw2DShapes(gc);
        root.getChildren().add(canvas);
        stage.setScene(new Scene(root));
        stage.show();
    }
    private void draw2DShapes(GraphicsContext gc) {
        double width = gc.getCanvas().getWidth();
        double height = gc.getCanvas().getHeight();

        Random random = new Random(System.currentTimeMillis());

        gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255), 0.9));
        gc.translate(width / 2, height / 2);

        for (int i = 0; i < 60; i++) {
           gc.rotate(6.0);

           gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255), 0.9));
           gc.fillOval(10, 60, 30, 30);

           gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255), 0.9));
           gc.strokeOval(60, 60, 30, 30);

           gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255), 0.9));
           gc.fillRoundRect(110, 60, 30, 30, 10, 10);

           gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255), 0.9));
           gc.fillPolygon(
               new double[] { 105, 117, 159, 123, 133, 105, 77, 87,51, 93 },
               new double[] { 150, 186, 186, 204, 246, 222, 246,204, 186, 186 }, 10);
        }
     }

    private void drawAnimationExample(Stage stage) {
        final double W = 299; // canvas dimensions.
        final double H = 200;
        final double D = 20;  // diameter.
        DoubleProperty x  = new SimpleDoubleProperty();
        DoubleProperty y  = new SimpleDoubleProperty();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
                                                      new KeyValue(x, 0),
                                                      new KeyValue(y, 0)),
                                         new KeyFrame(Duration.seconds(3),
                                                      new KeyValue(x, W - D),
                                                      new KeyValue(y, H - D)),
                                         new KeyFrame(Duration.seconds(3),
                                                      new KeyValue(x, W - D),
                                                      new KeyValue(y, H - D)
            )
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);

        final Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.setFill(Color.CORNSILK);
                gc.fillRect(0, 0, W, H);
                gc.setFill(Color.FORESTGREEN);
                gc.fillOval(
                    x.doubleValue(),
                    y.doubleValue(),
                    D,
                    D
                );
            }
        };

        stage.setScene(
            new Scene(
                new Group(
                    canvas
                )
            )
        );
        stage.show();

        timer.start();
        timeline.play();
    }

}
