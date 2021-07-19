package fmg.swing.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import fmg.core.mosaic.MosaicModelTest;
import fmg.swing.app.ProjSettings;
import io.reactivex.Flowable;

public class MosaicModelSwingTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicModelSwingTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory SWING inited...").subscribe(Logger::info);
    }

}
