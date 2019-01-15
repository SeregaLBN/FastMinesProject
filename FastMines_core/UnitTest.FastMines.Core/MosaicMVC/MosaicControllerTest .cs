using System;
using System.Threading;
using System.Threading.Tasks;
using System.ComponentModel;
using NUnit.Framework;
using fmg.common.geom;
using fmg.common.ui;
using DummyImage = System.Object;

namespace fmg.core.mosaic {

    class TestMosaicController : MosaicController<DummyImage, DummyImage, MosaicTestView, MosaicDrawModel<DummyImage>> {
        internal TestMosaicController() : base(new MosaicTestView(false)) { }
        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }
    }

    public class MosaicControllerTest {

        [SetUp]
        public void Setup() {
            //Factory.DEFERR_INVOKER = doRun => Task.Run(doRun);
            Factory.DEFERR_INVOKER = doRun => Task.Delay(50).ContinueWith(t => doRun());
        }

        [Test]
        public async Task PropertyChangedAsyncTest() {
            using (var signal = new Signal()) {
                using (var ctrlr = new TestMosaicController()) {
                    void onCtrlrPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                        if (ev.PropertyName == nameof(ctrlr.Size))
                            signal.Set();
                    }
                    ctrlr.PropertyChanged += onCtrlrPropertyChanged;

                    ctrlr.Model.Size = new SizeDouble(13, 15);

                    Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(1)));

                    ctrlr.PropertyChanged -= onCtrlrPropertyChanged;
                }
            }
        }

        /*
        [Test]
        public async Task ReadinessAtTheStartTest() {
            using (var signal = new Signal()) {
                using (var ctrlr = new TestMosaicController()) {
                    void onCtrlrPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                        if (ev.PropertyName == nameof(ctrlr.Size))
                            signal.Set();
                    }
                    ctrlr.PropertyChanged += onCtrlrPropertyChanged;

                    ctrlr.Model.Size = new SizeDouble(13, 15);

                    Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(1)));

                    ctrlr.PropertyChanged -= onCtrlrPropertyChanged;
                }
            }
        }
        */

    }

}
