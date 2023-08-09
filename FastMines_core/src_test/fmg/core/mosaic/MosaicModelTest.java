package fmg.core.mosaic;

import static fmg.core.img.PropertyConst.PROPERTY_AREA;
import static fmg.core.img.PropertyConst.PROPERTY_BACKGROUND_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_CELL_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_COLOR_LIGHT;
import static fmg.core.img.PropertyConst.PROPERTY_FILL_MODE;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_TYPE;
import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_PEN_BORDER;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE_FIELD;
import static fmg.core.img.PropertyConst.PROPERTY_WIDTH;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.*;

import fmg.common.Color;
import fmg.common.Logger;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.types.EMosaic;

public class MosaicModelTest {

    static class DummyImage extends Object {}

    /** double precision */
    static final double P = 0.001;

    static final int TEST_SIZE_W = 456;
    static final int TEST_SIZE_H = 789;


    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicModelTest::setup");
    }

    @Before
    public void before() {
        Logger.info("======================================================");
    }

    @AfterClass
    public static void after() {
        Logger.info("======================================================");
        Logger.info("< MosaicModelTest closed");
        Logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void mosaicGameModelPropertyChangedTest() {
        Logger.info("> MosaicModelTest::mosaicGameModelPropertyChangedTest");

        var model = new MosaicModel2(false);

        var modifiedProperties = new HashMap<String, Integer>();
        model.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        Assert.assertTrue(!model.getMatrix().isEmpty());
        Assert.assertSame(model.getShape(), model.getMatrix().get(0).getShape()); // reference equals

        model.setSizeField(new Matrisize(15, 10));

        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SIZE_FIELD));
        Assert.assertEquals(1, modifiedProperties.get        (PROPERTY_SIZE_FIELD).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_AREA));
        Assert.assertEquals(1, modifiedProperties.get        (PROPERTY_AREA).intValue());
        Assert.assertEquals(2, modifiedProperties.size());

        ////////////////////

        modifiedProperties.clear();
        model.setArea(12345);
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_AREA));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_AREA).intValue());
        Assert.assertEquals(1, modifiedProperties.size());
    }

    @Test
    public void mosaicDrawModelPropertyChangedTest() {
        Logger.info("> MosaicModelTest::mosaicDrawModelPropertyChangedTest");

        var model = new MosaicModel2(false);

        var modifiedProperties = new HashMap<String, Integer>();
        model.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        changeModel(model);

        var checkChanged = Map.of(PROPERTY_MOSAIC_TYPE, 1
                                , PROPERTY_SIZE_FIELD, 1
                                , PROPERTY_SIZE      , 1
                                , PROPERTY_AREA      , 4
                                , PROPERTY_PADDING   , 1
                                , PROPERTY_BACKGROUND_COLOR  , 1
                                , PROPERTY_CELL_COLOR, 1
                                , PROPERTY_FILL_MODE , 1
                                , PROPERTY_PEN_BORDER + '.' + PROPERTY_WIDTH, 1
                                , PROPERTY_PEN_BORDER + '.' + PROPERTY_COLOR_LIGHT, 1);
        Assert.assertEquals(checkChanged.size(), modifiedProperties.size());
        for (var kv : checkChanged.entrySet()) {
            var name = kv.getKey();
            var value = kv.getValue();
            Assert.assertTrue(modifiedProperties.containsKey(name));
            Assert.assertEquals(name, value, modifiedProperties.get(name));
        }
    }

    @Test
    public void mosaicDrawModelAsIsTest() {
        Logger.info("> MosaicModelTest::mosaicDrawModelAsIsTest");

        var model = new MosaicModel2(false);
        Assert.assertEquals(EMosaic.eMosaicSquare1, model.getMosaicType());
        Assert.assertEquals(new Matrisize(10, 10), model.getSizeField());
        Assert.assertEquals(model.getShape().getSize(model.getSizeField()), model.getSize());
    }

    @Test
    public void imageModeCheckAffectsToPaddingTest() {
        Logger.info("> MosaicModelTest::autoFitTrueCheckAffectsToPaddingTest");

        var model = new MosaicModel2(false);
        model.setSize(new SizeDouble(1000, 1000));
        model.setPadding(new BoundDouble(100));

        // change property
        model.setSize(new SizeDouble(500, 700));

        // check dependency
        Assert.assertEquals(50.0, model.getPadding().left  , 0);
        Assert.assertEquals(50.0, model.getPadding().right , 0);
        Assert.assertEquals(70.0, model.getPadding().top   , 0);
        Assert.assertEquals(70.0, model.getPadding().bottom, 0);
    }

    @Test
    public void controlModeCheckAffectsTest() {
        Logger.info("> MosaicModelTest::autoFitTrueCheckAffectsTest");

        Supplier<MosaicModel2> createTestModel = () -> {
            var model = new MosaicModel2(true);
            model.setSize(new SizeDouble(1000, 1000));

            // default check
            SizeDouble size = model.getSize();
            Assert.assertEquals(1000, size.width , P);
            Assert.assertEquals(1000, size.height, P);

            SizeDouble mosaicSize = model.getSizeMosaic();
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

        ////////////////////////////////////////////////
        var model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));

        SizeDouble size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        SizeDouble mosaicSize = model.getSizeMosaic();
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

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setPadding(new BoundDouble(150, 75, 50, 25));
        model.setSize(new SizeDouble(700, 500));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(500, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(100, mosaicOffset.width , P);
        Assert.assertEquals(  0, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(0, padding.left  , P);
        Assert.assertEquals(0, padding.top   , P);
        Assert.assertEquals(0, padding.right , P);
        Assert.assertEquals(0, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(525, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(87.5, mosaicOffset.width , P);
        Assert.assertEquals(   0, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(0, padding.left  , P);
        Assert.assertEquals(0, padding.top   , P);
        Assert.assertEquals(0, padding.right , P);
        Assert.assertEquals(0, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(350, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(175, mosaicOffset.width , P);
        Assert.assertEquals(  0, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(0, padding.left  , P);
        Assert.assertEquals(0, padding.top   , P);
        Assert.assertEquals(0, padding.right , P);
        Assert.assertEquals(0, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setPadding(new BoundDouble(150, 75, 50, 25));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(280, mosaicSize.width , P);
        Assert.assertEquals(400, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(260, mosaicOffset.width , P);
        Assert.assertEquals( 75, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(150, padding.left  , P);
        Assert.assertEquals( 75, padding.top   , P);
        Assert.assertEquals( 50, padding.right , P);
        Assert.assertEquals( 25, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setPadding(new BoundDouble(-150, -75, -50, -25));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(420, mosaicSize.width , P);
        Assert.assertEquals(600, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals( 90, mosaicOffset.width , P);
        Assert.assertEquals(-75, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(-150, padding.left  , P);
        Assert.assertEquals(- 75, padding.top   , P);
        Assert.assertEquals(- 50, padding.right , P);
        Assert.assertEquals(- 25, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setPadding(new BoundDouble(-150, -75, -50, -25));
        model.setArea(100);

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(105, mosaicSize.width , P);
        Assert.assertEquals(150, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(247.50, mosaicOffset.width , P);
        Assert.assertEquals(150   , mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(-150, padding.left  , P);
        Assert.assertEquals(-75 , padding.top   , P);
        Assert.assertEquals(-50 , padding.right , P);
        Assert.assertEquals(-25 , padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setPadding(new BoundDouble(150, 75, 50, 25));
        model.setArea(100);

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(105, mosaicSize.width , P);
        Assert.assertEquals(150, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(347.50, mosaicOffset.width , P);
        Assert.assertEquals(200   , mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(150, padding.left  , P);
        Assert.assertEquals( 75, padding.top   , P);
        Assert.assertEquals( 50, padding.right , P);
        Assert.assertEquals( 25, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        model.setMosaicOffset(new SizeDouble(200, 300));

        size = model.getSize();
        Assert.assertEquals(1000, size.width , P);
        Assert.assertEquals(1000, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(1000, mosaicSize.width , P);
        Assert.assertEquals(1000, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(200, mosaicOffset.width , P);
        Assert.assertEquals(300, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals( 200, padding.left  , P);
        Assert.assertEquals( 300, padding.top   , P);
        Assert.assertEquals(-200, padding.right , P);
        Assert.assertEquals(-300, padding.bottom, P);
    }

    @Test
    public void imageModeCheckAffectsTest() {
        Logger.info("> MosaicModelTest::autoFitFalseCheckAffectsTest");

        Supplier<MosaicModel2> createTestModel = () -> {
            var model = new MosaicModel2(false);
            model.setSize(new SizeDouble(1000, 1000));

            // default check
            SizeDouble size = model.getSize();
            Assert.assertEquals(1000, size.width , P);
            Assert.assertEquals(1000, size.height, P);

            SizeDouble mosaicSize = model.getSizeMosaic();
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

        var model = createTestModel.get();
        // change property
        model.setMosaicOffset(new SizeDouble(200, 300));

        SizeDouble size = model.getSize();
        Assert.assertEquals(1000, size.width , P);
        Assert.assertEquals(1000, size.height, P);

        SizeDouble mosaicSize = model.getSizeMosaic();
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

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setMosaicOffset(new SizeDouble(10, 15));
        model.setSize(new SizeDouble(700, 500));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(500, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(107, mosaicOffset.width , P);
        Assert.assertEquals(7.5, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals( 7  , padding.left  , P);
        Assert.assertEquals( 7.5, padding.top   , P);
        Assert.assertEquals(-7  , padding.right , P);
        Assert.assertEquals(-7.5, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(525, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(87.5, mosaicOffset.width , P);
        Assert.assertEquals(   0, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(0, padding.left  , P);
        Assert.assertEquals(0, padding.top   , P);
        Assert.assertEquals(0, padding.right , P);
        Assert.assertEquals(0, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(350, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(175, mosaicOffset.width , P);
        Assert.assertEquals(  0, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(0, padding.left  , P);
        Assert.assertEquals(0, padding.top   , P);
        Assert.assertEquals(0, padding.right , P);
        Assert.assertEquals(0, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setMosaicOffset(new SizeDouble(-15, -40));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(350, mosaicSize.width , P);
        Assert.assertEquals(500, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(-15, mosaicOffset.width , P);
        Assert.assertEquals(-40, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(-190, padding.left  , P);
        Assert.assertEquals( -40, padding.top   , P);
        Assert.assertEquals( 190, padding.right , P);
        Assert.assertEquals(  40, padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setMosaicOffset(new SizeDouble(-15, -40));
        model.setArea(225);

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(157.5, mosaicSize.width , P);
        Assert.assertEquals(225  , mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(81.25, mosaicOffset.width , P);
        Assert.assertEquals(97.50, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(-190, padding.left  , P);
        Assert.assertEquals(-40 , padding.top   , P);
        Assert.assertEquals( 190, padding.right , P);
        Assert.assertEquals( 40 , padding.bottom, P);

        ////////////////////////////////////////////////
        model = createTestModel.get();
        // change property
        model.setSize(new SizeDouble(700, 500));
        model.setMosaicType(EMosaic.eMosaicSquare2);
        model.setSizeField(new Matrisize(10, 15));
        model.setMosaicOffset(new SizeDouble(-15, -40));
        model.setArea(225);
        model.setPadding(new BoundDouble(150, 75, 50, 25));

        size = model.getSize();
        Assert.assertEquals(700, size.width , P);
        Assert.assertEquals(500, size.height, P);

        mosaicSize = model.getSizeMosaic();
        Assert.assertEquals(280, mosaicSize.width , P);
        Assert.assertEquals(400, mosaicSize.height, P);

        mosaicOffset = model.getMosaicOffset();
        Assert.assertEquals(260, mosaicOffset.width , P);
        Assert.assertEquals( 75, mosaicOffset.height, P);

        padding = model.getPadding();
        Assert.assertEquals(150, padding.left  , P);
        Assert.assertEquals( 75, padding.top   , P);
        Assert.assertEquals( 50, padding.right , P);
        Assert.assertEquals( 25, padding.bottom, P);
    }

    static void changeModel(MosaicModel2 m) {
        m.setMosaicType(EMosaic.eMosaicQuadrangle1);
        m.setSizeField(new Matrisize(22, 33));
        m.setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
      //m.setArea(1234);
        m.setPadding(new BoundDouble(10));
        m.setBackgroundColor(Color.DimGray());
        m.setCellColor(Color.Gray());
        m.setFillMode(1);
        m.getPenBorder().setColorLight(Color.MediumPurple());
        m.getPenBorder().setWidth(2);
    }

    @Test
    public void mosaicNoChangedTest()  {
        Logger.info("> MosaicModelTest::mosaicNoChangedTest");

        Consumer<Boolean> func = isControlMode -> {
            var model = new MosaicModel2(isControlMode);

            var modifiedProperties = new HashMap<String, Integer>();
            model.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

            // check no changes
            model.setSize(new SizeDouble(model.getSize()));
            model.setArea(model.getArea());
            model.setSizeField(new Matrisize(model.getSizeField()));
            model.setPadding(model.getPadding()==null ? null : new BoundDouble(model.getPadding()));
            Assert.assertTrue(modifiedProperties.isEmpty());
        };

        func.accept(true);
        func.accept(false);
    }

    @Test
    public void noChangeOffsetTest() {
        Consumer<Boolean> func = isControlMode -> {
            var model = new MosaicModel2(isControlMode);

            // change property
            SizeDouble size = new SizeDouble(model.getSize());
            size.width *= 2;
            model.setSize(new SizeDouble(size));

            // getsome properties
            BoundDouble pad   = new BoundDouble(model.getPadding());
            SizeDouble offset = new SizeDouble(model.getMosaicOffset());

            // try facked change
            model.setMosaicOffset(new SizeDouble(offset));

            // verify
            BoundDouble pad2   = model.getPadding();
            SizeDouble offset2 = model.getMosaicOffset();
            Assert.assertEquals(pad, pad2);
            Assert.assertEquals(offset, offset2);
        };

        func.accept(true);
        func.accept(false);
    }

}
