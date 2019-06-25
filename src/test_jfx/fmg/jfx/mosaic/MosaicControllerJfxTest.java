package fmg.jfx.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import fmg.core.mosaic.MosaicControllerTest;
import fmg.jfx.utils.ProjSettings;
import io.reactivex.Flowable;

public class MosaicControllerJfxTest extends MosaicControllerTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicControllerJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        ProjSettings.init();
        Flowable.just("UI factory JFX inited...").subscribe(Logger::info);
    }

}
