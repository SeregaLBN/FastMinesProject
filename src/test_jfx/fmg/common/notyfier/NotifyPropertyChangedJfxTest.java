package fmg.common.notyfier;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.jfx.utils.StaticInitializer;
import io.reactivex.Flowable;

public class NotifyPropertyChangedJfxTest extends NotifyPropertyChangedTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> NotifyPropertyChangedJfxTest::setup");
        new javafx.embed.swing.JFXPanel();
        StaticInitializer.init();
        Flowable.just("UI factory JFX inited...").subscribe(LoggerSimple::put);
    }

}
