using System.Threading.Tasks;
using Xunit;
using Fmg.Ava.App;

namespace Fmg.Core.Mosaic {

    public class MosaicHelperAvaTest : MosaicHelperTest {

        protected override void AssertEqual(object expected, object actual) {
            Assert.Equal(expected, actual);
        }
        protected override void AssertTrue(bool condition) {
            Assert.True(condition);
        }
        protected override void AssertLess(double valToBeLess, double valToBeGreater) {
            Assert.True(valToBeLess < valToBeGreater);
        }
        protected override void AssertGreaterOrEqual(double valToBeGreater, double valToBeLess) {
            Assert.True(valToBeGreater >= valToBeLess);
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
        public override void FindSizeByArea_eMosaicSquare1_Test() {
            Setup();
            base.FindSizeByArea_eMosaicSquare1_Test();
        }

        [Fact]
        public override void FindAreaBySize_eMosaicSquare1_Test1() {
            Setup();
            base.FindAreaBySize_eMosaicSquare1_Test1();
        }

        [Fact]
        public override void FindAreaBySize_eMosaicSquare1_Test2() {
            Setup();
            base.FindAreaBySize_eMosaicSquare1_Test2();
        }

        [Fact]
        public override void FindAreaBySize_eMosaicSquare1_Test3() {
            Setup();
            base.FindAreaBySize_eMosaicSquare1_Test3();
        }

        [Fact]
        public override void FindAreaBySize_Random_Test() {
            Setup();
            base.FindAreaBySize_Random_Test();
        }

        [Fact]
        public override async Task FindAreaBySize_eMosaicTrapezoid3_Test() {
            Setup();
            await base.FindAreaBySize_eMosaicTrapezoid3_Test();
        }

        [Fact]
        public override void FindAreaBySize_eMosaicTriangle1_Test() {
            Setup();
            base.FindAreaBySize_eMosaicTriangle1_Test();
        }

    }

}
