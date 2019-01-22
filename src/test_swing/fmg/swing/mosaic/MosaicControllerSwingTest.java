package fmg.swing.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicControllerTest;
import fmg.swing.utils.StaticInitializer;
import io.reactivex.Flowable;

public class MosaicControllerSwingTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicControllerSwingTest::setup");
        StaticInitializer.init();
        Flowable.just("UI factory SWING inited...").subscribe(LoggerSimple::put);
    }

}
