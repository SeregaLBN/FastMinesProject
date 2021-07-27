using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Fmg.Uwp.App;

namespace Fmg.Common.Notifier {

    public class PropertyChangeExecutorUwpTest : PropertyChangeExecutorTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
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
        protected override void AssertFail() {
            Assert.Fail();
        }

        [TestInitialize]
        public override void Setup() {
            base.Setup();
            ProjSettings.Init();
        }

        [TestMethod]
        public override async Task SimpleUsageTest() {
            await base.SimpleUsageTest();
        }

        [TestMethod]
        public override async Task ExtendedUsageTest() {
            await base.ExtendedUsageTest();
        }

        [TestMethod]
        public override async Task CreatorFailTest() {
            await base.CreatorFailTest();
        }

        [TestMethod]
        public override async Task ModificatorFailTest() {
            await base.ModificatorFailTest();
        }

        [TestMethod]
        public override async Task ValidatorFailTest() {
            await base.ValidatorFailTest();
        }

    }

}
