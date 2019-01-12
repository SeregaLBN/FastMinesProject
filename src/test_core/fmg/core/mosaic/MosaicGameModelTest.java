package fmg.core.mosaic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fmg.common.LoggerSimple;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageModel;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class MosaicGameModelTest {

    static final int MagicDelayMlsc = 50;
    static class DummyMosaicImageType extends Object {}

    static class Signal {
        private final CountDownLatch signal = new CountDownLatch(1);
        /** set signal */
        public void set() { signal.countDown(); }
        /** <summary> wait for signal */
        public boolean await(long timeoutMs) {
            try {
                return signal.await(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }


    @BeforeClass
    public static void setup() {
        ExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Factory.DEFERR_INVOKER = scheduler::execute;
        Flowable.just("Hello world").subscribe(LoggerSimple::put);
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }

    @Test
    public void mosaicGameModelPropertyChangedTest() {
        LoggerSimple.put("> mosaicGameModelPropertyChangedTest");

        try (MosaicGameModel model = new MosaicGameModel()) {
            Assert.assertTrue(!model.getMatrix().isEmpty());
            Assert.assertTrue(model.getCellAttr() == model.getMatrix().get(0).getAttr()); // reference equals

            List<String> modifiedProperties = new ArrayList<>();
            PropertyChangeListener onModelPropertyChanged = ev -> {
                LoggerSimple.put("  mosaicGameModelPropertyChangedTest: onModelPropertyChanged: ev.name=" + ev.getPropertyName());
                modifiedProperties.add(ev.getPropertyName());
            };

            model.addListener(onModelPropertyChanged);

            modifiedProperties.clear();
            model.setSizeField(new Matrisize(15, 10));
            Awaitility.await().atMost(200, TimeUnit.MILLISECONDS).until(() -> {
                LoggerSimple.put("  mosaicGameModelPropertyChangedTest: pooling1...");
                return modifiedProperties.contains(MosaicGameModel.PROPERTY_SIZE_FIELD) &&
                       modifiedProperties.contains(MosaicGameModel.PROPERTY_MATRIX);
            });

            modifiedProperties.clear();
            model.setArea(12345);
            Awaitility.await().atMost(200, TimeUnit.MILLISECONDS).until(() -> {
                LoggerSimple.put("  mosaicGameModelPropertyChangedTest: pooling2...");
                return modifiedProperties.contains(MosaicGameModel.PROPERTY_AREA) &&
                       modifiedProperties.contains(MosaicGameModel.PROPERTY_CELL_ATTR);
            });

            model.removeListener(onModelPropertyChanged);
        }
    }

    @Test
    public void mosaicDrawModelPropertyChangedTest() {
        LoggerSimple.put("> mosaicDrawModelPropertyChangedTest");

        try (MosaicDrawModel<DummyMosaicImageType> model = new MosaicDrawModel<>()) {
            Subject<PropertyChangeEvent> subject = PublishSubject.create();

            List<String> modifiedProperties = new ArrayList<>();
            PropertyChangeListener onModelPropertyChanged = ev -> {
                LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: onModelPropertyChanged: ev.name=" + ev.getPropertyName());
                modifiedProperties.add(ev.getPropertyName());
                subject.onNext(ev);
            };
            model.addListener(onModelPropertyChanged);

            Signal signal = new Signal();
            Disposable dis = subject.timeout(MagicDelayMlsc, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                        LoggerSimple.put("onError: " + ex);
                        signal.set();
                    });

            modifiedProperties.clear();
            model.setSize(new SizeDouble(123, 456));

            Assert.assertTrue(signal.await(5000));

            LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: checking...");
            Assert.assertTrue(modifiedProperties.contains(IImageModel.PROPERTY_SIZE));

            model.removeListener(onModelPropertyChanged);
            dis.dispose();
        }
    }

    @Test
    public void mosaicDrawModelTest() {
        try (MosaicDrawModel<DummyMosaicImageType> model = new MosaicDrawModel<>()) {
            Assert.assertEquals(model.getCellAttr().getSize(model.getSizeField()), model.getSize());
        }
    }

}
