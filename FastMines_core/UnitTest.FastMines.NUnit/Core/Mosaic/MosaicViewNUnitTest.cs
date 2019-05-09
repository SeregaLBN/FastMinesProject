using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.notifier;

namespace fmg.core.mosaic {

    public class MosaicViewNUnitTest : MosaicViewTest {

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
            Assert.NotNull(anObject);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        protected override void AssertNotEqual(object expected, object actual) {
            Assert.AreNotEqual(expected, actual);
        }

        [SetUp]
        public override void Setup() {
            base.Setup();
            NotifyPropertyChangedNUnitTest.StaticInitializer();
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
        public override async Task PropertyChangedTest() {
            await base.PropertyChangedTest();
        }

        [Test]
        public override async Task ReadinessAtTheStartTest() {
            await base.ReadinessAtTheStartTest();
        }

        [Test]
        public override async Task MultipleChangeModelOneDrawViewTest() {
            await base.MultipleChangeModelOneDrawViewTest();
        }

        [Test]
        public override async Task OneNotificationOfImageChangedTest() {
            await base.OneNotificationOfImageChangedTest();
        }

    }

}
