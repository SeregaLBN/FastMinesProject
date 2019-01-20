package fmg.common.notyfier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fmg.common.LoggerSimple;
import fmg.swing.utils.StaticInitializer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class NotifyPropertyChangedTest {

    private INotifyPropertyChanged _dummy = new INotifyPropertyChanged() {

        @Override
        public void removeListener(PropertyChangeListener listener) { }

        @Override
        public void addListener(PropertyChangeListener listener) { }

    };

    @BeforeClass
    public static void setup() {
        StaticInitializer.init();
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

            Subject<PropertyChangeEvent> subject = PublishSubject.create();
            PropertyChangeListener listener = ev -> {
                ++countReceivedEvents[0];
                firedValue[0] = ev.getNewValue();
                subject.onNext(ev);
            };
            notifier.addListener(listener);

            final String prefix = " Value ";
            SwingUtilities.invokeLater(() -> {
                for (int i=0; i<countFiredEvents; ++i)
                    notifier.onPropertyChanged(null, prefix + i, "propertyName");
            });
            Signal signal = new Signal();
            Disposable dis = subject.timeout(50, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                        LoggerSimple.put("onError: " + ex);
                        signal.set();
                    });


            Assert.assertTrue(signal.await(1000));

            notifier.removeListener(listener);
            dis.dispose();

            Assert.assertEquals(1, countReceivedEvents[0]);
            Assert.assertEquals(prefix + (countFiredEvents-1), firedValue[0]);
        }
    }

}
