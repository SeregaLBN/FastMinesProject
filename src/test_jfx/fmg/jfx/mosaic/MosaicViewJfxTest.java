package fmg.jfx.mosaic;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicViewTest;
import fmg.jfx.utils.StaticInitializer;
import io.reactivex.Flowable;

public class MosaicViewJfxTest extends MosaicViewTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicViewJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        StaticInitializer.init();
        Flowable.just("UI factory JFX inited...").subscribe(LoggerSimple::put);
    }

}
