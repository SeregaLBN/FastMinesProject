package fmg.core.mosaic;

import static fmg.core.img.PropertyConst.PROPERTY_IMAGE;
import static fmg.core.img.PropertyConst.PROPERTY_MODEL;
import static fmg.core.img.PropertyConst.PROPERTY_MODEL_AREA;
import static fmg.core.img.PropertyConst.PROPERTY_MODEL_SIZE;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

import org.junit.*;

import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EPlayInfo;

public class MosaicControllerTest {

    static final int TEST_SIZE_W = MosaicModelTest.TEST_SIZE_W;
    static final int TEST_SIZE_H = MosaicModelTest.TEST_SIZE_H;

    class MosaicTestView implements IMosaicView2<Object> {
        private boolean valid;
        private Object img;
        private int drawCount;
        int getDrawCount() { return drawCount; }
        private void draw() {
            drawCount++;
        }
        @Override public Object getImage() {
            if (img == null)
                img = new Object();
            if (!valid)
                draw();
            valid = true;
            return img;
        }
        @Override
        public boolean isValid() {
            return valid;
        }
        @Override
        public void reset() {
            img = null;
            valid = false;
        }
        @Override
        public void invalidate() {
            valid = false;
        }
        @Override
        public void invalidate(Collection<BaseCell> modifiedCells) {
            valid = false;
        }
    }

    class MosaicTestController extends MosaicController2<Object, MosaicTestView> {

        private final MosaicModel2 model = new MosaicModel2(true);
        private final MosaicTestView view = new MosaicTestView();

        MosaicTestController() {
            super.init(model, view);
        }

        @Override // to exclude a random result
        protected int nextFillMode() { return 1; }

        @Override protected void onChangeCellSquareSize() {}
        @Override protected void subscribeToViewControl() {}
        @Override protected void unsubscribeToViewControl() {}

        MosaicTestView getView() { return view; }
    }

    /** double precision */
    static final double P = MosaicModelTest.P;

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicControllerTest::setup");
    }

    @Before
    public void before() {
        Logger.info("======================================================");
    }
    @AfterClass
    public static void after() {
        Logger.info("======================================================");
        Logger.info("< MosaicControllerTest closed");
        Logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void propertyChangedTest() {
        Logger.info("> MosaicControllerTest::propertyChangedTest");

        Consumer<Boolean> func = callGetImage -> {
            try (var ctrlr = new MosaicTestController()) {
                var view = ctrlr.getView();
                Assert.assertFalse(view.isValid());
                ctrlr.getImage(); // draw image
                Assert.assertTrue("Check valid after draw", view.isValid());

                var modifiedProperties = new HashMap<String, Integer>();
                ctrlr.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));
                Assert.assertFalse("Check reset view after subscribe", view.isValid());

                if (callGetImage) {
                    ctrlr.getImage(); // draw image
                    Assert.assertTrue(view.isValid());
                }

                MosaicModelTest.changeModel(ctrlr.getModel());
                Assert.assertFalse(view.isValid());

                Assert.assertTrue (modifiedProperties.containsKey(PROPERTY_IMAGE));
                Assert.assertEquals(13, modifiedProperties.get(PROPERTY_IMAGE).intValue());

                if (callGetImage)
                    Assert.assertEquals(2, view.getDrawCount());
                else
                    Assert.assertEquals(1, view.getDrawCount());

                ctrlr.getImage();
                Assert.assertTrue(view.isValid());
            }
        };
        func.accept(true);
        func.accept(false);
    }

    @Test
    public void propertyChangedTest2() {
        Logger.info("> MosaicControllerTest::propertyChangedTest2");

        try (var ctrlr = new MosaicTestController()) {

            var modifiedProperties = new HashMap<String, Integer>();
            ctrlr.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

            ctrlr.getModel().setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));

            Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_IMAGE));
            Assert.assertEquals(2, modifiedProperties.get(        PROPERTY_IMAGE).intValue());
            Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MODEL));
            Assert.assertEquals(2, modifiedProperties.get(        PROPERTY_MODEL).intValue());
            Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MODEL_SIZE ));
            Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MODEL_SIZE ).intValue());
            Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MODEL_AREA ));
            Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MODEL_AREA ).intValue());
            Assert.assertEquals(4, modifiedProperties.size());
        }
    }

    @Test
    public void readinessAtTheStartTest() {
        Logger.info("> MosaicControllerTest::readinessAtTheStartTest");

        final int defArea = 500;

        try (var ctrlr = new MosaicTestController()) {
            Assert.assertEquals(defArea, ctrlr.getModel().getArea(), P);
            Assert.assertEquals(null, ctrlr.getCellDown());
            Assert.assertEquals(0, ctrlr.getCountClick());
            Assert.assertEquals(0, ctrlr.getCountFlag());
            Assert.assertEquals(10, ctrlr.getCountMinesLeft());
            Assert.assertEquals(0, ctrlr.getCountOpen());
            Assert.assertEquals(0, ctrlr.getCountUnknown());
            Assert.assertEquals(EGameStatus.eGSReady, ctrlr.getGameStatus());
            Assert.assertNotNull(ctrlr.getImage());
            Assert.assertNotNull(ctrlr.getModel().getMatrix());
            Assert.assertFalse(ctrlr.getModel().getMatrix().isEmpty());
            Assert.assertEquals(EMosaic.eMosaicSquare1, ctrlr.getModel().getMosaicType());
            Assert.assertEquals(EPlayInfo.ePlayerUnknown, ctrlr.getPlayInfo());
            Assert.assertNotNull(ctrlr.getRepositoryMines());
            Assert.assertTrue(ctrlr.getRepositoryMines().isEmpty());
            Assert.assertEquals(Math.sqrt(defArea) * 10, ctrlr.getModel().getSize().width, P);
            Assert.assertEquals(Math.sqrt(defArea) * 10, ctrlr.getModel().getSize().height, P);
            Assert.assertEquals(new Matrisize(10, 10), ctrlr.getModel().getSizeField());
        }
    }

    @Test
    public void readinessAtTheStartViewTest() {
        Logger.info("> MosaicControllerTest::readinessAtTheStartViewTest");

        try (var ctrlr = new MosaicTestController()) {
            var view = ctrlr.getView();
            Assert.assertEquals(0, view.getDrawCount());
            Assert.assertNotNull(view.getImage());
            Assert.assertEquals(1, view.getDrawCount());
        }
    }

    @Test
    public void multipleChangeModelOneDrawViewTest() throws InterruptedException {
        Logger.info("> MosaicControllerTest::multipleChangeModelOneDrawViewTest");

        try (var ctrlr = new MosaicTestController()) {
            var view = ctrlr.getView();
            Assert.assertEquals(0, view.getDrawCount());
            MosaicModelTest.changeModel(ctrlr.getModel());

            var img = view.getImage();
            Assert.assertNotNull(img);
            Assert.assertEquals(1, view.getDrawCount());

            // test no change
            ctrlr.getModel().setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));

            Assert.assertSame(img, view.getImage());
            Assert.assertEquals(1, view.getDrawCount());

            // test change
            ctrlr.getModel().setSize(new SizeDouble(TEST_SIZE_W + 1, TEST_SIZE_H));

            Assert.assertNotSame(img, view.getImage());
            Assert.assertNotNull(view.getImage());
            Assert.assertEquals(2, view.getDrawCount());
        }
    }

    @Test
    public void oneNotificationOfImageChangedViewTest() {
        Logger.info("> MosaicControllerTest::oneNotificationOfImageChangedViewTest");

        try (var ctrlr = new MosaicTestController()) {

            var modifiedProperties = new HashMap<String, Integer>();
            ctrlr.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

            var view = ctrlr.getView();

            MosaicModelTest.changeModel(ctrlr.getModel());

            Assert.assertTrue  (    modifiedProperties.containsKey(PROPERTY_IMAGE));
            Assert.assertEquals(13, modifiedProperties.get(        PROPERTY_IMAGE).intValue());
            Assert.assertEquals(0, view.getDrawCount());
            view.getImage(); // call the implicit draw method
            Assert.assertEquals(1, view.getDrawCount());
        }
    }

}
