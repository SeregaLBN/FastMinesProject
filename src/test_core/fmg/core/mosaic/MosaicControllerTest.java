package fmg.core.mosaic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.Matrisize;
import fmg.common.notyfier.Signal;
import fmg.common.ui.Factory;
import fmg.core.img.IImageController;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EPlayInfo;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

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
        try (MosaicTestController ctrlr = new MosaicTestController()) {
            Map<String /* property name */, Integer /* count */> modifiedProperties = new HashMap<>();

            Subject<PropertyChangeEvent> subject = PublishSubject.create();
            PropertyChangeListener onCtrlPropertyChanged = ev -> {
                String name = ev.getPropertyName();
                LoggerSimple.put("  propertyChangedTest: onCtrlPropertyChanged: ev.name=" + name);
                modifiedProperties.put(name, 1 + (modifiedProperties.containsKey(name) ? modifiedProperties.get(name) : 0));
                subject.onNext(ev);
            };
            ctrlr.addListener(onCtrlPropertyChanged);

            Signal signal = new Signal();
            Disposable dis = subject.timeout(50, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                        LoggerSimple.put("onError: " + ex);
                        signal.set();
                    });

            modifiedProperties.clear();
            Factory.DEFERR_INVOKER.accept(() -> MosaicModelTest.changeModel(ctrlr.getModel()));

            Assert.assertTrue(signal.await(1000));

            LoggerSimple.put("  propertyChangedTest: checking...");
            Assert.assertTrue(1 <= modifiedProperties.get(IImageController.PROPERTY_IMAGE)); // TODO must be assertEquals(1, modifiedProperties.get(IImageController.PROPERTY_IMAGE).intValue());

            ctrlr.removeListener(onCtrlPropertyChanged);
            dis.dispose();
        }
    }

    @Test
    public void readinessAtTheStartTest() {
        final int defArea = 500;
        try (MosaicTestController ctrlr = new MosaicTestController()) {
            Assert.assertEquals(defArea, ctrlr.getArea(), P);
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
