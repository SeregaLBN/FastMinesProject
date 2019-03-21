using System.Threading.Tasks;
using NUnit.Framework;

namespace fmg.core.mosaic {

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
        protected override void AssertFail() {
            Assert.Fail();
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
        public override void CheckTheImpossibilitySetCustomSkillLevelTest() {
            base.CheckTheImpossibilitySetCustomSkillLevelTest();
        }

        [Test]
        public override async Task CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
            await base.CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest();
        }

        [Test]
        public override async Task CheckNoRepeatNotificationsTest() {
            await base.CheckNoRepeatNotificationsTest();
        }

    }

}
