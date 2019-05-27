using System;
using System.Threading.Tasks;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.UI;
using Fmg.Common.Notifier;
using Fmg.Core.Types;

namespace Fmg.Core.Mosaic {

    public abstract class MosaicHelperTest {

        protected abstract void AssertEqual(object expected, object actual);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertLess(double valToBeLess, double valToBeGreater);
        protected abstract void AssertGreaterOrEqual(double valToBeGreater, double valToBeLess);

        public virtual void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicHelperTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            LoggerSimple.Put("======================================================");
        }

        public virtual void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(MosaicHelperTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual void FindSizeByArea_eMosaicSquare1_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindSizeByArea_eMosaicSquare1_Test));

            {
                var sizeClient = new SizeDouble(100, 100);
                var sizeField = MosaicHelper.FindSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

                AssertEqual(new Matrisize(10, 10), sizeField);
            }
            {
                var sizeClient = new SizeDouble(500, 500);
                var sizeField = MosaicHelper.FindSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

                AssertEqual(new Matrisize(50, 50), sizeField);
            }
            {
                var sizeClient = new SizeDouble(300, 700);
                var sizeField = MosaicHelper.FindSizeByArea(EMosaic.eMosaicSquare1, 100, sizeClient);

                AssertEqual(new Matrisize(30, 70), sizeField);
            }
        }

        public virtual void FindAreaBySize_eMosaicSquare1_Test1() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicSquare1_Test1));

            var sizeClientIn = new SizeDouble(100, 100);
            var sizeClientOut = sizeClientIn;
            var area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), ref sizeClientOut);

            // AssertEqual(100, area);
            AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 100 - area);

            // AssertEqual(sizeClientIn, sizeClientOut);
            AssertGreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Width - sizeClientOut.Width);
            AssertGreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Height - sizeClientOut.Height);
        }

        public virtual void FindAreaBySize_eMosaicSquare1_Test2() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicSquare1_Test2));

            var sizeClientIn = new SizeDouble(200, 200);
            var sizeClientOut = sizeClientIn;
            var area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 20), ref sizeClientOut);

            // AssertEqual(100, area);
            AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 100 - area);

            // AssertEqual(new SizeDouble(100, 200), sizeClientOut);
            AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 100 - sizeClientOut.Width);
            AssertGreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Height - sizeClientOut.Height);
        }

        public virtual void FindAreaBySize_eMosaicSquare1_Test3() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicSquare1_Test3));

            {
                var sizeClientIn = new SizeDouble(200, 400);
                var sizeClientOut = sizeClientIn;
                double area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), ref sizeClientOut);

                // AssertEquals(100, area);
                AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 400 - area);

                // AssertEquals(new SizeDouble(100, 200), sizeClientOut);
                AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 200 - sizeClientOut.Width);
                AssertGreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Width - sizeClientOut.Width);
            }
            {
                var sizeClientIn = new SizeDouble(400, 200);
                var sizeClientOut = sizeClientIn;
                double area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicSquare1, new Matrisize(10, 10), ref sizeClientOut);

                // AssertEquals(100, area);
                AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 400 - area);

                // AssertEquals(new SizeDouble(100, 200), sizeClientOut);
                AssertGreaterOrEqual(MosaicHelper.AreaPrecision, 200 - sizeClientOut.Height);
                AssertGreaterOrEqual(MosaicHelper.AreaPrecision, sizeClientIn.Height - sizeClientOut.Height);
            }
        }

        public virtual void FindAreaBySize_Random_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_Random_Test));

            var r = ThreadLocalRandom.Current;

            for (var i = 0; i < 1000; ++i) {
                var sizeClientIn = new SizeDouble(100 + r.Next(500), 100 + r.Next(500));
                var sizeClientOut = sizeClientIn;
                var mosaicType = EMosaicEx.FromOrdinal(r.Next(EMosaicEx.GetValues().Length));
                var mSize = new Matrisize(5 + r.Next(30), 5 + r.Next(30));
                var area = MosaicHelper.FindAreaBySize(mosaicType, mSize, ref sizeClientOut);

                // AssertEqual(100, area);
                //AssertGreaterOrEqual(MosaicHelper.AreaPrecision, ??? - area);
                AssertLess(0, area);

                var magicNumber = 8;
                AssertTrue((MosaicHelper.AreaPrecision >= (sizeClientIn.Width  - sizeClientOut.Width ) / magicNumber) ||
                           (MosaicHelper.AreaPrecision >= (sizeClientIn.Height - sizeClientOut.Height) / magicNumber));
            }
        }

        public virtual async Task FindAreaBySize_eMosaicTrapezoid3_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicTrapezoid3_Test));

            var signal = new Signal();
            double area = -1;
            UiInvoker.Deferred(() => {
                var sizeClientIn = new SizeDouble(169.90442448471225, 313.90196868082262);
                SizeDouble sizeClientOut = sizeClientIn;
                area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicTrapezoid3, new Matrisize(4, 2), ref sizeClientOut);

                signal.Set();
            });

            AssertTrue(await signal.Wait(TimeSpan.FromSeconds(1)));
            AssertGreaterOrEqual(area, 0);
        }

        public virtual void FindAreaBySize_eMosaicTriangle1_Test() {
            LoggerSimple.Put(">" + nameof(MosaicHelperTest) + "::" + nameof(FindAreaBySize_eMosaicTriangle1_Test));

            var sizeClientIn = new SizeDouble(186.89486693318347, 294.28309563827116);
            var sizeClientOut = sizeClientIn;
            double area = MosaicHelper.FindAreaBySize(EMosaic.eMosaicTriangle1, new Matrisize(3, 2), ref sizeClientOut);
            AssertGreaterOrEqual(area, 0);
        }

    }

}
