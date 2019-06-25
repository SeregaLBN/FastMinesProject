package fmg.common.notifier;

import java.beans.PropertyChangeListener;

import org.junit.*;

import fmg.common.Logger;
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
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> PropertyChangeExecutorTest::setup");

        MosaicModelTest.ProjSettings();

        Flowable.just("UI factory inited...").subscribe(Logger::info);
    }

    @Before
    public void before() {
        Logger.info("======================================================");
    }

    @AfterClass
    public static void after() {
        Logger.info("======================================================");
        Logger.info("< PropertyChangeExecutorTest closed");
        Logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void simpleUsageTest() {
        Logger.info("> PropertyChangeExecutorTest::simpleUsageTest");

        SimpleDataObj[] d = { null };
        new PropertyChangeExecutor<>(() -> d[0] = new SimpleDataObj()).run(10, 1000,
            data -> {
                Logger.info("    data modificator");
            }, (data, modifiedProperties) -> {
                Logger.info("    data validator");
                Assert.assertNotNull(data);
                Assert.assertEquals(0, modifiedProperties.size());
                Assert.assertFalse(data.isDisposed());
            });
        Assert.assertNotNull(d[0]);
        Assert.assertTrue(d[0].isDisposed());
    }

    @Test
    public void extendedUsageTest() {
        Logger.info("> PropertyChangeExecutorTest::extendedUsageTest");

        SimpleDataObj[] d = { null };
        new PropertyChangeExecutor<>(() -> d[0] = new SimpleDataObj(), false).run(300, 1000,
            data -> {
                Logger.info("    data modificator");
            }, (data, modifiedProperties) -> {
                Logger.info("    data validator");
                Assert.assertNotNull(data);
                Assert.assertEquals(0, modifiedProperties.size());
                Assert.assertFalse(data.isDisposed());
            });
        Assert.assertNotNull(d[0]);
        Assert.assertFalse(d[0].isDisposed());

        new PropertyChangeExecutor<>(() -> d[0]).run(10, 1000,
            data -> {
                Logger.info("    data modificator");
            }, (data, modifiedProperties) -> {
                Logger.info("    data validator");
                Assert.assertEquals(0, modifiedProperties.size());
                Assert.assertFalse(data.isDisposed());
            });
        Assert.assertTrue(d[0].isDisposed());
    }

    @Test
    public void creatorFailTest() {
        Logger.info("> PropertyChangeExecutorTest::creatorFailTest");

        IllegalArgumentException failEx = new IllegalArgumentException("Tested exception");
        try {
            new PropertyChangeExecutor<>(() -> { throw failEx; }).run(10, 1000,
               data -> {
                   Logger.info("    data modificator");
                   Assert.fail();
               }, (data, modifiedProperties) -> {
                   Logger.info("    data validator");
                   Assert.fail();
               });
            Assert.fail();
        } catch(Throwable ex) {
            Assert.assertEquals(failEx, ex);
        }
    }

    @Test
    public void modificatorFailTest() {
        Logger.info("> PropertyChangeExecutorTest::modificatorFailTest");

        IllegalArgumentException failEx = new IllegalArgumentException("Tested exception");
        try {
            new PropertyChangeExecutor<>(SimpleDataObj::new).run(10, 1000,
               data -> {
                   Logger.info("    data modificator");
                   throw failEx;
               }, (data, modifiedProperties) -> {
                   Logger.info("    data validator");
                   Assert.fail();
               });
            Assert.fail();
        } catch(Throwable ex) {
            Assert.assertEquals(failEx, ex);
        }
    }


    @Test
    public void validatorFailTest() {
        Logger.info("> PropertyChangeExecutorTest::modificatorFailTest");

        IllegalArgumentException failEx = new IllegalArgumentException("Tested exception");
        try {
            new PropertyChangeExecutor<>(SimpleDataObj::new).run(10, 1000,
               data -> {
                   Logger.info("    data modificator");
               }, (data, modifiedProperties) -> {
                   Logger.info("    data validator");
                   throw failEx;
               });
            Assert.fail();
        } catch(Throwable ex) {
            Assert.assertEquals(failEx, ex);
        }
    }

}
