package fmg.android.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicControllerTest;
import fmg.android.utils.StaticInitializer;
import io.reactivex.Flowable;

public class MosaicControllerAndroidTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicControllerAndroidTest::setup");
        StaticInitializer.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
