using System;
using System.Threading.Tasks;
using Xunit;
using Fmg.Ava.Utils;

namespace Fmg.Core.Mosaic {

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
        public override async Task CheckTheImpossibilitySetCustomSkillLevelTest() {
            Setup();
            await base.CheckTheImpossibilitySetCustomSkillLevelTest();
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

        [Fact]
        public override async Task CheckChangedMosaicGroupTest() {
            Setup();
            await base.CheckChangedMosaicGroupTest();
        }

        [Fact]
        public override async Task CheckNoChangedMosaicGroupTest() {
            Setup();
            await base.CheckNoChangedMosaicGroupTest();
        }

        [Fact]
        public override async Task CheckRestoreIndexInGroupTest() {
            Setup();
            await base.CheckRestoreIndexInGroupTest();
        }

    }

}
