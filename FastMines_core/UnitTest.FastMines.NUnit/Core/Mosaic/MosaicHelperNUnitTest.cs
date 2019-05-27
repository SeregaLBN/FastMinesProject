using System.Threading.Tasks;
using NUnit.Framework;
using Fmg.Common.Notifier;

namespace Fmg.Core.Mosaic {

    public class MosaicHelperNUnitTest : MosaicHelperTest {

        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        protected override void AssertLess(double valToBeLess, double valToBeGreater) {
            Assert.Less(valToBeLess, valToBeGreater);
        }
        protected override void AssertGreaterOrEqual(double valToBeGreater, double valToBeLess) {
            Assert.GreaterOrEqual(valToBeGreater, valToBeLess);
        }

        [OneTimeSetUp]
        public override void Setup() {
            base.Setup();
            NotifyPropertyChangedNUnitTest.ProjSettings();
        }

        [SetUp]
        public override void Before() {
            base.Before();
        }

        [OneTimeTearDown]
        public override void After() {
            base.After();
        }

        [Test]
        public override void FindSizeByArea_eMosaicSquare1_Test() {
            base.FindSizeByArea_eMosaicSquare1_Test();
        }

        [Test]
        public override void FindAreaBySize_eMosaicSquare1_Test1() {
            base.FindAreaBySize_eMosaicSquare1_Test1();
        }

        [Test]
        public override void FindAreaBySize_eMosaicSquare1_Test2() {
            base.FindAreaBySize_eMosaicSquare1_Test2();
        }

        [Test]
        public override void FindAreaBySize_eMosaicSquare1_Test3() {
            base.FindAreaBySize_eMosaicSquare1_Test3();
        }

        [Test]
        public override void FindAreaBySize_Random_Test() {
            base.FindAreaBySize_Random_Test();
        }

        [Test]
        public override async Task FindAreaBySize_eMosaicTrapezoid3_Test() {
            await base.FindAreaBySize_eMosaicTrapezoid3_Test();
        }

        [Test]
        public override void FindAreaBySize_eMosaicTriangle1_Test() {
            base.FindAreaBySize_eMosaicTriangle1_Test();
        }

    }

}
