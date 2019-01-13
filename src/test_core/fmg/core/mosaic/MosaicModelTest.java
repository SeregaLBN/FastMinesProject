package fmg.core.mosaic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageModel;
import fmg.core.types.EMosaic;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class MosaicModelTest {

    static class DummyMosaicImageType extends Object {}

    static class MosaicTestModel extends MosaicDrawModel<DummyMosaicImageType> {}

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

        try (MosaicTestModel model = new MosaicTestModel()) {
            Subject<PropertyChangeEvent> subject = PublishSubject.create();

            Map<String /* property name */, Integer /* count */> modifiedProperties = new HashMap<>();
            PropertyChangeListener onModelPropertyChanged = ev -> {
                String name = ev.getPropertyName();
                LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: onModelPropertyChanged: ev.name=" + name);
                modifiedProperties.put(name, 1 + (modifiedProperties.containsKey(name) ? modifiedProperties.get(name) : 0));
                subject.onNext(ev);
            };
            model.addListener(onModelPropertyChanged);

            Signal signal = new Signal();
            Disposable dis = subject.timeout(50, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                        LoggerSimple.put("onError: " + ex);
                        signal.set();
                    });

            modifiedProperties.clear();
            model.setSize(new SizeDouble(123, 456));

            Assert.assertTrue(signal.await(1000));

            LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: checking...");
            Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(IImageModel.PROPERTY_SIZE));
            Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(MosaicGameModel.PROPERTY_AREA));
            Assert.assertEquals(Integer.valueOf(1), modifiedProperties.get(MosaicGameModel.PROPERTY_CELL_ATTR));

            model.removeListener(onModelPropertyChanged);
            dis.dispose();
        }
    }

    @Test
    public void mosaicDrawModelAsIsTest() {
        try (MosaicTestModel model = new MosaicTestModel()) {
            Assert.assertEquals(EMosaic.eMosaicSquare1, model.getMosaicType());
            Assert.assertEquals(new Matrisize(10, 10), model.getSizeField());
            Assert.assertEquals(model.getCellAttr().getSize(model.getSizeField()), model.getSize());
        }
    }

    @Test
    public void autoFitTrueCheckAffectsToPaddingTest() {
        try (MosaicTestModel model = new MosaicTestModel()) {
            // set property
            model.setAutoFit(true);
            model.setSize(new SizeDouble(1000, 1000));
            model.setPadding(new BoundDouble(100));

            // change poperty
            model.setSize(new SizeDouble(500, 700));

            // check dependency
            Assert.assertEquals(50.0, model.getPadding().left  , 0);
            Assert.assertEquals(50.0, model.getPadding().right , 0);
            Assert.assertEquals(70.0, model.getPadding().top   , 0);
            Assert.assertEquals(70.0, model.getPadding().bottom, 0);
        }
    }

    @Test
    public void autoFitTrueCheckAffectsToMosaicSizeTest() {
        try (MosaicTestModel model = new MosaicTestModel()) {
            // set property
            model.setAutoFit(true);
            model.setSize(new SizeDouble(1000, 1000));

            // check dependency (evenly expanded)
            SizeDouble mosaicSize = model.getMosaicSize();
            Assert.assertEquals(1000, mosaicSize.width , 0);
            Assert.assertEquals(1000, mosaicSize.height, 0);


            // change poperty
            model.setSize(new SizeDouble(700, 500));

            // check dependency (evenly expanded)
            mosaicSize = model.getMosaicSize();
            Assert.assertEquals(500, mosaicSize.width , 0.001);
            Assert.assertEquals(500, mosaicSize.height, 0.001);


            // change poperty
            model.setMosaicType(EMosaic.eMosaicSquare2);

            // check dependency (evenly expanded)
            mosaicSize = model.getMosaicSize();
            Assert.assertEquals(525, mosaicSize.width , 0.001);
            Assert.assertEquals(500, mosaicSize.height, 0.001);


            // change poperty
            model.setSizeField(new Matrisize(10, 15));

            // check dependency (evenly expanded)
            mosaicSize = model.getMosaicSize();
            Assert.assertEquals(350, mosaicSize.width , 0.001);
            Assert.assertEquals(500, mosaicSize.height, 0.001);
        }
    }

}
