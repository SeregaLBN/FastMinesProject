using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Fmg.Uwp.Utils;

namespace Fmg.Common.Notifier {

    [TestClass]
    public class NotifyPropertyChangedUwpTest : NotifyPropertyChangedTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
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
        public override void NotifyPropertyChangedSyncTest() {
            base.NotifyPropertyChangedSyncTest();
        }

        [TestMethod]
        public override async Task NotifyPropertyChangedAsyncTest() {
            await base.NotifyPropertyChangedAsyncTest();
        }

        [TestMethod]
        public override async Task CheckForNoEventTest() {
            await base.CheckForNoEventTest();
        }

        [TestMethod]
        public override void ForgotToUnsubscribeTest() {
            base.ForgotToUnsubscribeTest();
        }

    }

}
