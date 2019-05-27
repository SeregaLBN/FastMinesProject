using System.Threading.Tasks;
using Xunit;
using Fmg.Ava.Utils;

namespace Fmg.Core.Mosaic {

    public class MosaicViewAvaTest : MosaicViewTest {

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
        protected override void AssertNotEqual(object expected, object actual) {
            Assert.NotEqual(expected, actual);
        }

        private bool _tuned;

        public override void Setup() {
            if (_tuned)
                return;
            base.Setup();
            _tuned = true;
            ProjSettings.Init();
        }

        [Fact]
        public override async Task PropertyChangedTest() {
            Setup();
            await base.PropertyChangedTest();
        }

        [Fact]
        public override async Task ReadinessAtTheStartTest() {
            Setup();
            await base.ReadinessAtTheStartTest();
        }

        [Fact]
        public override async Task MultipleChangeModelOneDrawViewTest() {
            Setup();
            await base.MultipleChangeModelOneDrawViewTest();
        }

        [Fact]
        public override async Task OneNotificationOfImageChangedTest() {
            Setup();
            await base.OneNotificationOfImageChangedTest();
        }

    }

}
