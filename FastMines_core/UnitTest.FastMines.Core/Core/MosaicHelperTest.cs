using System;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.core.types;
using fmg.common.geom;
using fmg.common;
using fmg.common.ui;

namespace fmg.core.mosaic {

    public class MosaicHelperTest {

        [SetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> MosaicHelperTest::Setup");

            MosaicModelTest.StaticInitializer();
        }

        [Test]
        public void FindSizeByArea_eMosaicSquare1_Test() {
            {
                var sizeClient = new SizeDouble(100, 100);
                var sizeField = MosaicHelper.FindSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

                Assert.AreEqual(new Matrisize(10, 10), sizeField);
            }
            {
                var sizeClient = new SizeDouble(500, 500);
                var sizeField = MosaicHelper.FindSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

                Assert.AreEqual(new Matrisize(50, 50), sizeField);
            }
            {
                var sizeClient = new SizeDouble(300, 700);
                var sizeField = MosaicHelper.FindSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

                Assert.AreEqual(new Matrisize(30, 70), sizeField);
            }
        }

        [Test]
        public void FindAreaBySize_eMosaicSquare1_Test1() {
            var sizeClientIn = new SizeDouble(100, 100);
            var sizeClientOut = sizeClientIn;
            var area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), ref sizeClientOut);

            // Assert.AreEqual(100, area);
            Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 100 - area);

            // Assert.AreEqual(sizeClientIn, sizeClientOut);
            Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Width - sizeClientOut.Width);
            Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Height - sizeClientOut.Height);
        }

        [Test]
        public void FindAreaBySize_eMosaicSquare1_Test2() {
            var sizeClientIn = new SizeDouble(200, 200);
            var sizeClientOut = sizeClientIn;
            var area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 20), ref sizeClientOut);

            // Assert.AreEqual(100, area);
            Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 100 - area);

            // Assert.AreEqual(new SizeDouble(100, 200), sizeClientOut);
            Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 100 - sizeClientOut.Width);
            Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Height - sizeClientOut.Height);
        }

        [Test]
        public void FindAreaBySize_Random_Test() {
            var r = ThreadLocalRandom.Current;

            for (var i = 0; i < 1000; ++i) {
                var sizeClientIn = new SizeDouble(100 + r.Next(500), 100 + r.Next(500));
                var sizeClientOut = sizeClientIn;
                var mosaicType = EMosaicEx.FromOrdinal(r.Next(EMosaicEx.GetValues().Length));
                var mSize = new Matrisize(5 + r.Next(30), 5 + r.Next(30));
                var area = MosaicHelper.FindAreaBySize(mosaicType, mSize, ref sizeClientOut);

                // Assert.AreEqual(100, area);
                //Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, ??? - area);
                Assert.Less(0, area);

                var magicNumber = 8;
                Assert.IsTrue((MosaicHelper.AreaPrecision >= (sizeClientIn.Width  - sizeClientOut.Width ) / magicNumber) ||
                              (MosaicHelper.AreaPrecision >= (sizeClientIn.Height - sizeClientOut.Height) / magicNumber));
            }
        }


        [Test]
        public async Task FindAreaBySize_eMosaicTrapezoid3_Test() {
            var signal = new Signal();
            double area = -1;
            Factory.DEFERR_INVOKER(() => {
                var sizeClientIn = new SizeDouble(169.90442448471225, 313.90196868082262);
                SizeDouble sizeClientOut = sizeClientIn;
                area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicTrapezoid3, new Matrisize(4, 2), ref sizeClientOut);

                signal.Set();
            });

            Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(1)));
            Assert.IsTrue(area > 0);
        }

    }

}
