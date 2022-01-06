package fmg.android.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import io.reactivex.Flowable;

import fmg.core.mosaic.MosaicViewTest;
import fmg.android.utils.ProjSettings;

public class MosaicViewAndroidTest extends MosaicViewTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicViewAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(Logger::info);
    }

}
