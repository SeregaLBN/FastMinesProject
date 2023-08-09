package fmg.swing.app;

import static fmg.core.img.PropertyConst.PROPERTY_IMAGE;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;

import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.IImageController2;
import fmg.core.img.MosaicImageController2;
import fmg.core.img.SmileModel2.EFaceType;
import fmg.core.img.TestDrawing2;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.swing.img.*;
import fmg.swing.mosaic.MosaicJPanelController2;

/** live UI test application
 * <p>run from command line
 * <br> <code>

  gradle :FastMines_swing:runDemoApp

 */
public class DemoApp2  {

    private static final int MARGIN = 10; // panel margin - padding to inner images

    private TestDrawing2 td;
    private JFrame frame;
    private JPanel jPanel;
    private Runnable onCloseImages;
    private Runnable[] onCreateImages; // images factory
    private int nextCreateImagesIndex;

    // #region images Fabrica
    public void testMosaicControl() {
        testApp(() -> {
            MosaicJPanelController2 mosaicController = new MosaicJPanelController2();

            if (ThreadLocalRandom.current().nextBoolean()) {
                // unmodified controller test
            } else {
                EMosaic mosaicType = EMosaic.fromOrdinal(ThreadLocalRandom.current().nextInt(EMosaic.values().length));
                ESkillLevel skill  = ESkillLevel.eBeginner;

                var model = mosaicController.getModel();
                model.setMosaicType(mosaicType);
                model.setSizeField(skill.getDefaultSize());
                mosaicController.setCountMines(skill.getNumberMines(mosaicType));
                mosaicController.gameNew();
            }
            return Stream.of(mosaicController);
        });
    }

    public void testMosaicImg() {
        testApp(() ->
             //// test single
             //Stream.of(new MosaicImg2.MosaicAwtImageController() { { getModel().setMosaicType(EMosaic.eMosaicSquare1); }})

             // test all
             Stream.of(EMosaic.values())
                 .map(e -> {
                     MosaicImageController2<?, ?> ctrlr = ThreadLocalRandom.current().nextBoolean()
                         ? new MosaicImg2.MosaicSwingIconController ()
                         : new MosaicImg2.MosaicAwtImageController();
                     ctrlr.getModel().setMosaicType(e);
                     return ctrlr;
                 })
         );
    }
    public void testMosaicGroupImg() {
        testApp(() -> Stream.concat(Stream.of((EMosaicGroup)null),
                                     Stream.of(EMosaicGroup.values()))
                 .map(e -> new Pair<>(new MosaicGroupImg2.MosaicGroupAwtImageController (e),
                                      new MosaicGroupImg2.MosaicGroupSwingIconController(e)))
                 .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testMosaicSkillImg() {
        testApp(() -> Stream.concat(Stream.of((ESkillLevel)null),
                                     Stream.of(ESkillLevel.values()))
                 .map(e -> new Pair<>(new MosaicSkillImg2.MosaicSkillSwingIconController(e),
                                      new MosaicSkillImg2.MosaicSkillAwtImageController (e)))
                 .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testLogo() {
        testApp(() -> Stream.of(new Logo2.LogoSwingIconController()
                              , new Logo2.LogoAwtImageController()
                              , new Logo2.LogoSwingIconController()
                              , new Logo2.LogoAwtImageController()
                              , new Logo2.LogoSwingIconController().asMine()
                              , new Logo2.LogoAwtImageController().asMine()));
    }
    public void testFlag() {
        testApp(() -> Stream.of(new Flag2.FlagSwingIconController()
                              , new Flag2.FlagAwtImageController()));
    }
    public void testSmile() {
        testApp(() -> Stream.of(EFaceType.values())
                            .map(e -> Stream.of(new Smile2.SmileSwingIconController(e),
                                                new Smile2.SmileAwtImageController(e)))
                            .flatMap(x -> x));
    }
    // #endregion

    public void runApp() {
        td = new TestDrawing2("Swing");

        onCreateImages = new Runnable[] {
            this::testMosaicControl,
            this::testMosaicImg,
            this::testMosaicSkillImg,
            this::testMosaicGroupImg,
            this::testSmile,
            this::testLogo,
            this::testFlag
        };

        frame = new JFrame();
        Container pane = frame.getContentPane();
        { // top
            GridLayout grLay = new GridLayout(0, 3);
            JPanel box2 = new JPanel(grLay);
            JButton prevImagesBtn = new JButton("...Previous");
            JButton refreshButton = new JButton("🗘");
            JButton nextImagesBtn = new JButton("Next...");
            box2.add(prevImagesBtn);
            box2.add(refreshButton);
            box2.add(nextImagesBtn);
            pane.add(box2, BorderLayout.PAGE_START);

            prevImagesBtn.addActionListener(ev -> onNextImages(false));
            refreshButton.addActionListener(ev -> onNextImages(null));
            nextImagesBtn.addActionListener(ev -> onNextImages(true));
            SwingUtilities.invokeLater(     () -> onNextImages(null));
            SwingUtilities.invokeLater(nextImagesBtn::requestFocus);
        }
        { // center
            jPanel = new JPanel();
            jPanel.setLayout(null);
    //        jPanel.setBorder(new LineBorder(Color.BLACK));
            pane.add(jPanel, BorderLayout.CENTER);
        }

        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                onDestroy();
            }
        });

        frame.setPreferredSize(new Dimension(300, 300));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    protected void onDestroy() {
        onCloseImages.run();
        frame.dispose();
        Animator.getSingleton().close();
    }

    @FunctionalInterface
    public interface Proc3Bool{
        void apply(boolean t1, boolean t2, boolean t3);
    }

    void testApp(Supplier<Stream<IImageController2<?,?>>> funcGetImages) {
        List<IImageController2<?,?>> images = funcGetImages.get().collect(Collectors.toList());
        frame.setTitle(td.getTitle(images));
        jPanel.removeAll();

        List<Component> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean isMosaicGameController = false; // images.get(0) instanceof MosaicJPanelController2;

        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (isMosaicGameController) // when is this game field...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = td.bl();
                images.forEach(img -> td.changeSettings(img, testTransparent[0]));
            }

            double sizeW = jPanel.getWidth();
            double sizeH = jPanel.getHeight();
            RectDouble rc = new RectDouble(MARGIN, MARGIN, sizeW - MARGIN * 2, sizeH - MARGIN * 2); // inner rect where drawing images as tiles

            TestDrawing2.CellTilingResult2 ctr = td.cellTiling(rc, images, testTransparent[0]);
            SizeDouble imgSize = ctr.imageSize;
            if (imgSize.width <= 0 || imgSize.height <= 0)
                return;
            if (createImgControls)
                imgControls.clear();

            Function<IImageController2<?,?>, TestDrawing2.CellTilingInfo> callback = ctr.itemCallback;
            for (IImageController2<?,?> imgObj : images) {
                TestDrawing2.CellTilingInfo cti = callback.apply(imgObj);
                PointDouble offset = cti.imageOffset;

                if (createImgControls) {
                    Object img = imgObj.getImage();
                    Component imgControl = null;
                    if (img instanceof Component) {
                        imgControl = (Component)img;
                    } else
                    if ((img instanceof Icon) ||
                        (img instanceof Image))
                    {
                        imgControl = new JPanel() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public void paintComponent(Graphics g) {
                              //super.paintComponent(g); // don`t redraw base
                                Object img2 = imgObj.getImage(); // reload image
                                if (img2 instanceof Icon) {
                                    Icon ico = (Icon)img2;
                                    //ico = ImgUtils.zoom(ico, imgSize.width, imgSize.height);
                                    ico.paintIcon(null, g, 0, 0);
                                } else
                                if (img2 instanceof Image) {
                                    Image image = (Image)img2;
                                    //image = ImgUtils.zoom(image, imgSize.width, imgSize.height);
                                    g.drawImage(image, 0, 0, null);
                                } else {
                                    throw new IllegalArgumentException("Unsupported image type: " + img2.getClass().getName());
                                }
                            }
                        };
                      //imgControl.setBackgroundColor(Cast.toColor(Color.RandomColor().brighter()));

                        Component imgControl2 = imgControl;
                        Consumer<String> onChangeImageAsync = property -> {
                            if (PROPERTY_IMAGE.equals(property)) {
                                jPanel.repaint();
                                imgControl2.repaint();
                            }
                        };
                        Consumer<String> onChangeImage = property -> UiInvoker.Deferred.accept(() -> onChangeImageAsync.accept(property));
                                jPanel.repaint();
                        imgObj.setListener(onChangeImage);
                    } else {
                        throw new IllegalArgumentException("Unsupported image type: " + img.getClass().getName());
                    }

                    jPanel.add(imgControl);
//                    ((JPanel)imgControl).setBorder(new LineBorder(Color.RED));
                    imgControls.add(ctr.tableSize.width * cti.j + cti.i, imgControl);
                }

                if (resized) {
                    imgObj.getModel().setSize(imgSize);
                    Component imgControl = imgControls.get(ctr.tableSize.width * cti.j + cti.i);
                    Rectangle bound = imgControl.getBounds();
                    bound.x      = (int)offset.x;
                    bound.y      = (int)offset.y;
                    bound.width  = (int)imgSize.width;
                    bound.height = (int)imgSize.height;
                    imgControl.setBounds(bound);
                }
            }
        };

        onCellTilingHandler.apply(true, true, true);


        ComponentListener onSizeChanged = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                onCellTilingHandler.apply(false, false, true);
            }
        };
        jPanel.addComponentListener(onSizeChanged);

        MouseListener onMousePressed = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onCellTilingHandler.apply(true, false, false);
            }
        };
        jPanel.addMouseListener(onMousePressed);

        onCloseImages = () -> {
            jPanel.removeComponentListener(onSizeChanged);
            jPanel.removeMouseListener(onMousePressed);
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
        SwingUtilities.invokeLater(() ->
            new DemoApp2().runApp()
        );
    }

}
