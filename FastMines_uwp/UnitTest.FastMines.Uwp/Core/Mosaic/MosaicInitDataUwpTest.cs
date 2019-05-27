using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Fmg.Uwp.Utils;

namespace Fmg.Core.Mosaic {

    [TestClass]
    public class MosaicInitDataUwpTest : MosaicInitDataTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        protected override void AssertFalse(bool condition) {
            Assert.IsFalse(condition);
        }
        protected override void AssertFail() {
            Assert.Fail();
        }

        [TestInitialize]
        public override void Setup() {
            base.Setup();
            ProjSettings.Init();
        }

        [TestMethod]
        public override async Task CheckTheImpossibilitySetCustomSkillLevelTest() {
            await base.CheckTheImpossibilitySetCustomSkillLevelTest();
        }

        [TestMethod]
        public override async Task CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
            await base.CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest();
        }

        [TestMethod]
        public override async Task CheckNoRepeatNotificationsTest() {
            await base.CheckNoRepeatNotificationsTest();
        }

        [TestMethod]
        public override async Task CheckChangedMosaicGroupTest() {
            await base.CheckChangedMosaicGroupTest();
        }

        [TestMethod]
        public override async Task CheckNoChangedMosaicGroupTest() {
            await base.CheckNoChangedMosaicGroupTest();
        }

        [TestMethod]
        public override async Task CheckRestoreIndexInGroupTest() {
            await base.CheckRestoreIndexInGroupTest();
        }

    }

}
