using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.ui;

namespace fmg.common.notifier {

    public class NotifyPropertyChangedNUnitTest : NotifyPropertyChangedTest {

        internal static void StaticInitializer() {
            UiInvoker.Deferred = SimpleUiThreadLoop.AddTask;
            LoggerSimple.Put("Simple UI factory inited...");
        }

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

        [OneTimeSetUp]
        public override void Setup() {
            base.Setup();
            StaticInitializer();
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
        public override void NotifyPropertyChangedSyncTest() {
            base.NotifyPropertyChangedSyncTest();
        }

        [Test]
        public override async Task NotifyPropertyChangedAsyncTest() {
            await base.NotifyPropertyChangedAsyncTest();
        }

        [Test]
        public override async Task CheckForNoEventTest() {
            await base.CheckForNoEventTest();
        }

        [Test]
        public override void ForgotToUnsubscribeTest() {
            base.ForgotToUnsubscribeTest();
        }

    }

}
