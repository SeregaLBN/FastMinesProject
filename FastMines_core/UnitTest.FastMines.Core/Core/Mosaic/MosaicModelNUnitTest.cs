using System.Threading.Tasks;
using NUnit.Framework;

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
        public override void MosaicDrawModelAsIsTest() {
            base.MosaicDrawModelAsIsTest();
        }

        [Test]
        public override void AutoFitTrueCheckAffectsToPaddingTest() {
            base.AutoFitTrueCheckAffectsToPaddingTest();
        }

        [Test]
        public override void AutoFitTrueCheckAffectsTest() {
            base.AutoFitTrueCheckAffectsTest();
        }

        [Test]
        public override void AutoFitFalseCheckAffectsTest() {
            base.AutoFitFalseCheckAffectsTest();
        }

        [Test]
        public override async Task MosaicNoChangedTest() {
            await base.MosaicNoChangedTest();
        }

    }

}
