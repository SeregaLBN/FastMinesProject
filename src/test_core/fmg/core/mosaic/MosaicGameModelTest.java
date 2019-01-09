package fmg.core.mosaic;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fmg.common.LoggerSimple;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import fmg.core.img.IImageModel;

public class MosaicGameModelTest {

    static class DummyMosaicImageType extends Object {}

    @BeforeClass
    public static void setup() {
        ExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Factory.DEFERR_INVOKER = scheduler::execute;
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
            List<String> modifiedProperties = new ArrayList<>();
            PropertyChangeListener onModelPropertyChanged = ev -> {
                LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: onModelPropertyChanged: ev.name=" + ev.getPropertyName());
                modifiedProperties.add(ev.getPropertyName());
            };

            model.addListener(onModelPropertyChanged);

            modifiedProperties.clear();
            model.setSize(new SizeDouble(123, 456));
            LoggerSimple.put("  mosaicDrawModelPropertyChangedTest: checking...");
            Assert.assertTrue(modifiedProperties.contains(IImageModel.PROPERTY_SIZE));

            model.removeListener(onModelPropertyChanged);
        }
    }

    @Test
    public void mosaicDrawModelTest() {
        try (MosaicDrawModel<DummyMosaicImageType> model = new MosaicDrawModel<>()) {
            Assert.assertEquals(model.getCellAttr().getSize(model.getSizeField()), model.getSize());
        }
    }
}
