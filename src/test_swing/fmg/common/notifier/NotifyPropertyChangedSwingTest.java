package fmg.common.notifier;

import org.junit.BeforeClass;

import fmg.common.Logger;
import fmg.swing.app.ProjSettings;
import io.reactivex.Flowable;

public class NotifyPropertyChangedSwingTest extends NotifyPropertyChangedTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> NotifyPropertyChangedSwingTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory SWING inited...").subscribe(Logger::info);
    }

}
