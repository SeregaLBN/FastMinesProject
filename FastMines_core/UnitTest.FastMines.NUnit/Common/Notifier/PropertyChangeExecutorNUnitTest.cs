using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.ui;

namespace fmg.common.notifier {

    public class PropertyChangeExecutorNUnitTest : PropertyChangeExecutorTest {

        internal static void StaticInitializer() {
            UiInvoker.Deferred = SimpleUiThreadLoop.AddTask;
            LoggerSimple.Put("Simple UI factory inited...");
        }

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertNotNull(object anObject) {
            Assert.NotNull(anObject);
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
        public override async Task SimpleUsageTest() {
            await base.SimpleUsageTest();
        }

        [Test]
        public override async Task ExtendedUsageTest() {
            await base.ExtendedUsageTest();
        }

        [Test]
        public override async Task CreatorFailTest() {
            await base.CreatorFailTest();
        }

        [Test]
        public override async Task ModificatorFailTest() {
            await base.ModificatorFailTest();
        }

        [Test]
        public override async Task ValidatorFailTest() {
            await base.ValidatorFailTest();
        }

    }

}
