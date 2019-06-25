package fmg.common.notifier;

import org.junit.BeforeClass;

import fmg.common.Logger;
import fmg.jfx.utils.ProjSettings;
import io.reactivex.Flowable;

public class NotifyPropertyChangedJfxTest extends NotifyPropertyChangedTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> NotifyPropertyChangedJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        ProjSettings.init();
        Flowable.just("UI factory JFX inited...").subscribe(Logger::info);
    }

}
