package fmg.common.notifier;

import org.junit.BeforeClass;

import fmg.common.LoggerSimple;
import fmg.common.notifier.NotifyPropertyChangedTest;
import fmg.swing.utils.StaticInitializer;
import io.reactivex.Flowable;

public class NotifyPropertyChangedSwingTest extends NotifyPropertyChangedTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> NotifyPropertyChangedSwingTest::setup");
        StaticInitializer.init();
        Flowable.just("UI factory SWING inited...").subscribe(LoggerSimple::put);
    }

}
