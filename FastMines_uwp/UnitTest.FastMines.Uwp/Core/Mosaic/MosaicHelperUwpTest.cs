using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using fmg.uwp.utils;

namespace fmg.core.mosaic {

    [TestClass]
    public class MosaicHelperUwpTest : MosaicHelperTest {

        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        protected override void AssertLess(double valToBeLess, double valToBeGreater) {
            Assert.IsTrue(valToBeLess < valToBeGreater);
        }
        protected override void AssertGreaterOrEqual(double valToBeGreater, double valToBeLess) {
            Assert.IsTrue(valToBeGreater >= valToBeLess);
        }

        [TestInitialize]
        public override void Setup() {
            base.Setup();
            ProjSettings.Init();
        }

        [TestMethod]
        public override void FindSizeByArea_eMosaicSquare1_Test() {
            base.FindSizeByArea_eMosaicSquare1_Test();
        }

        [TestMethod]
        public override void FindAreaBySize_eMosaicSquare1_Test1() {
            base.FindAreaBySize_eMosaicSquare1_Test1();
        }

        [TestMethod]
        public override void FindAreaBySize_eMosaicSquare1_Test2() {
            base.FindAreaBySize_eMosaicSquare1_Test2();
        }

        [TestMethod]
        public override void FindAreaBySize_eMosaicSquare1_Test3() {
            base.FindAreaBySize_eMosaicSquare1_Test3();
        }

        [TestMethod]
        public override void FindAreaBySize_Random_Test() {
            base.FindAreaBySize_Random_Test();
        }

        [TestMethod]
        public override async Task FindAreaBySize_eMosaicTrapezoid3_Test() {
            await base.FindAreaBySize_eMosaicTrapezoid3_Test();
        }

        [TestMethod]
        public override void FindAreaBySize_eMosaicTriangle1_Test() {
            base.FindAreaBySize_eMosaicTriangle1_Test();
        }

    }

}
