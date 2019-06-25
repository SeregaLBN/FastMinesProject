package fmg.android.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import io.reactivex.Flowable;

import fmg.core.mosaic.MosaicControllerTest;
import fmg.android.utils.ProjSettings;

public class MosaicControllerAndroidTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicControllerAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(Logger::info);
    }

}
