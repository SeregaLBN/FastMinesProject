package fmg.jfx.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicControllerTest;
import fmg.jfx.utils.StaticInitializer;
import io.reactivex.Flowable;

public class MosaicControllerJfxTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicControllerJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        StaticInitializer.init();
        Flowable.just("UI factory JFX inited...").subscribe(LoggerSimple::put);
    }

}
