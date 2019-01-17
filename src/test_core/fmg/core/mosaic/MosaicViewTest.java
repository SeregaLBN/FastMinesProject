package fmg.core.mosaic;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageView;
import fmg.core.mosaic.cells.BaseCell;
import io.reactivex.Flowable;

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
    public void propertyChangedTest() throws InterruptedException {
        List<String> modifiedProperties = new ArrayList<>();
        PropertyChangeListener onViewPropertyChanged = ev -> {
            LoggerSimple.put("  MosaicTestView::propertyChangedTest: onViewPropertyChanged: ev.name=" + ev.getPropertyName());
            modifiedProperties.add(ev.getPropertyName());
        };

        try (MosaicTestView view = new MosaicTestView(false)) {
            view.addListener(onViewPropertyChanged);

            view.getModel().setSize(new SizeDouble(123, 456));

            Thread.sleep(200);

            Assert.assertTrue(modifiedProperties.contains(IImageView.PROPERTY_MODEL));
            Assert.assertTrue(modifiedProperties.contains(IImageView.PROPERTY_SIZE));
            Assert.assertTrue(modifiedProperties.contains(IImageView.PROPERTY_IMAGE));

            view.removeListener(onViewPropertyChanged);
        }

        LoggerSimple.put("------------------------------------------------------");

        modifiedProperties.clear();
        try (MosaicTestView view = new MosaicTestView(true)) {
            view.addListener(onViewPropertyChanged);

            view.getModel().setSize(new SizeDouble(123, 456));

            Thread.sleep(200);

            Assert.assertTrue(modifiedProperties.contains(IImageView.PROPERTY_MODEL));
            Assert.assertTrue(modifiedProperties.contains(IImageView.PROPERTY_SIZE));
            Assert.assertTrue(modifiedProperties.contains(IImageView.PROPERTY_IMAGE));

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
    public void mosaicXxxTest() {
    }

}
