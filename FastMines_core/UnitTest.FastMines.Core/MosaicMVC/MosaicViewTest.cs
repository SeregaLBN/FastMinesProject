using System;
using System.Threading;
using System.Threading.Tasks;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Collections.Generic;
using NUnit.Framework;
using fmg.common;
using fmg.common.ui;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using DummyImage = System.Object;

namespace fmg.core.mosaic {

    using MosaicModel = MosaicDrawModel<DummyImage>;

    class TestMosaicView : MosaicView<DummyImage, DummyImage, MosaicModel> {
        internal TestMosaicView(bool deferredNotifications) : base(new MosaicModel(), deferredNotifications) { }
        protected override object CreateImage() { return new DummyImage(); }
        internal int DrawCount { get; private set; }
        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            System.Diagnostics.Debug.WriteLine(nameof(TestMosaicView) + "::DrawModified");
            ++DrawCount;
        }
        protected override void Disposing() {
            base.Disposing();
            Model.Dispose();
        }
    }

    class Signal : IDisposable {
        private readonly SemaphoreSlim signal = new SemaphoreSlim(0, 1);
        /// <summary> set signal </summary>
        public void Set() { signal.Release(); }
        ///// <summary> unset signal </summary>
        //public void Reset() { signal.Dispose(); signal = new SemaphoreSlim(0, 1); }
        /// <summary> wait for signal </summary>
        public async Task<bool> Wait(TimeSpan ts) { return await signal.WaitAsync(ts); }
        public void Dispose() { signal.Dispose(); }
    }

    public class MosaicViewTest {

        const int MagicDelayMlsc = 50;

        [SetUp]
        public void Setup() {
            //Factory.DEFERR_INVOKER = doRun => Task.Run(doRun);
            Factory.DEFERR_INVOKER = doRun => Task.Delay(MagicDelayMlsc).ContinueWith(t => doRun());
        }

        [Test]
        public void PropertyChangedTest() {
            using (var view = new TestMosaicView(false)) {
                var modifiedProperties = new List<string>();
                void onViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    modifiedProperties.Add(ev.PropertyName);
                }
                view.PropertyChanged += onViewPropertyChanged;

                view.Model.Size = new SizeDouble(123, 456);

                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Model)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Size)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Image)));

                view.PropertyChanged -= onViewPropertyChanged;
            }
        }

        [Test]
        public void ReadinessAtTheStartTest() {
            using (var view = new TestMosaicView(false)) {
                Assert.AreEqual(0, view.DrawCount);
                Assert.NotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);
            }
        }

        [Test]
        public void MultipleChangeModelOneDrawViewTest() {
            using (var view = new TestMosaicView(false)) {
                Assert.AreEqual(0, view.DrawCount);

                var m = view.Model;
                ChangeModel(m);
                Assert.NotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);

                m.Area = 1234; // test no change
                Assert.NotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);

                m.Area = 1235; // test change
                Assert.NotNull(view.Image);
                Assert.AreEqual(2, view.DrawCount);
            }
        }

        private void ChangeModel(MosaicModel m) {
            m.MosaicType = EMosaic.eMosaicQuadrangle1;
            m.SizeField = new Matrisize(22, 33);
            m.Size = new SizeDouble(345, 678);
            m.Area = 1234;
            m.Padding = new BoundDouble(10);
            m.BackgroundColor = Color.DimGray;
            m.BkFill.Mode = 1;
            m.ColorText.SetColorClose(1, Color.LightSalmon);
            m.ColorText.SetColorOpen(2, Color.MediumSeaGreen);
            m.PenBorder.ColorLight = Color.MediumPurple;
            m.PenBorder.Width = 2;
        }

        [Test]
        public void MultiNotificationOfImageChangedTest() {
            using (var view = new TestMosaicView(false)) {
                var imgChangeCount = 0;
                void onViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    System.Diagnostics.Debug.WriteLine(ev.PropertyName);
                    if (ev.PropertyName == nameof(view.Image))
                        ++imgChangeCount;
                }
                view.PropertyChanged += onViewPropertyChanged;

                var m = view.Model;
                ChangeModel(m);

                Assert.LessOrEqual(1, imgChangeCount);

                view.PropertyChanged -= onViewPropertyChanged;
            }
        }

        [Test]
        public async Task OneNotificationOfImageChangedTest() {
            using (var view = new TestMosaicView(true)) {
                var imgChangeCount = 0;

                using (var signal = new Signal()) {
                    var ob = Observable
                        .FromEventPattern<PropertyChangedEventHandler, PropertyChangedEventArgs>(h => view.PropertyChanged += h, h => view.PropertyChanged -= h);
                    using (ob.Subscribe(
                        ev => {
                            LoggerSimple.Put("onViewPropertyChanged: PropertyName=" + ev.EventArgs.PropertyName);
                            if (ev.EventArgs.PropertyName == nameof(view.Image))
                                ++imgChangeCount;
                        }))
                    {
                        using (ob.Timeout(TimeSpan.FromMilliseconds(MagicDelayMlsc * 1.5))
                            .Subscribe(ev => {
                                LoggerSimple.Put("onNext:");
                            }, ex => {
                                LoggerSimple.Put("onError: " + ex);
                                signal.Set();
                            }))
                        {
                            var m = view.Model;
                            ChangeModel(m);

                            Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(5)));
                        }
                    }
                }

                Assert.AreEqual(1, imgChangeCount);
            }
        }

    }

}
