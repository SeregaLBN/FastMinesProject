package fmg.android.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicModelTest;
import fmg.android.utils.StaticInitializer;
import io.reactivex.Flowable;

public class MosaicModelAndroidTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicModelAndroidTest::setup");
        StaticInitializer.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
