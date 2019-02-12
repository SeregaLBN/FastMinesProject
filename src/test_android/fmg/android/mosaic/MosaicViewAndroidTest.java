package fmg.android.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicViewTest;
import fmg.android.utils.StaticInitializer;
import io.reactivex.Flowable;

public class MosaicViewAndroidTest extends MosaicViewTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicViewAndroidTest::setup");
        StaticInitializer.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
