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
import fmg.core.img.IImageController;
import fmg.core.img.SmileModel.EFaceType;
import fmg.core.img.TestDrawing;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.swing.img.*;
import fmg.swing.mosaic.MosaicJPanelController;

public class DemoApp  {

    private TestDrawing _td;
    private JFrame _frame;
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
        _td = new TestDrawing("Swing");

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

        _frame = new JFrame();
        Container pane = _frame.getContentPane();
        GridLayout grLay = new GridLayout(0, 3);
        JPanel box2 = new JPanel(grLay);
        JButton prevImagesBtn = new JButton("...Previous");
        JButton refresh = new JButton("🗘");
        JButton nextImagesBtn = new JButton("Next...");
        box2.add(prevImagesBtn);
        box2.add(refresh);
        box2.add(nextImagesBtn);
        pane.add(box2, BorderLayout.PAGE_START);

        _jPanel = new JPanel();
        _jPanel.setLayout(null);
//        _jPanel.setBorder(new LineBorder(Color.BLACK));
        pane.add(_jPanel, BorderLayout.CENTER);

        nextImagesBtn.addActionListener(ev -> onNextImages(true));
        prevImagesBtn.addActionListener(ev -> onNextImages(false));
        refresh      .addActionListener(ev -> onNextImages(null));
        SwingUtilities.invokeLater(     () -> onNextImages(null));



        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        _frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                onDestroy();
            }
        });

        SwingUtilities.invokeLater(nextImagesBtn::requestFocus);
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

    void testApp(Supplier<List<IImageController<?,?,?>>> funcGetImages) {
        _jPanel.removeAll();
        List<IImageController<?,?,?>> images = funcGetImages.get();
        _frame.setTitle(_td.getTitle(images));

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
                    Component imgControl = null;
                    if (imgIsControl) {
                        imgControl = (Component)imgObj.getImage();
                    } else {
                        imgControl = new JPanel() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public void paintComponent(Graphics g) {
                              //super.paintComponent(g); // don`t redraw base
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
                            if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE)) {
                                _jPanel.repaint();
                                imgControl2.repaint();
                            }
                        };
                        imgObj.addListener(onChangeImage);
                        binding.put(imgObj, onChangeImage);
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
                if (!imgIsControl)
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
        SwingUtilities.invokeLater(() ->
            new DemoApp().runApp()
        );
    }

}
