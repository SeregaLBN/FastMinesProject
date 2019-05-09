using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.notifier;

namespace fmg.core.mosaic {

    public class MosaicModelNUnitTest : MosaicModelTest {

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
        protected override void AssertFalse(bool condition) {
            Assert.IsFalse(condition);
        }
        protected override void AssertLessOrEqual(int valToBeLess, int valToBeGreater) {
            Assert.LessOrEqual(valToBeLess, valToBeGreater);
        }


        [OneTimeSetUp]
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
        public override async Task MosaicGameModelPropertyChangedTest() {
            await base.MosaicGameModelPropertyChangedTest();
        }

        [Test]
      //[Retry(100)]
        public override async Task MosaicDrawModelPropertyChangedTest() {
            await base.MosaicDrawModelPropertyChangedTest();
        }

        [Test]
        public override async Task MosaicDrawModelAsIsTest() {
            await base.MosaicDrawModelAsIsTest();
        }

        [Test]
        public override async Task AutoFitTrueCheckAffectsToPaddingTest() {
            await base.AutoFitTrueCheckAffectsToPaddingTest();
        }

        [Test]
        public override async Task AutoFitTrueCheckAffectsTest() {
            await base.AutoFitTrueCheckAffectsTest();
        }

        [Test]
        public override async Task AutoFitFalseCheckAffectsTest() {
            await base.AutoFitFalseCheckAffectsTest();
        }

        [Test]
        public override async Task MosaicNoChangedTest() {
            await base.MosaicNoChangedTest();
        }

    }

}
