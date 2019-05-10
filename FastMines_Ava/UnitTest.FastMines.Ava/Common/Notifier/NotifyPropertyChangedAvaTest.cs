using System;
using System.Threading.Tasks;
using Xunit;
using fmg.ava.utils;

namespace fmg.common.notifier {

    public class NotifyPropertyChangedAvaTest : NotifyPropertyChangedTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.Equal(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.Equal(expected, actual);
        }
        protected override void AssertTrue(bool condition) {
            Assert.True(condition);
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
            StaticInitializer.Init();
        }

        [Fact]
        public override void NotifyPropertyChangedSyncTest() {
            Setup();
            base.NotifyPropertyChangedSyncTest();
        }

        [Fact]
        public override async Task NotifyPropertyChangedAsyncTest() {
            Setup();
            await base.NotifyPropertyChangedAsyncTest();
        }

        [Fact]
        public override async Task CheckForNoEventTest() {
            Setup();
            await base.CheckForNoEventTest();
        }

        [Fact]
        public override void ForgotToUnsubscribeTest() {
            Setup();
            base.ForgotToUnsubscribeTest();
        }

    }

}
