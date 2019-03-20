package fmg.common.notifier;

import java.beans.PropertyChangeListener;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.*;

import fmg.common.LoggerSimple;
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
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> NotifyPropertyChangedTest::setup");

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
        LoggerSimple.put("< NotifyPropertyChangedTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void notifyPropertyChangedSyncTest() {
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
        final int initialValue = 1;
        try (SimpleProperty data = new SimpleProperty(initialValue, true)) {
            final int countFiredEvents = 3 + ThreadLocalRandom.current().nextInt(10);
            final String prefix = " Value ";
            new PropertyChangeExecutor<>(data).run(200, 1000,
                () -> {
                    for (int i = 0; i < countFiredEvents; ++i)
                        data.setProperty(prefix + i);
                }, modifiedProperties -> {
                    int countOfProperties = modifiedProperties.size();
                    Assert.assertEquals(1, countOfProperties);
                    int countReceivedEvents= modifiedProperties.values().iterator().next().first;
                    Assert.assertEquals(1, countReceivedEvents);
                    Object lastFiredValue = modifiedProperties.values().iterator().next().second;
                    Assert.assertEquals(prefix + (countFiredEvents-1), lastFiredValue);
                });
        }
    }

    @Test
    public void checkForNoEventTest() {
        LoggerSimple.put("> checkForNoEventTest");

        final int initialValue = 1;
        try (SimpleProperty data = new SimpleProperty(initialValue, true)) {
            new PropertyChangeExecutor<>(data).run(100, 1000,
                () -> {
                    LoggerSimple.put("    data.Property={0}", data.getProperty());
                    data.setProperty(initialValue + 123);
                    LoggerSimple.put("    data.Property={0}", data.getProperty());
                    data.setProperty(initialValue); // restore original value
                    LoggerSimple.put("    data.Property={0}", data.getProperty());
                }, modifiedProperties -> {
                    Assert.assertEquals(0, modifiedProperties.size());
                });
        }
    }

}
