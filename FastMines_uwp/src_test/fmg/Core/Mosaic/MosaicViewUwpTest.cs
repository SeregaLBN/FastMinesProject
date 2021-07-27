using System;
using System.Threading.Tasks;
using System.Reactive.Linq;
using Windows.UI.Core;
using Windows.ApplicationModel.Core;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Fmg.Uwp.App;

namespace Fmg.Core.Mosaic {

    [TestClass]
    public class MosaicViewUwpTest : MosaicViewTest {

        protected override void AssertEqual(int expected, int actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(object expected, object actual) {
            Assert.AreEqual(expected, actual);
        }
        protected override void AssertEqual(double expected, double actual, double delta) {
            Assert.AreEqual(expected, actual, delta);
        }
        protected override void AssertNotNull(object anObject) {
            Assert.IsNotNull(anObject);
        }
        protected override void AssertTrue(bool condition) {
            Assert.IsTrue(condition);
        }
        protected override void AssertNotEqual(object expected, object actual) {
            Assert.AreNotEqual(expected, actual);
        }

        [TestInitialize]
        public override void Setup() {
            base.Setup();
            ProjSettings.Init();
        }

        [TestMethod]
        public override async Task PropertyChangedTest() {
            await base.PropertyChangedTest();
        }

        [TestMethod]
        public override async Task ReadinessAtTheStartTest() {
            await base.ReadinessAtTheStartTest();
        }

        [TestMethod]
        public override async Task MultipleChangeModelOneDrawViewTest() {
            await base.MultipleChangeModelOneDrawViewTest();
        }

        [TestMethod]
        public override async Task OneNotificationOfImageChangedTest() {
            await base.OneNotificationOfImageChangedTest();
        }


        [TestMethod]
        public async Task IdiotoTest() {
            var noTimeout = false;
            await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                CoreDispatcherPriority.High,
                async () => {
                    var signal = new Fmg.Common.Notifier.Signal();
            //Assert.AreEqual(2, 3);
                    signal.Set();
                    noTimeout = await signal.Wait(TimeSpan.FromSeconds(5));
                    signal.Dispose();
                });
            Assert.IsTrue(noTimeout);
        }

    }

}
