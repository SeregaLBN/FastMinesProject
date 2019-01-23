package fmg.swing.app;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
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
import fmg.core.img.ATestDrawing;
import fmg.core.img.IImageController;
import fmg.core.img.SmileModel.EFaceType;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.swing.img.*;
import fmg.swing.mosaic.MosaicJPanelController;

public class DemoApp extends JFrame {
    private static final long serialVersionUID = 1L;

    class TestDrawing extends ATestDrawing {
        TestDrawing() { super("Swing"); }
    }

    private TestDrawing _td;
    private JPanel _jPanel;
    private static final int margin = 10; // panel margin - padding to inner images
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
                EMosaic mosaicType = EMosaic.eMosaicTrSq1;
                ESkillLevel skill  = ESkillLevel.eBeginner;

                mosaicController.setArea(1500);
                mosaicController.setMosaicType(mosaicType);
                mosaicController.setSizeField(skill.getDefaultSize());
                mosaicController.setMinesCount(skill.getNumberMines(mosaicType));
                mosaicController.gameNew();
            }
            return Arrays.asList(mosaicController);

        }
    );}

    public void testMosaicImg     () { testApp(() ->
                                               //  // test single
                                               //  Arrays.asList(new MosaicImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})

                                                 // test all
                                                 Stream.of(EMosaic.values())

                                               //         // variant 1
                                               //         .map(e -> Stream.of(new MosaicImg.ControllerIcon () { { setMosaicType(e); }},
                                               //                             new MosaicImg.ControllerImage() { { setMosaicType(e); }}))
                                               //         .flatMap(x -> x)

                                                        // variant 2
                                                        .map(e -> ThreadLocalRandom.current().nextBoolean()
                                                                    ? new MosaicImg.ControllerIcon () { { setMosaicType(e); }}
                                                                    : new MosaicImg.ControllerImage() { { setMosaicType(e); }}
                                                            )
                                                        .collect(Collectors.toList())
                                     ); }
    public void testMosaicGroupImg() { testApp(() -> Stream.concat(Stream.of((EMosaicGroup)null),
                                                                   Stream.of(EMosaicGroup.values()))
                                               .map(e -> new Pair<>(new MosaicGroupImg.ControllerIcon (e),
                                                                    new MosaicGroupImg.ControllerImage(e)))
                                               .flatMap(x -> Stream.of(x.first, x.second))
                                               .collect(Collectors.toList())
                                     ); }
    public void testMosaicSkillImg() { testApp(() -> Stream.concat(Stream.of((ESkillLevel)null),
                                                                   Stream.of(ESkillLevel.values()))
                                               .map(e -> new Pair<>(new MosaicSkillImg.ControllerIcon (e),
                                                                    new MosaicSkillImg.ControllerImage(e)))
                                               .flatMap(x -> Stream.of(x.first, x.second))
                                               .collect(Collectors.toList())
                                     ); }
    public void testLogos         () { testApp(() -> Arrays.asList(new Logo.ControllerIcon()
                                                                 , new Logo.ControllerImage()
                                                                 , new Logo.ControllerIcon()
                                                                 , new Logo.ControllerImage())); }
    public void testMines         () { testApp(() -> Arrays.asList(new Mine.ControllerIcon()
                                                                 , new Mine.ControllerImage()
                                                                 , new Mine.ControllerIcon()
                                                                 , new Mine.ControllerImage())); }
    public void testFlags         () { testApp(() -> Arrays.asList(new Flag.ControllerIcon()
                                                                 , new Flag.ControllerImage())); }
    public void testSmiles        () { testApp(() -> Arrays.asList(EFaceType.values()).stream()
                                               .map(e -> Stream.of(new Smile.ControllerIcon(e),
                                                                   new Smile.ControllerImage(e)))
                                               .flatMap(x -> x)
                                               .collect(Collectors.toList())); }
    // #endregion

    public void runApp() {
        _td = new TestDrawing();

        _onCreateImages = new Runnable[] {
            this::testMosaicControl,
            this::testMosaicImg,
            this::testMosaicSkillImg,
            this::testMosaicGroupImg,
            this::testSmiles,
            this::testLogos,
            this::testMines,
            this::testFlags
        };

        Box box = Box.createVerticalBox();

        JButton nextImagesBtn = new JButton("Next...");
        //nextImagesBtn.setMargin(new Insets(margin, margin, 2, margin));
        nextImagesBtn.addActionListener(ev -> onNextImages());
        box.add(nextImagesBtn);

        box.add(Box.createVerticalStrut(5));

        _jPanel = new JPanel();
        _jPanel.setLayout(null);
//        _jPanel.setBorder(new LineBorder(Color.BLACK));
        box.add(_jPanel);
        add(box);

        SwingUtilities.invokeLater(this::onNextImages);



        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                onDestroy();
            }
        });

        setPreferredSize(new Dimension(300, 300));
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    protected void onDestroy() {
        _onCloseImages.run();
        dispose();
        Animator.getSingleton().close();
    }

    @FunctionalInterface
    public interface Proc3Bool{
        void apply(boolean t1, boolean t2, boolean t3);
    }

    void testApp(Supplier<List<IImageController<?,?,?>>> funcGetImages) {
        _jPanel.removeAll();
        List<IImageController<?,?,?>> images = funcGetImages.get();
        setTitle(_td.getTitle(images));

        List<Component> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean imgIsControl = images.get(0).getImage() instanceof Component;
        Map<IImageController<?,?,?>, PropertyChangeListener> binding = imgIsControl ? null : new HashMap<>(images.size());

        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (images.size() == 1)     // if one image...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = _td.bl();
                images.forEach(img -> _td.applySettings(img, testTransparent[0]));
            }

            double sizeW = _jPanel.getWidth();
            double sizeH = _jPanel.getHeight();
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult ctr = _td.cellTiling(rc, images, testTransparent[0]);
            SizeDouble imgSize = ctr.imageSize;
            if (createImgControls)
                imgControls.clear();

            Function<IImageController<?,?,?>, ATestDrawing.CellTilingInfo> callback = ctr.itemCallback;
            for (IImageController<?,?,?> imgObj : images) {
                ATestDrawing.CellTilingInfo cti = callback.apply(imgObj);
                PointDouble offset = cti.imageOffset;

                if (createImgControls) {
                    Component imgControl = null;
                    if (imgIsControl) {
                        imgControl = (Component)imgObj.getImage();
                    } else {
                        imgControl = new JPanel() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public void paintComponent(Graphics g) {
                                //super.paintComponent(g);
                                Object image = imgObj.getImage();
                                if (image instanceof Icon) {
                                    Icon ico = (Icon)image;
                                    //ico = ImgUtils.zoom(ico, imgSize.width, imgSize.height);
                                    ico.paintIcon(null, g, 0, 0);
                                } else
                                if (image instanceof Image) {
                                    Image img = (Image)image;
                                    //img = ImgUtils.zoom(img, imgSize.width, imgSize.height);
                                    g.drawImage(img, 0, 0, null);
                                } else
                                    throw new IllegalArgumentException("Not supported image type is " + image.getClass().getName());
                            }
                        };
                      //imgControl.setBackgroundColor(Cast.toColor(Color.RandomColor().brighter()));

                        Component imgControl2 = imgControl;
                        PropertyChangeListener onChangeImage = ev -> {
                            if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE))
                                imgControl2.invalidate();
                        };
                        imgObj.addListener(onChangeImage);
                        binding.put(imgObj, onChangeImage);
                    }

                    _jPanel.add(imgControl);
//                    ((JPanel)imgControl).setBorder(new LineBorder(Color.RED));
                    imgControls.add(ctr.tableSize.width * cti.j + cti.i, imgControl);
//                    resized = true; // to set real values to imgControl.setBounds
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
                if (!imgIsControl)
                    imgObj.removeListener(binding.get(imgObj));
                imgObj.close();
            });
            //images.clear(); // unmodifiable list
            //images = null; // not final
        };
    }

    void onNextImages() {
        if (_onCloseImages != null)
            _onCloseImages.run();

        Runnable onCreate = _onCreateImages[_nextCreateImagesIndex];
        if (++_nextCreateImagesIndex >= _onCreateImages.length)
            _nextCreateImagesIndex = 0;
        onCreate.run();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new DemoApp().runApp()
        );
    }

}
