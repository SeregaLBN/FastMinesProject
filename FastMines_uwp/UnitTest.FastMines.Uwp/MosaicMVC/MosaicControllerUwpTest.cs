using System;
using System.Threading;
using System.Threading.Tasks;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.ApplicationModel.Core;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using DummyImage = System.Object;

namespace fmg.core.mosaic {

    [TestClass]
    public class MosaicControllerUwpTest: MosaicControllerTest {

        public override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        public override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        public override void AssertEqual(double expected, double actual, double delta) {
            Assert.AreEqual(expected, actual, delta);
        }
        public override void AssertNotNull(object anObject) {
            Assert.IsNotNull(anObject);
        }
        public override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        public override void AssertFalse(bool condition) {
            Assert.IsFalse(condition);
        }

        [TestInitialize]
        public override void Setup() {
            base.Setup();
            StaticInitializer.Init();
        }

        [TestMethod]
        public override async Task PropertyChangedTest() {
            await base.PropertyChangedTest();
        }

        [TestMethod]
        public override void ReadinessAtTheStartTest() {
            base.ReadinessAtTheStartTest();
        }

    }

}
