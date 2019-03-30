using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Xunit;
using fmg.ava.utils;

namespace fmg.core.mosaic {

    internal class DoubleComparer : IEqualityComparer<double> {
        private readonly double _delta;

        public DoubleComparer(double delta) {
            _delta = delta;
        }

        public bool Equals(double x, double y) {
            return Math.Abs(x - y) <= _delta;
        }

        public int GetHashCode(double obj) {
            return obj.GetHashCode();
        }
    }

    public class MosaicControllerAvaTest : MosaicControllerTest {

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

        private bool _tuned;

        public override void Setup() {
            if (_tuned)
                return;
            base.Setup();
            _tuned = true;
            StaticInitializer.Init();
        }

        [Fact]
        public override async Task PropertyChangedTest() {
            Setup();
            await base.PropertyChangedTest();
        }

        [Fact]
        public override void ReadinessAtTheStartTest() {
            Setup();
            base.ReadinessAtTheStartTest();
        }

    }

}
