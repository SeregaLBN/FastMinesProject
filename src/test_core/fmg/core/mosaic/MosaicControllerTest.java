package fmg.core.mosaic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageController;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

class MosaicTestController extends MosaicController<DummyImage, DummyImage, MosaicTestView, MosaicTestModel> {
    MosaicTestController() { super(new MosaicTestView(false)); }
    @Override public void close() {
        super.close();
        getView().close();
    }
}

public class MosaicControllerTest {

    static final int TEST_SIZE_W = MosaicModelTest.TEST_SIZE_W;
    static final int TEST_SIZE_H = MosaicModelTest.TEST_SIZE_H;

    @BeforeClass
    public static void setup() {
        LoggerSimple.put("MosaicControllerTest::setup");

//        ExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        Factory.DEFERR_INVOKER = scheduler::execute;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Factory.DEFERR_INVOKER = run -> scheduler.schedule(run, 10, TimeUnit.MILLISECONDS);

        Flowable.just("UI factory inited...").subscribe(LoggerSimple::put);
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }
    @AfterClass
    public static void after() {
        LoggerSimple.put("======================================================");
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
            //MosaicModelTest.changeModel(ctrlr.getModel());
            ctrlr.getModel().setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));

            Assert.assertTrue(signal.await(1000));

            LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: checking...");
            Assert.assertTrue(1 <= modifiedProperties.get(IImageController.PROPERTY_IMAGE)); // TODO must be assertEquals(1, modifiedProperties.get(IImageController.PROPERTY_IMAGE).intValue());

            ctrlr.removeListener(onCtrlPropertyChanged);
            dis.dispose();
        }
    }

}
