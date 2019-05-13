using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using fmg.uwp.utils;

namespace fmg.core.mosaic {

    [TestClass]
    public class MosaicControllerUwpTest : MosaicControllerTest {

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

        [TestInitialize]
        public override void Setup() {
            base.Setup();
            ProjSettings.Init();
        }

        [TestMethod]
        public override async Task PropertyChangedTest() {
            await base.PropertyChangedTest();
        }

        [TestMethod]
        public override async Task ReadinessAtTheStartTest() {
            await base.ReadinessAtTheStartTest();
        }

    }

}
