using System.Threading.Tasks;
using NUnit.Framework;

namespace fmg.common.notifier {

    public class NotifyPropertyChangedNUnitTest : NotifyPropertyChangedTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }

        [OneTimeSetUp]
        public override void Setup() {
            base.Setup();
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

    }

}
