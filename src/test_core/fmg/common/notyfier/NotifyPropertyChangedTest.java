package fmg.common.notyfier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicModelTest;
import io.reactivex.Flowable;
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

            Signal signal = new Signal();
            Disposable dis = subject.timeout(100, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                        LoggerSimple.put("onError: " + ex);
                        signal.set();
                    });

            final String prefix = " Value ";
            for (int i=0; i<countFiredEvents; ++i)
                notifier.onPropertyChanged(null, prefix + i, "propertyName");

            Assert.assertTrue(signal.await(1000));

            Assert.assertEquals(1, countReceivedEvents[0]);
            Assert.assertEquals(prefix + (countFiredEvents-1), firedValue[0]);

            notifier.removeListener(listener);
            dis.dispose();
        }
    }

}
