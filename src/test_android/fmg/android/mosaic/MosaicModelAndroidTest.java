package fmg.android.mosaic;

import org.junit.BeforeClass;
import io.reactivex.Flowable;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicModelTest;
import fmg.android.utils.ProjSettings;

public class MosaicModelAndroidTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicModelAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
