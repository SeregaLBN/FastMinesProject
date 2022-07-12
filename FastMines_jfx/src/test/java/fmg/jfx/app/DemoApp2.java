package fmg.jfx.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.IImageController2;
import fmg.core.img.ImageHelper;
import fmg.core.img.SmileModel2.EFaceType;
import fmg.core.img.TestDrawing2;
import fmg.core.img.TestDrawing2.CellTilingInfo;
import fmg.core.types.EMosaicGroup;
import fmg.jfx.img.*;
import fmg.jfx.mosaic.MosaicCanvasController;
import fmg.jfx.utils.Cast;

/** live UI test application
 * <p>run from command line
 * <br> <code>

  gradle :FastMines_jfx:runDemoApp

 */
public final class DemoApp2 extends Application {

    static final int MARGIN = 10;

    private TestDrawing2 td;
    private Stage primaryStage;
    private Pane pane;
    private Canvas canvas;
    private Runnable onCloseImages;
    private Runnable[] onCreateImages; // images factory
    private int nextCreateImagesIndex;

    // #region images Fabrica
//    public void testMosaicControl() {
//        //MosaicView._DEBUG_DRAW_FLOW = true;
//        testApp(() -> {
//            MosaicCanvasController ctrllr = new MosaicCanvasController();
//            if (ThreadLocalRandom.current().nextBoolean()) {
//                // unmodified controller test
//            } else {
//                EMosaic mosaicType = EMosaic.fromOrdinal(ThreadLocalRandom.current().nextInt(EMosaic.values().length));
//                ESkillLevel skill  = ESkillLevel.eBeginner;
//
//                ctrllr.setMosaicType(mosaicType);
//                ctrllr.setSizeField(skill.getDefaultSize());
//                ctrllr.setCountMines(skill.getNumberMines(mosaicType));
//                ctrllr.gameNew();
//            }
//            return Stream.of(ctrllr);
//        }
//    );}
//
//    public void testMosaicImg() {
//        testApp(() ->
//            // // test single
//            // Stream.of(new MosaicImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})
//
//            // test all
//            Stream.of(EMosaic.values())
//
//                 //// variant 1
//                 //.map(e -> Stream.of(new MosaicImg.ControllerCanvas(),
//                 //                    new MosaicImg.ControllerImage ())
//                 //         .peek(ctrlr -> ctrlr.setMosaicType(e)))
//                 //.flatMap(x -> x)
//
//                // variant 2
//                .map(e -> {
//                        MosaicImageController<?, ?> ctrlr = ThreadLocalRandom.current().nextBoolean()
//                                ? new MosaicImg.CanvasController()
//                                : new MosaicImg.ImageJfxController();
//                        ctrlr.setMosaicType(e);
//                        return ctrlr;
//                    }));
//    }
    public void testMosaicGroupImg() {
        testApp(() ->
            Stream.concat(Stream.of((EMosaicGroup)null),
                          Stream.of(EMosaicGroup.values()))
                .map(e -> new Pair<>(new MosaicGroupImg2.MosaicGroupJfxCanvasController(e),
                                     new MosaicGroupImg2.MosaicGroupJfxImageController (e)))
                .flatMap(x -> Stream.of(x.first, x.second))
        );
    }
//    public void testMosaicSkillImg() {
//        testApp(() ->
//            Stream.concat(Stream.of((ESkillLevel)null),
//                          Stream.of(ESkillLevel.values()))
//                .map(e -> new Pair<>(new MosaicSkillImg.CanvasController(e),
//                                     new MosaicSkillImg.ImageJfxController(e)))
//                .flatMap(x -> Stream.of(x.first, x.second))
//        );
//    }
    public void testLogo() {
        testApp(() -> Stream.of(new Logo2.LogoJfxImageController()
                              , new Logo2.LogoJfxCanvasController()
                              , new Logo2.LogoJfxImageController()
                              , new Logo2.LogoJfxCanvasController()
                              , new Logo2.LogoJfxImageController().asMine()
                              , new Logo2.LogoJfxCanvasController().asMine()));
    }
    public void testFlag() {
        testApp(() -> Stream.of(new Flag2.FlagJfxCanvasController()
                              , new Flag2.FlagJfxImageController()));
    }
    public void testSmile() {
        testApp(() -> Stream.of(EFaceType.values())
                    .map(e -> Stream.of(new Smile2.SmileJfxCanvasController(e),
                                        new Smile2.SmileJfxImageController(e)))
                    .flatMap(x -> x));
    }
    // #endregion


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        td = new TestDrawing2("JFX");

        onCreateImages = new Runnable[] {
//            this::testMosaicControl,
//            this::testMosaicImg,
//            this::testMosaicSkillImg,
            this::testMosaicGroupImg,
            this::testSmile,
            this::testLogo,
            this::testFlag
        };


        BorderPane border = new BorderPane();
        border.setStyle("-fx-border-color: black;");
        Scene scene = new Scene(border);
        { // top
            HBox hbox = new HBox();
            hbox.setPadding(new Insets(1, 1, 3, 1));
            hbox.setSpacing(1);
            hbox.setStyle("-fx-background-color: #336699;");

            Button prevImagesBtn = new Button("...Previous");
            Button refreshButton = new Button("🗘");
            Button nextImagesBtn = new Button("Next...");
            prevImagesBtn.setMinSize(100, 20);
            refreshButton.setMinSize(20, 20);
            nextImagesBtn.setMinSize(100, 20);
            HBox.setHgrow(prevImagesBtn, Priority.ALWAYS);
            HBox.setHgrow(refreshButton, Priority.ALWAYS);
            HBox.setHgrow(nextImagesBtn, Priority.ALWAYS);
            prevImagesBtn.setMaxWidth(Double.MAX_VALUE);
            refreshButton.setMaxWidth(Double.MAX_VALUE);
            nextImagesBtn.setMaxWidth(Double.MAX_VALUE);
            hbox.getChildren().addAll(prevImagesBtn, refreshButton, nextImagesBtn);
            border.setTop(hbox);

            prevImagesBtn.setOnAction(    ev -> onNextImages(false));
            refreshButton.setOnAction(    ev -> onNextImages(null));
            nextImagesBtn.setOnAction(    ev -> onNextImages(true));
            UiInvoker.Deferred.accept(() -> onNextImages(null));
            UiInvoker.Deferred.accept(nextImagesBtn::requestFocus);
        }
        { // center
            canvas = new Canvas(300, 300);
            pane = new Pane(canvas);
            pane.setStyle("-fx-background-color: #00FF00;");
            border.setCenter(pane);
        }

        primaryStage.setScene(scene);
        primaryStage.setMinHeight(125);
        primaryStage.setMinWidth(100);
        primaryStage.setHeight(300);
        primaryStage.setWidth(300);
        primaryStage.setOnCloseRequest(ev -> onDestroy());
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    protected void onDestroy() {
        onCloseImages.run();
        Animator.getSingleton().close();
    }

    @FunctionalInterface
    public interface Proc3Bool{
        void apply(boolean t1, boolean t2, boolean t3);
    }

    void testApp(Supplier<Stream<IImageController2<?,?>>> funcGetImages) {
        List<IImageController2<?,?>> images = funcGetImages.get().collect(Collectors.toList());
        primaryStage.setTitle(td.getTitle(images));
        pane.getChildren().remove(1, pane.getChildren().size());

        List<Canvas> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean isMosaicGameController = images.get(0) instanceof MosaicCanvasController;
        AnimationTimer[] timer = { null };
        boolean[] closed = { false };


        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (isMosaicGameController) // when is this game field...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = td.bl();
                images.forEach(img -> td.changeSettings(img, testTransparent[0]));
            }

            double sizeW = canvas.getWidth();
            double sizeH = canvas.getHeight();
            RectDouble rc = new RectDouble(MARGIN, MARGIN, sizeW - MARGIN * 2, sizeH - MARGIN * 2); // inner rect where drawing images as tiles

            TestDrawing2.CellTilingResult2 ctr = td.cellTiling(rc, images, testTransparent[0]);
            SizeDouble imgSize = ctr.imageSize;
            if (imgSize.width <= 0 || imgSize.height <= 0)
                return;
            if (createImgControls)
                imgControls.clear();

            timer[0] = new AnimationTimer() {

                @Override
                public void handle(long now) {
                    if (closed[0])
                        return;
                    if ((rc.width <= 0) || (rc.height <= 0))
                        return;

                    SizeDouble imgSize = ctr.imageSize;
                    if ((imgSize.width <= 0) || (imgSize.height <= 0))
                        return;

                    GraphicsContext gc = canvas.getGraphicsContext2D();
                  //gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
                    gc.setFill(Cast.toColor(Color.Gray().brighter()));
                    gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

                  //gc.setStroke(Cast.toColor(Color.Black));
                  //gc.setLineWidth(1);
                    gc.strokeRect(rc.x, rc.y, rc.width, rc.height);

                    Function<IImageController2<?,?>, CellTilingInfo> callback = ctr.itemCallback;
                    images.forEach(imgController -> {
                        CellTilingInfo cti = callback.apply(imgController);
                        PointDouble offset = cti.imageOffset;

                        Object imgObj = imgController.getImage();
                        if (imgObj instanceof Canvas) {
                            // none
                        }  else
                        if (imgObj instanceof Image) {
                            Image img = (Image)imgObj;
                            gc.drawImage(img, offset.x, offset.y);
                        } else {
                            throw new IllegalArgumentException("Unsupported image type: " + imgObj.getClass().getName());
                        }
                    });
                }
            };
            timer[0].start();

            Function<IImageController2<?,?>, CellTilingInfo> callback = ctr.itemCallback;
            for (IImageController2<?,?> imgObj : images) {
                TestDrawing2.CellTilingInfo cti = callback.apply(imgObj);
                PointDouble offset = cti.imageOffset;

                if (createImgControls) {
                    Object img = imgObj.getImage();
                    Canvas imgControl = null;
                    if (img instanceof Canvas) {
                        imgControl = (Canvas)img;
                    } else
                    if (img instanceof Image) {
                        // ignore.. - drawed into AnimationTimer
                    } else {
                        throw new IllegalArgumentException("Unsupported image type: " + img.getClass().getName());
                    }

                    // TODO remove unused code
                    if (imgControl != null) {
//                        Canvas imgControl2 = imgControl;
                        Consumer<String> onChangeImage = name -> {
                            if (ImageHelper.PROPERTY_NAME_IMAGE.equals(name)) {
                                //group.repaint();
                                //imgControl2.invalidate();
                            }
                        };

                        imgObj.setListener(onChangeImage);
                        pane.getChildren().add(imgControl);
                    }

                    imgControls.add(ctr.tableSize.width * cti.j + cti.i, imgControl);
                }

                if (resized) {
                    imgObj.getModel().setSize(imgSize);

                    Object img = imgObj.getImage();
                    if (img instanceof Canvas) {
                        Canvas imgCanvas = (Canvas)img;
                        imgCanvas.relocate(offset.x, offset.y);
                    }
                }
            }
        };

        onCellTilingHandler.apply(true, true, true);

        ChangeListener<Number> onSizeWListener = (observable, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            onCellTilingHandler.apply(false, false, true);
        };
        ChangeListener<Number> onSizeHListener = (observable, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            onCellTilingHandler.apply(false, false, true);
        };
        pane. widthProperty().addListener(onSizeWListener);
        pane.heightProperty().addListener(onSizeHListener);

        EventHandler<MouseEvent> mouseHandler = ev -> {
            onCellTilingHandler.apply(true, false, false);
        };
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);

        onCloseImages = () -> {
            closed[0] = true;
            timer[0].stop();
            pane. widthProperty().removeListener(onSizeWListener);
            pane.heightProperty().removeListener(onSizeHListener);
            canvas.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
            images.forEach(IImageController2::close);
            //images.clear(); // unmodifiable list
            //images = null; // not final
        };
    }

    void onNextImages(Boolean isNext) {
        if (onCloseImages != null)
            onCloseImages.run();

        if (isNext != null)
            if (isNext) {
                if (++nextCreateImagesIndex >= onCreateImages.length)
                    nextCreateImagesIndex = 0;
            } else {
                if (--nextCreateImagesIndex < 0)
                    nextCreateImagesIndex = onCreateImages.length - 1;
            }

        onCreateImages[nextCreateImagesIndex].run();
    }

    public static void main(String[] args) {
        ProjSettings.init();
        launch(args);
    }

}
