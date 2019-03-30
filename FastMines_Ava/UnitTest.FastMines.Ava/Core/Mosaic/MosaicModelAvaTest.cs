using System.Threading.Tasks;
using Xunit;
using fmg.ava.utils;

namespace fmg.core.mosaic {

    public class MosaicModelAvaTest : MosaicModelTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.Equal(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.Equal(expected, actual);
        }
        protected override void AssertEqual(double expected, double actual, double delta) {
            Assert.Equal(expected, actual, new DoubleComparer(delta));
        }
        protected override void AssertNotNull(object anObject) {
            Assert.NotNull(anObject);
        }
        protected override void AssertTrue(bool condition) {
            Assert.True(condition);
        }
        protected override void AssertFalse(bool condition) {
            Assert.False(condition);
        }
        protected override void AssertLessOrEqual(int valToBeLess, int valToBeGreater) {
            Assert.True(valToBeLess <= valToBeGreater);
        }


        private bool _tuned;

        public override void Setup() {
            if (_tuned)
                return;
            base.Setup();
            _tuned = true;
            StaticInitializer.Init();
        }

        [Fact]
        public override async Task MosaicGameModelPropertyChangedTest() {
            Setup();
            await base.MosaicGameModelPropertyChangedTest();
        }

        [Fact]
        //[Retry(100)]
        public override async Task MosaicDrawModelPropertyChangedTest() {
            Setup();
            await base.MosaicDrawModelPropertyChangedTest();
        }

        [Fact]
        public override void MosaicDrawModelAsIsTest() {
            Setup();
            base.MosaicDrawModelAsIsTest();
        }

        [Fact]
        public override void AutoFitTrueCheckAffectsToPaddingTest() {
            Setup();
            base.AutoFitTrueCheckAffectsToPaddingTest();
        }

        [Fact]
        public override void AutoFitTrueCheckAffectsTest() {
            Setup();
            base.AutoFitTrueCheckAffectsTest();
        }

        [Fact]
        public override void AutoFitFalseCheckAffectsTest() {
            Setup();
            base.AutoFitFalseCheckAffectsTest();
        }

        [Fact]
        public override async Task MosaicNoChangedTest() {
            Setup();
            await base.MosaicNoChangedTest();
        }

    }

}
