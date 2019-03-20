package fmg.core.mosaic;

import java.util.Collection;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.PropertyChangeExecutor;
import fmg.core.img.IImageView;
import fmg.core.mosaic.cells.BaseCell;
import io.reactivex.Flowable;

class MosaicTestView extends MosaicView<DummyImage, DummyImage, MosaicTestModel> {
    MosaicTestView() { super(new MosaicTestModel()); }
    @Override protected DummyImage createImage() { return new DummyImage(); }
    private int drawCount;
    int getDrawCount() { return drawCount; }
    @Override protected void drawModified(Collection<BaseCell> modifiedCells) {
        LoggerSimple.put("MosaicTestView::drawModified");
        ++drawCount;
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
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicViewTest::setup");

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
        LoggerSimple.put("< MosaicViewTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void propertyChangedTest() {
        LoggerSimple.put("> MosaicTestView::propertyChangedTest");

        try (MosaicTestView view = new MosaicTestView()) {
            new PropertyChangeExecutor<>(view).run(100, 1000,
                () -> {
                    view.getModel().setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
                }, modifiedProperties -> {
                    Assert.assertTrue  (                    modifiedProperties.containsKey(IImageView.PROPERTY_MODEL));
                    Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        IImageView.PROPERTY_MODEL).first);
                    Assert.assertTrue  (                    modifiedProperties.containsKey(IImageView.PROPERTY_SIZE ));
                    Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        IImageView.PROPERTY_SIZE ).first);
                    Assert.assertTrue  (                    modifiedProperties.containsKey(IImageView.PROPERTY_IMAGE));
                    Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        IImageView.PROPERTY_IMAGE).first);
                    Assert.assertEquals(                3 , modifiedProperties.size());
                });
        }
    }

    @Test
    public void readinessAtTheStartTest() {
        LoggerSimple.put("> MosaicTestView::readinessAtTheStartTest");

        try (MosaicTestView view = new MosaicTestView()) {
            Assert.assertEquals(0, view.getDrawCount());
            Assert.assertNotNull(view.getImage());
            Assert.assertEquals(1, view.getDrawCount());
        }
    }

    @Test
    public void multipleChangeModelOneDrawViewTest() throws InterruptedException {
        LoggerSimple.put("> MosaicTestView::multipleChangeModelOneDrawViewTest");

        try (MosaicTestView view = new MosaicTestView()) {
            Assert.assertEquals(0, view.getDrawCount());

            DummyImage[] img = { null };

            MosaicTestModel m = view.getModel();
            new PropertyChangeExecutor<>(view).run(100, 1000,
               () -> {
                   MosaicModelTest.changeModel(m);
               }, modifiedProperties -> {
                   img[0] = view.getImage();
                   Assert.assertNotNull(img[0]);
                   Assert.assertEquals(1, view.getDrawCount());
               });

            // test no change
            new PropertyChangeExecutor<>(view).run(100, 1000,
               () -> {
                   m.setSize(new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
               }, modifiedProperties -> {
                   Assert.assertEquals(img[0], view.getImage());
                   Assert.assertEquals(1, view.getDrawCount());
               });

            // test change
            new PropertyChangeExecutor<>(view).run(100, 1000,
               () -> {
                   m.setSize(new SizeDouble(TEST_SIZE_W + 1, TEST_SIZE_H));
               }, modifiedProperties -> {
                   Assert.assertNotEquals(img[0], view.getImage());
                   Assert.assertNotNull(view.getImage());
                   Assert.assertEquals(2, view.getDrawCount());
               });
        }
    }

    @Test
    public void oneNotificationOfImageChangedTest() {
        LoggerSimple.put("> MosaicTestView::oneNotificationOfImageChangedTest");

        try (MosaicTestView view = new MosaicTestView()) {
            new PropertyChangeExecutor<>(view).run(100, 1000,
               () -> {
                   MosaicModelTest.changeModel(view.getModel());
               }, modifiedProperties -> {
                   Assert.assertTrue  (                    modifiedProperties.containsKey(IImageView.PROPERTY_IMAGE));
                   Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(        IImageView.PROPERTY_IMAGE).first);
                   Assert.assertEquals(0, view.getDrawCount());
                   view.getImage(); // call the implicit draw method
                   Assert.assertEquals(1, view.getDrawCount());
               });
        }
    }

}
