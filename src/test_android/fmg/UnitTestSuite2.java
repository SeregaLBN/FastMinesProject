package fmg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fmg.android.mosaic.MosaicModelAndroidTest;
import fmg.android.mosaic.MosaicViewAndroidTest;
import fmg.common.notyfier.NotifyPropertyChangedAndroidTest;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({NotifyPropertyChangedAndroidTest.class,
                     MosaicModelAndroidTest.class,
                     MosaicViewAndroidTest.class,
                     MosaicModelAndroidTest.class})
public class UnitTestSuite2 {

}