package fmg.swing.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicViewTest;
import fmg.swing.utils.ProjSettings;
import io.reactivex.Flowable;

public class MosaicViewSwingTest extends MosaicViewTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicViewSwingTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory SWING inited...").subscribe(LoggerSimple::put);
    }

}
