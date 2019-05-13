package fmg.jfx.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicModelTest;
import fmg.jfx.utils.ProjSettings;
import io.reactivex.Flowable;

public class MosaicModelJfxTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicModelJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        ProjSettings.init();
        Flowable.just("UI factory JFX inited...").subscribe(LoggerSimple::put);
    }

}
