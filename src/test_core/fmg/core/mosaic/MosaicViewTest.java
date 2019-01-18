package fmg.core.mosaic;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.*;

import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
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

    static final int TEST_SIZE_W = 456;
    static final int TEST_SIZE_H = 789;

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
            changeModel(m);
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

    private void changeModel(MosaicTestModel m) {
        m.setMosaicType(EMosaic.eMosaicQuadrangle1);
        m.setSizeField(new Matrisize(22, 33));
        m.setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
        m.setPadding(new BoundDouble(10));
        m.setBackgroundColor(Color.DimGray());
        m.getBackgroundFill().setMode(1);
        m.getColorText().setColorClose(1, Color.LightSalmon());
        m.getColorText().setColorOpen(2, Color.MediumSeaGreen());
        m.getPenBorder().setColorLight(Color.MediumPurple());
        m.getPenBorder().setWidth(2);
    }

}
