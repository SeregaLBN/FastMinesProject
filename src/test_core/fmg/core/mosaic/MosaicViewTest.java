package fmg.core.mosaic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageView;
import fmg.core.mosaic.cells.BaseCell;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

class MosaicTestView extends MosaicView<DummyImage, DummyImage, MosaicTestModel> {
    MosaicTestView(boolean deferredNotifications) { super(new MosaicTestModel(), deferredNotifications); }
    @Override protected DummyImage createImage() { return new DummyImage(); }
    int DrawCount;// { get; private set; }
    @Override protected void drawModified(Collection<BaseCell> modifiedCells) {
        LoggerSimple.put("MosaicTestView::drawModified");
        ++DrawCount;
    }
    @Override public void close() {
        super.close();
        getModel().close();
    }
}

public class MosaicViewTest {

    static final int TEST_SIZE_W = MosaicModelTest.TEST_SIZE_W;
    static final int TEST_SIZE_H = MosaicModelTest.TEST_SIZE_H;

    @BeforeClass
    public static void setup() {
        LoggerSimple.put("MosaicViewTest::setup");

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
        List<String> modifiedProperties = new ArrayList<>();
        PropertyChangeListener onViewPropertyChanged = ev -> {
            LoggerSimple.put("  MosaicTestView::propertyChangedTest: onViewPropertyChanged: ev.name=" + ev.getPropertyName());
            modifiedProperties.add(ev.getPropertyName());
        };

        try (MosaicTestView view = new MosaicTestView(false)) {
            view.addListener(onViewPropertyChanged);

            view.getModel().setSize(new SizeDouble(123, 456));

            Awaitility.await().atMost(200, TimeUnit.MILLISECONDS).until(() ->
                modifiedProperties.contains(IImageView.PROPERTY_MODEL) &&
                modifiedProperties.contains(IImageView.PROPERTY_SIZE)  &&
                modifiedProperties.contains(IImageView.PROPERTY_IMAGE)
            );

            view.removeListener(onViewPropertyChanged);
        }

        LoggerSimple.put("------------------------------------------------------");

        modifiedProperties.clear();
        try (MosaicTestView view = new MosaicTestView(true)) {
            view.addListener(onViewPropertyChanged);

            view.getModel().setSize(new SizeDouble(123, 456));

            Awaitility.await().atMost(200, TimeUnit.MILLISECONDS).until(() ->
                modifiedProperties.contains(IImageView.PROPERTY_MODEL) &&
                modifiedProperties.contains(IImageView.PROPERTY_SIZE)  &&
                modifiedProperties.contains(IImageView.PROPERTY_IMAGE)
            );

            view.removeListener(onViewPropertyChanged);
        }
    }

    @Test
    public void readinessAtTheStartTest() {
        try (MosaicTestView view = new MosaicTestView(false)) {
            Assert.assertEquals(0, view.DrawCount);
            Assert.assertNotNull(view.getImage());
            Assert.assertEquals(1, view.DrawCount);
        }
    }

    @Test
    public void multipleChangeModelOneDrawViewTest() throws InterruptedException {
        try (MosaicTestView view = new MosaicTestView(false)) {
            Assert.assertEquals(0, view.DrawCount);

            MosaicTestModel m = view.getModel();
            MosaicModelTest.changeModel(m);
            DummyImage img = view.getImage();
            Assert.assertNotNull(img);
            Assert.assertEquals(1, view.DrawCount);

            m.setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
            Assert.assertEquals(img, view.getImage());
            Assert.assertEquals(1, view.DrawCount);

            m.setSize(new SizeDouble(TEST_SIZE_W + 1, TEST_SIZE_H));
            Awaitility.await().atMost(200, TimeUnit.MILLISECONDS).until(() ->
                !img.equals(view.getImage()) &&
                (view.DrawCount == 2)
            );
            Assert.assertNotNull(view.getImage());
        }
    }

    @Test
    public void oneNotificationOfImageChangedTest() {
        try (MosaicTestView view = new MosaicTestView(true)) {
            Map<String /* property name */, Integer /* count */> modifiedProperties = new HashMap<>();

            Subject<PropertyChangeEvent> subject = PublishSubject.create();
            PropertyChangeListener onViewPropertyChanged = ev -> {
                String name = ev.getPropertyName();
                LoggerSimple.put("  oneNotificationOfImageChangedTest: onViewPropertyChanged: ev.name=" + name);
                modifiedProperties.put(name, 1 + (modifiedProperties.containsKey(name) ? modifiedProperties.get(name) : 0));
                subject.onNext(ev);
            };
            view.addListener(onViewPropertyChanged);

            Signal signal = new Signal();
            Disposable dis = subject.timeout(50, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                        LoggerSimple.put("onError: " + ex);
                        signal.set();
                    });

            modifiedProperties.clear();
            MosaicModelTest.changeModel(view.getModel());

            Assert.assertTrue(signal.await(1000));

            LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: checking...");
            Assert.assertTrue(1 <= modifiedProperties.get(IImageView.PROPERTY_IMAGE)); // TODO must be assertEquals(1, modifiedProperties.get(IImageView.PROPERTY_IMAGE).intValue());
            Assert.assertEquals(0, view.DrawCount);
            view.getImage(); // call the implicit draw method
            Assert.assertEquals(1, view.DrawCount);

            view.removeListener(onViewPropertyChanged);
            dis.dispose();
        }
    }

}
