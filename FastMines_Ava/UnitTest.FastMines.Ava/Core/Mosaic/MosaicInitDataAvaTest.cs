using System;
using System.Threading.Tasks;
using Xunit;
using fmg.ava.utils;

namespace fmg.core.mosaic {

    public class MosaicInitDataAvaTest : MosaicInitDataTest {

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
        public override void CheckTheImpossibilitySetCustomSkillLevelTest() {
            Setup();
            base.CheckTheImpossibilitySetCustomSkillLevelTest();
        }

        [Fact]
        public override async Task CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
            Setup();
            await base.CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest();
        }

        [Fact]
        public override async Task CheckNoRepeatNotificationsTest() {
            Setup();
            await base.CheckNoRepeatNotificationsTest();
        }

    }

}
