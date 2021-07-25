package fmg.android.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import io.reactivex.Flowable;

import fmg.core.mosaic.MosaicModelTest;
import fmg.android.utils.ProjSettings;

public class MosaicModelAndroidTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicModelAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(Logger::info);
    }

}
