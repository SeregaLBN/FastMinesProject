package fmg.swing.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import fmg.core.mosaic.MosaicControllerTest;
import fmg.swing.utils.ProjSettings;
import io.reactivex.Flowable;

public class MosaicControllerSwingTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicControllerSwingTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory SWING inited...").subscribe(Logger::info);
    }

}
