package fmg.android.mosaic;

import org.junit.BeforeClass;
import io.reactivex.Flowable;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicControllerTest;
import fmg.android.utils.ProjSettings;

public class MosaicControllerAndroidTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicControllerAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
