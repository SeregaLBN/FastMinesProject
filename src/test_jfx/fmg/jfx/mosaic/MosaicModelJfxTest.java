package fmg.jfx.mosaic;

import org.junit.BeforeClass;

import fmg.common.Logger;
import fmg.core.mosaic.MosaicModelTest;
import fmg.jfx.utils.ProjSettings;
import io.reactivex.Flowable;

public class MosaicModelJfxTest extends MosaicModelTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicModelJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        ProjSettings.init();
        Flowable.just("UI factory JFX inited...").subscribe(Logger::info);
    }

}
