package fmg.swing.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicModelTest;
import fmg.swing.utils.ProjSettings;
import io.reactivex.Flowable;

public class MosaicModelSwingTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicModelSwingTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory SWING inited...").subscribe(LoggerSimple::put);
    }

}
