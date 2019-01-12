package fmg.common.notyfier;

import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fmg.common.ui.Factory;

public class NotifyPropertyChangedTest {

    private INotifyPropertyChanged _dummy = new INotifyPropertyChanged() {

        @Override
        public void removeListener(PropertyChangeListener listener) { }

        @Override
        public void addListener(PropertyChangeListener listener) { }

    };

    static ExecutorService scheduler;

    @BeforeClass
    public static void setup() {
        scheduler = Executors.newScheduledThreadPool(1);
        Factory.DEFERR_INVOKER = scheduler::execute;
    }

    @Test
    public void NotifyPropertyChangedSyncTest() {
        try (NotifyPropertyChanged notifier = new NotifyPropertyChanged(_dummy, false)) {

            int countFiredEvents = 3 + ThreadLocalRandom.current().nextInt(10);
            int[] countReceivedEvents = { 0 };

            PropertyChangeListener listener = ev -> { ++countReceivedEvents[0]; };
            notifier.addListener(listener);
            for (int i=0; i<countFiredEvents; ++i)
                notifier.onPropertyChanged("propertyName");
            notifier.removeListener(listener);

            Assert.assertEquals(countFiredEvents, countReceivedEvents[0]);
        }
    }

    @Test
    public void NotifyPropertyChangedAsyncTest() throws InterruptedException {
        try (NotifyPropertyChanged notifier = new NotifyPropertyChanged(_dummy, true)) {

            int countFiredEvents = 3 + ThreadLocalRandom.current().nextInt(10);
            int[] countReceivedEvents = { 0 };
            Object[] firedValue = { null };

            PropertyChangeListener listener = ev -> {
                ++countReceivedEvents[0];
                firedValue[0] = ev.getNewValue();
            };
            notifier.addListener(listener);
            final String prefix = " Value ";
            for (int i=0; i<countFiredEvents; ++i)
                notifier.onPropertyChanged(null, prefix + i, "propertyName");

            scheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
            notifier.removeListener(listener);

            Assert.assertEquals(1, countReceivedEvents[0]);
            Assert.assertEquals(prefix + (countFiredEvents-1), firedValue[0]);
        }
    }

}
