package fmg.swing.app;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;

import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.SmileModel.EFaceType;
import fmg.core.img.TestDrawing;
import fmg.core.mosaic.MosaicImageController;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.swing.img.*;
import fmg.swing.mosaic.MosaicJPanelController;
import fmg.swing.utils.ProjSettings;

/** live UI test application
 * <p>run from command line
 * <br> <code>

  gradle :FastMines_swing:runDemoApp

 */
public class DemoApp  {

    private static final int MARGIN = 10; // panel margin - padding to inner images

    private TestDrawing _td;
    private JFrame _frame;
    private JPanel _jPanel;
    private Runnable _onCloseImages;
    private Runnable[] _onCreateImages; // images factory
    private int _nextCreateImagesIndex;

    // #region images Fabrica
    public void testMosaicControl () {
        MosaicView._DEBUG_DRAW_FLOW = true;
        testApp(() -> {
            MosaicJPanelController mosaicController = new MosaicJPanelController();

            if (ThreadLocalRandom.current().nextBoolean()) {
                // unmodified controller test
            } else {
                EMosaic mosaicType = EMosaic.fromOrdinal(ThreadLocalRandom.current().nextInt(EMosaic.values().length));
                ESkillLevel skill  = ESkillLevel.eBeginner;

                mosaicController.setMosaicType(mosaicType);
                mosaicController.setSizeField(skill.getDefaultSize());
                mosaicController.setMinesCount(skill.getNumberMines(mosaicType));
                mosaicController.gameNew();
            }
            return Stream.of(mosaicController);
        });
    }

    public void testMosaicImg     () {
        testApp(() ->
             //// test single
             //Stream.of(new MosaicImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})

             // test all
             Stream.of(EMosaic.values())

             //// variant 1
             //.map(e -> Stream.of(new MosaicImg.ControllerIcon (),
             //                    new MosaicImg.ControllerImage())
             //                .peek(ctrlr -> ctrlr.setMosaicType(e)))
             //.flatMap(x -> x)

             // variant 2
             .map(e -> {
                     MosaicImageController<?, ?> ctrlr = ThreadLocalRandom.current().nextBoolean()
                         ? new MosaicImg.IconController ()
                         : new MosaicImg.ImageAwtController();
                     ctrlr.setMosaicType(e);
                     return ctrlr;
                 }));
    }
    public void testMosaicGroupImg() {
        testApp(() -> Stream.concat(Stream.of((EMosaicGroup)null),
                                     Stream.of(EMosaicGroup.values()))
                 .map(e -> new Pair<>(new MosaicGroupImg.IconController (e),
                                      new MosaicGroupImg.ImageAwtController(e)))
                 .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testMosaicSkillImg() {
        testApp(() -> Stream.concat(Stream.of((ESkillLevel)null),
                                     Stream.of(ESkillLevel.values()))
                 .map(e -> new Pair<>(new MosaicSkillImg.IconController (e),
                                      new MosaicSkillImg.ImageAwtController(e)))
                 .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testLogo() {
        testApp(() -> Stream.of(new Logo.IconController()
                              , new Logo.ImageAwtController()
                              , new Logo.IconController()
                              , new Logo.ImageAwtController()));
    }
    public void testMine() {
        testApp(() -> Stream.of(new Mine.IconController()
                              , new Mine.ImageAwtController()
                              , new Mine.IconController()
                              , new Mine.ImageAwtController()));
    }
    public void testFlag() {
        testApp(() -> Stream.of(new Flag.IconController()
                              , new Flag.ImageAwtController()));
    }
    public void testSmile() {
        testApp(() -> Stream.of(EFaceType.values())
                            .map(e -> Stream.of(new Smile.IconController(e),
                                                new Smile.ImageAwtController(e)))
                            .flatMap(x -> x));
    }
    // #endregion

    public void runApp() {
        _td = new TestDrawing("Swing");

        _onCreateImages = new Runnable[] {
            this::testMosaicControl,
            this::testMosaicImg,
            this::testMosaicSkillImg,
            this::testMosaicGroupImg,
            this::testSmile,
            this::testLogo,
            this::testMine,
            this::testFlag
        };

        _frame = new JFrame();
        Container pane = _frame.getContentPane();
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
            _jPanel = new JPanel();
            _jPanel.setLayout(null);
    //        _jPanel.setBorder(new LineBorder(Color.BLACK));
            pane.add(_jPanel, BorderLayout.CENTER);
        }

        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        _frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                onDestroy();
            }
        });

        _frame.setPreferredSize(new Dimension(300, 300));
        _frame.setLocationRelativeTo(null);
        _frame.pack();
        _frame.setVisible(true);
    }

    protected void onDestroy() {
        _onCloseImages.run();
        _frame.dispose();
        Animator.getSingleton().close();
    }

    @FunctionalInterface
    public interface Proc3Bool{
        void apply(boolean t1, boolean t2, boolean t3);
    }

    void testApp(Supplier<Stream<IImageController<?,?,?>>> funcGetImages) {
        List<IImageController<?,?,?>> images = funcGetImages.get().collect(Collectors.toList());
        _frame.setTitle(_td.getTitle(images));
        _jPanel.removeAll();

        List<Component> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean isMosaicGameController = images.get(0) instanceof MosaicJPanelController;
        Map<IImageController<?,?,?>, PropertyChangeListener> binding = new HashMap<>();

        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (isMosaicGameController) // when is this game field...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = _td.bl();
                images.forEach(img -> _td.applySettings(img, testTransparent[0]));
            }

            double sizeW = _jPanel.getWidth();
            double sizeH = _jPanel.getHeight();
            RectDouble rc = new RectDouble(MARGIN, MARGIN, sizeW - MARGIN * 2, sizeH - MARGIN * 2); // inner rect where drawing images as tiles

            TestDrawing.CellTilingResult ctr = _td.cellTiling(rc, images, testTransparent[0]);
            SizeDouble imgSize = ctr.imageSize;
            if (imgSize.width <= 0 || imgSize.height <= 0)
                return;
            if (createImgControls)
                imgControls.clear();

            Function<IImageController<?,?,?>, TestDrawing.CellTilingInfo> callback = ctr.itemCallback;
            for (IImageController<?,?,?> imgObj : images) {
                TestDrawing.CellTilingInfo cti = callback.apply(imgObj);
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
                        PropertyChangeListener onChangeImage = ev -> {
                            if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE)) {
                                _jPanel.repaint();
                                imgControl2.repaint();
                            }
                        };
                        imgObj.addListener(onChangeImage);
                        binding.put(imgObj, onChangeImage);
                    } else {
                        throw new IllegalArgumentException("Unsupported image type: " + img.getClass().getName());
                    }

                    _jPanel.add(imgControl);
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
        _jPanel.addComponentListener(onSizeChanged);

        MouseListener onMousePressed = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onCellTilingHandler.apply(true, false, false);
            }
        };
        _jPanel.addMouseListener(onMousePressed);

        _onCloseImages = () -> {
            _jPanel.removeComponentListener(onSizeChanged);
            _jPanel.removeMouseListener(onMousePressed);
            images.forEach(imgObj -> {
                if (binding.containsKey(imgObj))
                    imgObj.removeListener(binding.get(imgObj));
                imgObj.close();
            });
            //images.clear(); // unmodifiable list
            //images = null; // not final
        };
    }

    void onNextImages(Boolean isNext) {
        if (_onCloseImages != null)
            _onCloseImages.run();

        if (isNext != null)
            if (isNext) {
                if (++_nextCreateImagesIndex >= _onCreateImages.length)
                    _nextCreateImagesIndex = 0;
            } else {
                if (--_nextCreateImagesIndex < 0)
                    _nextCreateImagesIndex = _onCreateImages.length - 1;
            }

        _onCreateImages[_nextCreateImagesIndex].run();
    }

    public static void main(String[] args) {
        ProjSettings.init();
        SwingUtilities.invokeLater(() ->
            new DemoApp().runApp()
        );
    }

}
