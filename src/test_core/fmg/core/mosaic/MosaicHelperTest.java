package fmg.core.mosaic;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.Signal;
import fmg.common.ui.Factory;
import fmg.core.types.EMosaic;

public class MosaicHelperTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicHelperTest::setup");

        MosaicModelTest.StaticInitializer();
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }
    @AfterClass
    public static void after() {
        LoggerSimple.put("======================================================");
        LoggerSimple.put("< MosaicHelperTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }


    @Test
    public void findSizeByArea_eMosaicSquare1_Test() {
        {
            SizeDouble sizeClient = new SizeDouble(100, 100);
            Matrisize sizeField = MosaicHelper.findSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

            Assert.assertEquals(new Matrisize(10, 10), sizeField);
        }
        {
            SizeDouble sizeClient = new SizeDouble(500, 500);
            Matrisize sizeField = MosaicHelper.findSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

            Assert.assertEquals(new Matrisize(50, 50), sizeField);
        }
        {
            SizeDouble sizeClient = new SizeDouble(300, 700);
            Matrisize sizeField = MosaicHelper.findSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

            Assert.assertEquals(new Matrisize(30, 70), sizeField);
        }
    }

    @Test
    public void findAreaBySize_eMosaicSquare1_Test1() {
        SizeDouble sizeClientIn = new SizeDouble(100, 100);
        SizeDouble sizeClientOut = new SizeDouble();
        double area = MosaicHelper.findAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), sizeClientIn, sizeClientOut);

        // Assert.assertEquals(100, area);
        Assert.assertTrue(MosaicHelper.AreaPrecision >= (100 - area));

        // Assert.assertEquals(sizeClientIn, sizeClientOut);
        Assert.assertTrue(MosaicHelper.AreaPrecision >= (sizeClientIn.width  - sizeClientOut.width));
        Assert.assertTrue(MosaicHelper.AreaPrecision >= (sizeClientIn.height - sizeClientOut.height));
    }

    @Test
    public void findAreaBySize_eMosaicSquare1_Test2() {
        SizeDouble sizeClientIn = new SizeDouble(200, 200);
        SizeDouble sizeClientOut = new SizeDouble();
        double area = MosaicHelper.findAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 20), sizeClientIn, sizeClientOut);

        // Assert.assertEquals(100, area);
        Assert.assertTrue(MosaicHelper.AreaPrecision >= (100 - area));

        // Assert.assertEquals(new SizeDouble(100, 200), sizeClientOut);
        Assert.assertTrue(MosaicHelper.AreaPrecision >= (100 - sizeClientOut.width));
        Assert.assertTrue(MosaicHelper.AreaPrecision >= (sizeClientIn.height - sizeClientOut.height));
    }

    @Test
    public void findAreaBySize_Random_Test() {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        for (int i = 0; i < 1000; ++i) {
            SizeDouble sizeClientIn = new SizeDouble(100 + r.nextInt(500), 100 + r.nextInt(500));
            SizeDouble sizeClientOut = new SizeDouble();
            EMosaic mosaicType = EMosaic.fromOrdinal(r.nextInt(EMosaic.values().length));
            Matrisize mSize = new Matrisize(5 + r.nextInt(30), 5 + r.nextInt(30));
            double area = MosaicHelper.findAreaBySize(mosaicType, mSize, sizeClientIn, sizeClientOut);

            // Assert.assertEquals(100, area);
            //Assert.assertTrue(MosaicHelper.AreaPrecision >= (??? - area));
            Assert.assertTrue(area > 0);

            int magicNumber = 8;
            Assert.assertTrue((MosaicHelper.AreaPrecision >= (sizeClientIn.width  - sizeClientOut.width )/magicNumber) ||
                              (MosaicHelper.AreaPrecision >= (sizeClientIn.height - sizeClientOut.height)/magicNumber));
        }
    }

    @Test
    public void findAreaBySize_eMosaicTrapezoid3_Test() {
        Signal signal = new Signal();
        double[] area = { -1 };
        Factory.DEFERR_INVOKER.accept(() -> {
            SizeDouble sizeClientIn = new SizeDouble(169.90442448471225, 313.90196868082262);
            SizeDouble sizeClientOut = new SizeDouble();
            area[0] = MosaicHelper.findAreaBySize(EMosaic.eMosaicTrapezoid3, new Matrisize(4, 2), sizeClientIn, sizeClientOut);

            signal.set();
        });

        Assert.assertTrue(signal.await(1000));
        Assert.assertTrue(area[0] > 0);
    }

}
