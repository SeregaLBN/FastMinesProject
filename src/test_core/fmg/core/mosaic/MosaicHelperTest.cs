using System;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common;
using fmg.common.geom;
using fmg.common.ui;
using fmg.common.notifier;
using fmg.core.types;

namespace fmg.core.mosaic {

    public class MosaicHelperTest {

        [SetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicHelperTest) + "::" + nameof(Setup));

            MosaicModelTest.StaticInitializer();
        }

        [Test]
        public void FindSizeByArea_eMosaicSquare1_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindSizeByArea_eMosaicSquare1_Test));

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
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicSquare1_Test1));

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
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicSquare1_Test2));

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
        public void FindAreaBySize_eMosaicSquare1_Test3() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicSquare1_Test3));

            {
                var sizeClientIn = new SizeDouble(200, 400);
                var sizeClientOut = sizeClientIn;
                double area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), ref sizeClientOut);

                // Assert.AreEquals(100, area);
                Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 400 - area);

                // Assert.assertEquals(new SizeDouble(100, 200), sizeClientOut);
                Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 200 - sizeClientOut.Width);
                Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Width - sizeClientOut.Width);
            }
            {
                var sizeClientIn = new SizeDouble(400, 200);
                var sizeClientOut = sizeClientIn;
                double area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), ref sizeClientOut);

                // Assert.assertEquals(100, area);
                Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 400 - area);

                // Assert.assertEquals(new SizeDouble(100, 200), sizeClientOut);
                Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, 200 - sizeClientOut.Height);
                Assert.GreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Height - sizeClientOut.Height);
            }
        }

        [Test]
        public void FindAreaBySize_Random_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_Random_Test));

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
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicTrapezoid3_Test));

            var signal = new Signal();
            double area = -1;
            UiInvoker.Deferred(() => {
                var sizeClientIn = new SizeDouble(169.90442448471225, 313.90196868082262);
                SizeDouble sizeClientOut = sizeClientIn;
                area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicTrapezoid3, new Matrisize(4, 2), ref sizeClientOut);

                signal.Set();
            });

            Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(1)));
            Assert.GreaterOrEqual(area, 0);
        }

        [Test]
        public void findAreaBySize_eMosaicTriangle1_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(findAreaBySize_eMosaicTriangle1_Test));

            var sizeClientIn = new SizeDouble(186.89486693318347, 294.28309563827116);
            var sizeClientOut = sizeClientIn;
            double area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicTriangle1, new Matrisize(3, 2), ref sizeClientOut);
            Assert.GreaterOrEqual(area, 0);
        }

    }

}
