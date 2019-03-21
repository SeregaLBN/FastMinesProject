using System;
using System.ComponentModel;
using System.Threading.Tasks;
using Windows.UI.Core;
using Windows.ApplicationModel.Core;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using fmg.uwp.utils;

namespace fmg.common.notifier {

    [TestClass]
    public class NotifyPropertyChangedUwpTest : NotifyPropertyChangedTest {

        public override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        public override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }

        [TestInitialize]
        public override void Setup() {
            base.Setup();

            StaticInitializer.Init();
        }

        [TestMethod]
        public override void NotifyPropertyChangedSyncTest() {
            base.NotifyPropertyChangedSyncTest();
        }

        [TestMethod]
        public override async Task NotifyPropertyChangedAsyncTest() {
            await base.NotifyPropertyChangedAsyncTest();
        }

        [TestMethod]
        public override async Task CheckForNoEventTest() {
            await base.CheckForNoEventTest();
        }

    }

}
