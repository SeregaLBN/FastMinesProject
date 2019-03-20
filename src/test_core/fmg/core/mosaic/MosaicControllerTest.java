package fmg.core.mosaic;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.Matrisize;
import fmg.common.notifier.PropertyChangeExecutor;
import fmg.core.img.IImageController;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EPlayInfo;
import io.reactivex.Flowable;

class MosaicTestController extends MosaicController<DummyImage, DummyImage, MosaicTestView, MosaicTestModel> {
    MosaicTestController() { super(new MosaicTestView()); }
    @Override public void close() {
        super.close();
        getView().close();
    }
}

public class MosaicControllerTest {

    /** double precision */
    static final double P = MosaicModelTest.P;

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicControllerTest::setup");

        MosaicModelTest.StaticInitializer();

        Flowable.just("UI factory inited...").subscribe(LoggerSimple::put);
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }
    @AfterClass
    public static void after() {
        LoggerSimple.put("======================================================");
        LoggerSimple.put("< MosaicControllerTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void propertyChangedTest() {
        LoggerSimple.put("> MosaicControllerTest::propertyChangedTest");

        try (MosaicTestController ctrlr = new MosaicTestController()) {
            new PropertyChangeExecutor<>(ctrlr).run(100, 1000,
               () -> {
                   MosaicModelTest.changeModel(ctrlr.getModel());
               }, modifiedProperties -> {
                   Assert.assertTrue  (                    modifiedProperties.containsKey(IImageController.PROPERTY_IMAGE));
                   Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        IImageController.PROPERTY_IMAGE).first);
               });
        }
    }

    @Test
    public void readinessAtTheStartTest() {
        LoggerSimple.put("> MosaicControllerTest::readinessAtTheStartTest");

        final int defArea = 500;
        try (MosaicTestController ctrlr = new MosaicTestController()) {
            Assert.assertEquals(defArea, ctrlr.getModel().getArea(), P);
            Assert.assertEquals(null, ctrlr.getCellDown());
            Assert.assertEquals(0, ctrlr.getCountClick());
            Assert.assertEquals(0, ctrlr.getCountFlag());
            Assert.assertEquals(10, ctrlr.getCountMinesLeft());
            Assert.assertEquals(0, ctrlr.getCountOpen());
            Assert.assertEquals(0, ctrlr.getCountUnknown());
            Assert.assertEquals(EGameStatus.eGSReady, ctrlr.getGameStatus());
            Assert.assertNotNull(ctrlr.getImage());
            Assert.assertNotNull(ctrlr.getMatrix());
            Assert.assertFalse(ctrlr.getMatrix().isEmpty());
            Assert.assertEquals(EMosaic.eMosaicSquare1, ctrlr.getMosaicType());
            Assert.assertEquals(EPlayInfo.ePlayerUnknown, ctrlr.getPlayInfo());
            Assert.assertNotNull(ctrlr.getRepositoryMines());
            Assert.assertTrue(ctrlr.getRepositoryMines().isEmpty());
            Assert.assertEquals(Math.sqrt(defArea) * 10, ctrlr.getSize().width, P);
            Assert.assertEquals(Math.sqrt(defArea) * 10, ctrlr.getSize().height, P);
            Assert.assertEquals(new Matrisize(10, 10), ctrlr.getSizeField());
        }
    }

}
