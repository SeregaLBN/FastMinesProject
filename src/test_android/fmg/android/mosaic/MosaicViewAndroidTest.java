package fmg.android.mosaic;

import org.junit.BeforeClass;
import io.reactivex.Flowable;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicViewTest;
import fmg.android.utils.ProjSettings;

public class MosaicViewAndroidTest extends MosaicViewTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicViewAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
