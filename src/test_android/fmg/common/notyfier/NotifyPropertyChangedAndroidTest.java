package fmg.common.notifier;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import org.junit.BeforeClass;

import io.reactivex.Flowable;

import fmg.common.Logger;
import fmg.android.utils.ProjSettings;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class NotifyPropertyChangedAndroidTest extends NotifyPropertyChangedTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> NotifyPropertyChangedAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(Logger::info);
    }

}
