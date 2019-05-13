using System;
using System.Threading.Tasks;
using Xunit;
using fmg.ava.utils;

namespace fmg.common.notifier {

    public class PropertyChangeExecutorAvaTest : PropertyChangeExecutorTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.Equal(expected, actual);
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
        protected override void AssertFail() {
            //Assert.Fail();
            throw new Exception("Assert.Fail");
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
        public override async Task SimpleUsageTest() {
            Setup();
            await base.SimpleUsageTest();
        }

        [Fact]
        public override async Task ExtendedUsageTest() {
            Setup();
            await base.ExtendedUsageTest();
        }

        [Fact]
        public override async Task CreatorFailTest() {
            Setup();
            await base.CreatorFailTest();
        }

        [Fact]
        public override async Task ModificatorFailTest() {
            Setup();
            await base.ModificatorFailTest();
        }

        [Fact]
        public override async Task ValidatorFailTest() {
            Setup();
            await base.ValidatorFailTest();
        }

    }

}
