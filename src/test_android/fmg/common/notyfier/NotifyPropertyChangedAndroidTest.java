package fmg.common.notifier;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.reactivex.Flowable;

import fmg.common.LoggerSimple;
import fmg.android.utils.ProjSettings;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class NotifyPropertyChangedAndroidTest extends NotifyPropertyChangedTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> NotifyPropertyChangedAndroidTest::setup");
        ProjSettings.init();
        Flowable.just("UI factory Android inited...").subscribe(LoggerSimple::put);
    }

}
