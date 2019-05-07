package fmg.common.notifier;

import java.beans.PropertyChangeListener;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicModelTest;
import io.reactivex.Flowable;

public class PropertyChangeExecutorTest {

    private static class SimpleDataObj implements INotifyPropertyChanged, AutoCloseable {

        private final NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

        @Override
        public void addListener(PropertyChangeListener listener) { notifier.addListener(listener); }
        @Override
        public void removeListener(PropertyChangeListener listener) { notifier.removeListener(listener); }

        @Override
        public void close() { notifier.close(); }

        public boolean isDisposed() { return notifier.isDisposed(); }

    }


    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> PropertyChangeExecutorTest::setup");

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
        LoggerSimple.put("< PropertyChangeExecutorTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void simpleUsageTest() {
        LoggerSimple.put("> PropertyChangeExecutorTest::simpleUsageTest");

        SimpleDataObj[] d = { null };
        new PropertyChangeExecutor<>(() -> d[0] = new SimpleDataObj()).run(1, 10,
            data -> {
                LoggerSimple.put("    data modificator");
            }, (data, modifiedProperties) -> {
                LoggerSimple.put("    data validator");
                Assert.assertEquals(0, modifiedProperties.size());
                Assert.assertFalse(data.isDisposed());
            });
        Assert.assertNotNull(d[0]);
        Assert.assertTrue(d[0].isDisposed());
    }

    @Test
    public void extendedUsageTest() {
        LoggerSimple.put("> PropertyChangeExecutorTest::extendedUsageTest");

        SimpleDataObj[] d = { null };
        new PropertyChangeExecutor<>(() -> d[0] = new SimpleDataObj(), false).run(1, 10,
            data -> {
                LoggerSimple.put("    data modificator");
            }, (data, modifiedProperties) -> {
                LoggerSimple.put("    data validator");
                Assert.assertEquals(0, modifiedProperties.size());
                Assert.assertFalse(data.isDisposed());
            });
        Assert.assertNotNull(d[0]);
        Assert.assertFalse(d[0].isDisposed());

        new PropertyChangeExecutor<>(() -> d[0]).run(1, 10,
            data -> {
                LoggerSimple.put("    data modificator");
            }, (data, modifiedProperties) -> {
                LoggerSimple.put("    data validator");
                Assert.assertEquals(0, modifiedProperties.size());
                Assert.assertFalse(data.isDisposed());
            });
        Assert.assertTrue(d[0].isDisposed());
    }

    @Test
    public void modificatorFailTest() {
        LoggerSimple.put("> PropertyChangeExecutorTest::modificatorFailTest");

        IllegalArgumentException failEx = new IllegalArgumentException("Tested exception");
        try {
            new PropertyChangeExecutor<>(() -> new SimpleDataObj()).run(1, 10,
               data -> {
                   LoggerSimple.put("    data modificator");
                   throw failEx;
               }, (data, modifiedProperties) -> {
                   LoggerSimple.put("    data validator");
                   Assert.fail();
               });
            Assert.fail();
        } catch(Throwable ex) {
            Assert.assertEquals(failEx, ex);
        }
    }


    @Test
    public void validatorFailTest() {
        LoggerSimple.put("> PropertyChangeExecutorTest::modificatorFailTest");

        IllegalArgumentException failEx = new IllegalArgumentException("Tested exception");
        try {
            new PropertyChangeExecutor<>(() -> new SimpleDataObj()).run(1, 10,
               data -> {
                   LoggerSimple.put("    data modificator");
               }, (data, modifiedProperties) -> {
                   LoggerSimple.put("    data validator");
                   throw failEx;
               });
            Assert.fail();
        } catch(Throwable ex) {
            Assert.assertEquals(failEx, ex);
        }
    }

}
