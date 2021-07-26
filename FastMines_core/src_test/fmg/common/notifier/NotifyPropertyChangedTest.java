package fmg.common.notifier;

import java.beans.PropertyChangeListener;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.*;

import fmg.common.Logger;
import fmg.core.mosaic.MosaicModelTest;
import io.reactivex.Flowable;

public class NotifyPropertyChangedTest {

    static class SimpleProperty implements INotifyPropertyChanged, AutoCloseable {
        public static final String PROPERTY_FIELD_NAME = "Property";
        private Object property;
        private final NotifyPropertyChanged notifier;
        SimpleProperty(Object initValueOfProperty, boolean deferredNotifications) {
            notifier = new NotifyPropertyChanged(this, deferredNotifications);
            this.property = initValueOfProperty;
        }
        public Object getProperty() { return property; }
        public void setProperty(Object value) { notifier.setProperty(property, value, PROPERTY_FIELD_NAME); }
        @Override
        public void addListener(PropertyChangeListener listener) { notifier.addListener(listener); }
        @Override
        public void removeListener(PropertyChangeListener listener) { notifier.removeListener(listener); }
        @Override
        public void close() { notifier.close(); }
    }


    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> NotifyPropertyChangedTest::setup");

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
        Logger.info("< NotifyPropertyChangedTest closed");
        Logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void notifyPropertyChangedSyncTest() {
        Logger.info("> NotifyPropertyChangedTest::notifyPropertyChangedSyncTest");

        try (SimpleProperty data = new SimpleProperty(-1, false)) {
            int countFiredEvents = 3 + ThreadLocalRandom.current().nextInt(10);
            int[] countReceivedEvents = { 0 };

            PropertyChangeListener listener = ev -> { ++countReceivedEvents[0]; };
            data.addListener(listener);
            for (int i=0; i<countFiredEvents; ++i)
                data.setProperty(i);
            data.removeListener(listener);

            Assert.assertEquals(countFiredEvents, countReceivedEvents[0]);
        }
    }

    @Test
    public void notifyPropertyChangedAsyncTest() {
        Logger.info("> NotifyPropertyChangedTest::notifyPropertyChangedAsyncTest");

        final int initialValue = 1;
        final int countFiredEvents = 3 + ThreadLocalRandom.current().nextInt(10);
        final String prefix = " Value ";
        new PropertyChangeExecutor<>(() -> new SimpleProperty(initialValue, true)).run(200, 1000,
            data -> {
                for (int i = 0; i < countFiredEvents; ++i)
                    data.setProperty(prefix + i);
            }, (data, modifiedProperties) -> {
                int countOfProperties = modifiedProperties.size();
                Assert.assertEquals(1, countOfProperties);
                int countReceivedEvents= modifiedProperties.values().iterator().next().first;
                Assert.assertEquals(1, countReceivedEvents);
                Object lastFiredValue = modifiedProperties.values().iterator().next().second;
                Assert.assertEquals(prefix + (countFiredEvents-1), lastFiredValue);
            });
    }

    @Test
    public void checkForNoEventTest() {
        Logger.info("> NotifyPropertyChangedTest::checkForNoEventTest");

        final int initialValue = 1;
        new PropertyChangeExecutor<>(() -> new SimpleProperty(initialValue, true)).run(100, 1000,
            data -> {
                Logger.info("    data.Property={0}", data.getProperty());
                data.setProperty(initialValue + 123);
                Logger.info("    data.Property={0}", data.getProperty());
                data.setProperty(initialValue); // restore original value
                Logger.info("    data.Property={0}", data.getProperty());
            }, (data, modifiedProperties) -> {
                Assert.assertEquals(0, modifiedProperties.size());
            });
    }

    @Test
    public void forgotToUnsubscribeTest() {
        Logger.info("> NotifyPropertyChangedTest::forgotToUnsubscribeTest");

        try {
            try (SimpleProperty obj = new SimpleProperty(null, false)) {
                PropertyChangeListener listener = ev -> {};
                obj.addListener(listener);
              //obj.removeListener(listener); // test forgot this
            }
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IllegalStateException);
            Assert.assertEquals(ex.getMessage(), "Illegal usage: Not all listeners were unsubscribed (type " + SimpleProperty.class.getName() + "): count=1");
        }
    }

}
