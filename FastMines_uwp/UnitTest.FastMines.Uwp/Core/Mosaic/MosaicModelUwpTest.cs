using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Fmg.Uwp.Utils;

namespace Fmg.Core.Mosaic {

    [TestClass]
    public class MosaicModelUwpTest : MosaicModelTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(double expected, double actual, double delta) {
            Assert.AreEqual(expected, actual, delta);
        }
        protected override void AssertNotNull(object anObject) {
            Assert.IsNotNull(anObject);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        protected override void AssertFalse(bool condition) {
            Assert.IsFalse(condition);
        }
        protected override void AssertLessOrEqual(int valToBeLess, int valToBeGreater) {
            Assert.IsTrue(valToBeLess <= valToBeGreater);
        }


        [TestInitialize]
        public override void Setup() {
            base.Setup();
            ProjSettings.Init();
        }

        [TestMethod]
        public override async Task MosaicGameModelPropertyChangedTest() {
            await base.MosaicGameModelPropertyChangedTest();
        }

        [TestMethod]
        //[Retry(100)]
        public override async Task MosaicDrawModelPropertyChangedTest() {
            await base.MosaicDrawModelPropertyChangedTest();
        }

        [TestMethod]
        public override async Task MosaicDrawModelAsIsTest() {
            await base.MosaicDrawModelAsIsTest();
        }

        [TestMethod]
        public override async Task AutoFitTrueCheckAffectsToPaddingTest() {
            await base.AutoFitTrueCheckAffectsToPaddingTest();
        }

        [TestMethod]
        public override async Task AutoFitTrueCheckAffectsTest() {
            await base.AutoFitTrueCheckAffectsTest();
        }

        [TestMethod]
        public override async Task AutoFitFalseCheckAffectsTest() {
            await base.AutoFitFalseCheckAffectsTest();
        }

        [TestMethod]
        public override async Task MosaicNoChangedTest() {
            await base.MosaicNoChangedTest();
        }

        [TestMethod]
        public override async Task NoChangeOffsetTest() {
            await base.NoChangeOffsetTest();
        }

    }

}
