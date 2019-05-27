using System.Threading.Tasks;
using NUnit.Framework;
using Fmg.Common.Notifier;

namespace Fmg.Core.Mosaic {

    public class MosaicInitDataNUnitTest : MosaicInitDataTest {

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

        [OneTimeSetUp]
        public override void Setup() {
            base.Setup();
            NotifyPropertyChangedNUnitTest.ProjSettings();
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
        public override async Task CheckTheImpossibilitySetCustomSkillLevelTest() {
            await base.CheckTheImpossibilitySetCustomSkillLevelTest();
        }

        [Test]
        public override async Task CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
            await base.CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest();
        }

        [Test]
        public override async Task CheckNoRepeatNotificationsTest() {
            await base.CheckNoRepeatNotificationsTest();
        }

        [Test]
        public override async Task CheckChangedMosaicGroupTest() {
            await base.CheckChangedMosaicGroupTest();
        }

        [Test]
        public override async Task CheckNoChangedMosaicGroupTest() {
            await base.CheckNoChangedMosaicGroupTest();
        }

        [Test]
        public override async Task CheckRestoreIndexInGroupTest() {
            await base.CheckRestoreIndexInGroupTest();
        }

    }

}
