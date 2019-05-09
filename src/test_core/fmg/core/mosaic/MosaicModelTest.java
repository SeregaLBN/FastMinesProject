package fmg.core.mosaic;

import java.util.function.Supplier;

import org.junit.*;

import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.SimpleUiThreadLoop;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.PropertyChangeExecutor;
import fmg.common.ui.UiInvoker;
import fmg.core.img.IImageModel;
import fmg.core.types.EMosaic;
import io.reactivex.Flowable;

class DummyImage extends Object {}
class MosaicTestModel extends MosaicDrawModel<DummyImage> {}

public class MosaicModelTest {

    /** double precision */
    static final double P = 0.001;

    static final int TEST_SIZE_W = 456;
    static final int TEST_SIZE_H = 789;

    public static void StaticInitializer() {
        UiInvoker.DEFERRED = SimpleUiThreadLoop::addTask;
    }

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicModelTest::setup");

        StaticInitializer();

        Flowable.just("UI factory inited...").subscribe(LoggerSimple::put);
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }

    @AfterClass
    public static void after() {
        LoggerSimple.put("======================================================");
        LoggerSimple.put("< MosaicModelTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void mosaicGameModelPropertyChangedTest() {
        LoggerSimple.put("> MosaicModelTest::mosaicGameModelPropertyChangedTest");

        MosaicGameModel[] m = { null };
        new PropertyChangeExecutor<>(() -> m[0] = new MosaicGameModel(), false).run(100, 1000,
            model -> {
                Assert.assertTrue(!model.getMatrix().isEmpty());
                Assert.assertTrue(model.getCellAttr() == model.getMatrix().get(0).getAttr()); // reference equals

                model.setSizeField(new Matrisize(15, 10));
            }, (model, modifiedProperties) -> {
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicGameModel.PROPERTY_SIZE_FIELD));
                Assert.assertEquals(1, modifiedProperties.get        (MosaicGameModel.PROPERTY_SIZE_FIELD).first.intValue());
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicGameModel.PROPERTY_MATRIX));
                Assert.assertEquals(1, modifiedProperties.get        (MosaicGameModel.PROPERTY_MATRIX).first.intValue());
                Assert.assertEquals(2, modifiedProperties.size());
            });

        new PropertyChangeExecutor<>(() -> m[0]).run(100, 1000,
            model -> {
                model.setArea(12345);
            }, (model, modifiedProperties) -> {
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicGameModel.PROPERTY_AREA));
                Assert.assertEquals(1, modifiedProperties.get(        MosaicGameModel.PROPERTY_AREA).first.intValue());
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicGameModel.PROPERTY_CELL_ATTR));
                Assert.assertEquals(1, modifiedProperties.get(        MosaicGameModel.PROPERTY_CELL_ATTR).first.intValue());
                Assert.assertEquals(2, modifiedProperties.size());
            });
    }

    @Test
    public void mosaicDrawModelPropertyChangedTest() {
        LoggerSimple.put("> MosaicModelTest::mosaicDrawModelPropertyChangedTest");

        new PropertyChangeExecutor<>(() -> new MosaicTestModel()).run(100, 1000,
            model -> {
                changeModel(model);
            }, (model, modifiedProperties) -> {
                Assert.assertTrue  (                    modifiedProperties.containsKey(IImageModel    .PROPERTY_SIZE            ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        IImageModel    .PROPERTY_SIZE            ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicGameModel.PROPERTY_AREA            ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicGameModel.PROPERTY_AREA            ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicGameModel.PROPERTY_CELL_ATTR       ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicGameModel.PROPERTY_CELL_ATTR       ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicGameModel.PROPERTY_MOSAIC_TYPE     ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicGameModel.PROPERTY_MOSAIC_TYPE     ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicGameModel.PROPERTY_MATRIX          ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicGameModel.PROPERTY_MATRIX          ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicDrawModel.PROPERTY_BACKGROUND_COLOR));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicDrawModel.PROPERTY_BACKGROUND_COLOR).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicDrawModel.PROPERTY_BACKGROUND_FILL ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicDrawModel.PROPERTY_BACKGROUND_FILL ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicDrawModel.PROPERTY_COLOR_TEXT      ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicDrawModel.PROPERTY_COLOR_TEXT      ).first);
                Assert.assertTrue  (                    modifiedProperties.containsKey(MosaicDrawModel.PROPERTY_PEN_BORDER      ));
                Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        MosaicDrawModel.PROPERTY_PEN_BORDER      ).first);
            });
    }

    @Test
    public void mosaicDrawModelAsIsTest() {
        LoggerSimple.put("> MosaicModelTest::mosaicDrawModelAsIsTest");

        new PropertyChangeExecutor<>(MosaicTestModel::new).run(1, 100,
            model -> {
                Assert.assertEquals(EMosaic.eMosaicSquare1, model.getMosaicType());
                Assert.assertEquals(new Matrisize(10, 10), model.getSizeField());
                Assert.assertEquals(model.getCellAttr().getSize(model.getSizeField()), model.getSize());
            }, (model, modifiedProperties) -> { });
    }

    @Test
    public void autoFitTrueCheckAffectsToPaddingTest() {
        LoggerSimple.put("> MosaicModelTest::autoFitTrueCheckAffectsToPaddingTest");

        new PropertyChangeExecutor<>(MosaicTestModel::new).run(1, 100,
            model -> {
                // set property
                model.setAutoFit(true);
                model.setSize(new SizeDouble(1000, 1000));
                model.setPadding(new BoundDouble(100));

                // change property
                model.setSize(new SizeDouble(500, 700));

                // check dependency
                Assert.assertEquals(50.0, model.getPadding().left  , 0);
                Assert.assertEquals(50.0, model.getPadding().right , 0);
                Assert.assertEquals(70.0, model.getPadding().top   , 0);
                Assert.assertEquals(70.0, model.getPadding().bottom, 0);
            }, (model, modifiedProperties) -> { });
    }

    @Test
    public void autoFitTrueCheckAffectsTest() {
        LoggerSimple.put("> MosaicModelTest::autoFitTrueCheckAffectsTest");

        Supplier<MosaicTestModel> createTestModel = () -> {
            MosaicTestModel model = new MosaicTestModel();
            // set property
            model.setAutoFit(true);
            model.setSize(new SizeDouble(1000, 1000));

            // default check
            SizeDouble size = model.getSize();
            Assert.assertEquals(1000, size.width , P);
            Assert.assertEquals(1000, size.height, P);

            SizeDouble mosaicSize = model.getMosaicSize();
            Assert.assertEquals(1000, mosaicSize.width , P);
            Assert.assertEquals(1000, mosaicSize.height, P);

            SizeDouble mosaicOffset = model.getMosaicOffset();
            Assert.assertEquals(0, mosaicOffset.width , P);
            Assert.assertEquals(0, mosaicOffset.height, P);

            BoundDouble padding = model.getPadding();
            Assert.assertEquals(0, padding.left  , P);
            Assert.assertEquals(0, padding.top   , P);
            Assert.assertEquals(0, padding.right , P);
            Assert.assertEquals(0, padding.bottom, P);

            return model;
        };

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(500, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(100, mosaicOffset.width , P);
                Assert.assertEquals(  0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(0, padding.left  , P);
                Assert.assertEquals(0, padding.top   , P);
                Assert.assertEquals(0, padding.right , P);
                Assert.assertEquals(0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setPadding(new BoundDouble(150, 75, 50, 25));
                model.setSize(new SizeDouble(700, 500));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(450, mosaicSize.width , P);
                Assert.assertEquals(450, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(160 , mosaicOffset.width , P);
                Assert.assertEquals(37.5, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(105 , padding.left  , P);
                Assert.assertEquals(37.5, padding.top   , P);
                Assert.assertEquals(35  , padding.right , P);
                Assert.assertEquals(12.5, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(525, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(87.5, mosaicOffset.width , P);
                Assert.assertEquals(   0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(0, padding.left  , P);
                Assert.assertEquals(0, padding.top   , P);
                Assert.assertEquals(0, padding.right , P);
                Assert.assertEquals(0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(350, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(175, mosaicOffset.width , P);
                Assert.assertEquals(  0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(0, padding.left  , P);
                Assert.assertEquals(0, padding.top   , P);
                Assert.assertEquals(0, padding.right , P);
                Assert.assertEquals(0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setPadding(new BoundDouble(150, 75, 50, 25));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(280, mosaicSize.width , P);
                Assert.assertEquals(400, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(260, mosaicOffset.width , P);
                Assert.assertEquals( 75, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(150, padding.left  , P);
                Assert.assertEquals( 75, padding.top   , P);
                Assert.assertEquals( 50, padding.right , P);
                Assert.assertEquals( 25, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setPadding(new BoundDouble(-150, -75, -50, -25));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(420, mosaicSize.width , P);
                Assert.assertEquals(600, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals( 90, mosaicOffset.width , P);
                Assert.assertEquals(-75, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(-150, padding.left  , P);
                Assert.assertEquals(- 75, padding.top   , P);
                Assert.assertEquals(- 50, padding.right , P);
                Assert.assertEquals(- 25, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setPadding(new BoundDouble(-150, -75, -50, -25));
                model.setArea(100);

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(105, size.width , P);
                Assert.assertEquals(150, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(105, mosaicSize.width , P);
                Assert.assertEquals(150, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(0, mosaicOffset.width , P);
                Assert.assertEquals(0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(0, padding.left  , P);
                Assert.assertEquals(0, padding.top   , P);
                Assert.assertEquals(0, padding.right , P);
                Assert.assertEquals(0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setPadding(new BoundDouble(150, 75, 50, 25));
                model.setArea(100);

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(305, size.width , P);
                Assert.assertEquals(250, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(105, mosaicSize.width , P);
                Assert.assertEquals(150, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(150, mosaicOffset.width , P);
                Assert.assertEquals( 75, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(150, padding.left  , P);
                Assert.assertEquals( 75, padding.top   , P);
                Assert.assertEquals( 50, padding.right , P);
                Assert.assertEquals( 25, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setMosaicOffset(new SizeDouble(200, 300));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(1000, size.width , P);
                Assert.assertEquals(1000, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(1000, mosaicSize.width , P);
                Assert.assertEquals(1000, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(200, mosaicOffset.width , P);
                Assert.assertEquals(300, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals( 200, padding.left  , P);
                Assert.assertEquals( 300, padding.top   , P);
                Assert.assertEquals(-200, padding.right , P);
                Assert.assertEquals(-300, padding.bottom, P);
            }, (model, modifiedProperties) -> {});
    }

    @Test
    public void autoFitFalseCheckAffectsTest() {
        LoggerSimple.put("> MosaicModelTest::autoFitFalseCheckAffectsTest");

        Supplier<MosaicTestModel> createTestModel = () -> {
            MosaicTestModel model = new MosaicTestModel();
            // set property
            model.setAutoFit(false);
            model.setSize(new SizeDouble(1000, 1000));

            // default check
            SizeDouble size = model.getSize();
            Assert.assertEquals(1000, size.width , P);
            Assert.assertEquals(1000, size.height, P);

            SizeDouble mosaicSize = model.getMosaicSize();
            Assert.assertEquals(1000, mosaicSize.width , P);
            Assert.assertEquals(1000, mosaicSize.height, P);

            SizeDouble mosaicOffset = model.getMosaicOffset();
            Assert.assertEquals(0, mosaicOffset.width , P);
            Assert.assertEquals(0, mosaicOffset.height, P);

            BoundDouble padding = model.getPadding();
            Assert.assertEquals(0, padding.left  , P);
            Assert.assertEquals(0, padding.top   , P);
            Assert.assertEquals(0, padding.right , P);
            Assert.assertEquals(0, padding.bottom, P);

            return model;
        };

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setMosaicOffset(new SizeDouble(200, 300));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(1000, size.width , P);
                Assert.assertEquals(1000, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(1000, mosaicSize.width , P);
                Assert.assertEquals(1000, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(200, mosaicOffset.width , P);
                Assert.assertEquals(300, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals( 200, padding.left  , P);
                Assert.assertEquals( 300, padding.top   , P);
                Assert.assertEquals(-200, padding.right , P);
                Assert.assertEquals(-300, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setMosaicOffset(new SizeDouble(10, 15));
                model.setSize(new SizeDouble(700, 500));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(500, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(100, mosaicOffset.width , P);
                Assert.assertEquals(  0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(100, padding.left  , P);
                Assert.assertEquals(  0, padding.top   , P);
                Assert.assertEquals(100, padding.right , P);
                Assert.assertEquals(  0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(525, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(87.5, mosaicOffset.width , P);
                Assert.assertEquals(   0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(87.5, padding.left  , P);
                Assert.assertEquals(   0, padding.top   , P);
                Assert.assertEquals(87.5, padding.right , P);
                Assert.assertEquals(   0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(350, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(175, mosaicOffset.width , P);
                Assert.assertEquals(  0, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(175, padding.left  , P);
                Assert.assertEquals(  0, padding.top   , P);
                Assert.assertEquals(175, padding.right , P);
                Assert.assertEquals(  0, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setMosaicOffset(new SizeDouble(-15, -40));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(350, mosaicSize.width , P);
                Assert.assertEquals(500, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(-15, mosaicOffset.width , P);
                Assert.assertEquals(-40, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(-15, padding.left  , P);
                Assert.assertEquals(-40, padding.top   , P);
                Assert.assertEquals(365, padding.right , P);
                Assert.assertEquals( 40, padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setMosaicOffset(new SizeDouble(-15, -40));
                model.setArea(225);

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(157.5, mosaicSize.width , P);
                Assert.assertEquals(225  , mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(-15, mosaicOffset.width , P);
                Assert.assertEquals(-40, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(-15  , padding.left  , P);
                Assert.assertEquals(-40  , padding.top   , P);
                Assert.assertEquals(557.5, padding.right , P);
                Assert.assertEquals(315  , padding.bottom, P);
            }, (model, modifiedProperties) -> {});

        new PropertyChangeExecutor<>(createTestModel).run(1, 100,
            model -> {
                // change property
                model.setSize(new SizeDouble(700, 500));
                model.setMosaicType(EMosaic.eMosaicSquare2);
                model.setSizeField(new Matrisize(10, 15));
                model.setMosaicOffset(new SizeDouble(-15, -40));
                model.setArea(225);
                model.setPadding(new BoundDouble(150, 75, 50, 25));

                // check dependency (evenly expanded)
                SizeDouble size = model.getSize();
                Assert.assertEquals(700, size.width , P);
                Assert.assertEquals(500, size.height, P);

                SizeDouble mosaicSize = model.getMosaicSize();
                Assert.assertEquals(280, mosaicSize.width , P);
                Assert.assertEquals(400, mosaicSize.height, P);

                SizeDouble mosaicOffset = model.getMosaicOffset();
                Assert.assertEquals(260, mosaicOffset.width , P);
                Assert.assertEquals( 75, mosaicOffset.height, P);

                BoundDouble padding = model.getPadding();
                Assert.assertEquals(150, padding.left  , P);
                Assert.assertEquals( 75, padding.top   , P);
                Assert.assertEquals( 50, padding.right , P);
                Assert.assertEquals( 25, padding.bottom, P);
            }, (model, modifiedProperties) -> {});
    }

    static void changeModel(MosaicTestModel m) {
        m.setMosaicType(EMosaic.eMosaicQuadrangle1);
        m.setSizeField(new Matrisize(22, 33));
        m.setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
      //m.setArea(1234);
        m.setPadding(new BoundDouble(10));
        m.setBackgroundColor(Color.DimGray());
        m.getBackgroundFill().setMode(1);
        m.getColorText().setColorClose(1, Color.LightSalmon());
        m.getColorText().setColorOpen(2, Color.MediumSeaGreen());
        m.getPenBorder().setColorLight(Color.MediumPurple());
        m.getPenBorder().setWidth(2);
    }

    @Test
    public void mosaicNoChangedTest() throws InterruptedException {
        LoggerSimple.put("> MosaicModelTest::mosaicNoChangedTest");

        MosaicTestModel[] m = { null };

        // step 1: init
        new PropertyChangeExecutor<>(() -> m[0] = new MosaicTestModel(), false).run(100, 1000,
            model -> {
                SizeDouble size = model.getSize(); // implicit call setter Size
                Assert.assertNotNull(size);
            }, (model, modifiedProperties) -> {
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicDrawModel.PROPERTY_SIZE));
                Assert.assertEquals(1, modifiedProperties.get        (MosaicDrawModel.PROPERTY_SIZE).first.intValue());
                Assert.assertTrue(1 <= modifiedProperties.size());
            });

        // step 2: check no changes
        new PropertyChangeExecutor<>(() -> m[0]).run(100, 1000,
            model -> {
                model.setSize(new SizeDouble(model.getSize()));
                model.setArea(model.getArea());
                model.setSizeField(new Matrisize(model.getSizeField()));
                model.setPadding(model.getPadding()==null ? null : new BoundDouble(model.getPadding()));
            }, (model, modifiedProperties) -> {
                Assert.assertTrue(modifiedProperties.isEmpty());
            });
    }

}
