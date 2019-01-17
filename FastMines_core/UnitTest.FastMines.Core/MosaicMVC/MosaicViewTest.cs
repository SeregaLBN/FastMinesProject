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
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<object>;

namespace fmg.core.mosaic {

    class MosaicTestView : MosaicView<DummyImage, DummyImage, MosaicTestModel> {
        internal MosaicTestView(bool deferredNotifications) : base(new MosaicTestModel(), deferredNotifications) { }
        protected override DummyImage CreateImage() { return new DummyImage(); }
        internal int DrawCount { get; private set; }
        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            LoggerSimple.Put(nameof(MosaicTestView) + "::DrawModified");
            ++DrawCount;
        }
        protected override void Disposing() {
            base.Disposing();
            Model.Dispose();
        }
    }

    public class MosaicViewTest {

        const int MagicDelayMlsc = 50;

        [SetUp]
        public void Setup() {
            //Factory.DEFERR_INVOKER = doRun => Task.Run(doRun);
            Factory.DEFERR_INVOKER = doRun => Task.Delay(MagicDelayMlsc).ContinueWith(t => doRun());
        }

        [Test]
        public async Task PropertyChangedTest() {
            using (var view = new MosaicTestView(false)) {
                var modifiedProperties = new List<string>();
                void onViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    LoggerSimple.Put("  MosaicTestView::PropertyChangedTest: onViewPropertyChanged: ev.name=" + ev.PropertyName);
                    modifiedProperties.Add(ev.PropertyName);
                }
                view.PropertyChanged += onViewPropertyChanged;

                view.Model.Size = new SizeDouble(123, 456);

                await Task.Delay(200);

                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Model)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Size)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Image)));

                view.PropertyChanged -= onViewPropertyChanged;
            }
        }

        [Test]
        public void ReadinessAtTheStartTest() {
            using (var view = new MosaicTestView(false)) {
                Assert.AreEqual(0, view.DrawCount);
                Assert.NotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);
            }
        }

        [Test]
        public void MultipleChangeModelOneDrawViewTest() {
            using (var view = new MosaicTestView(false)) {
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

        private void ChangeModel(MosaicTestModel m) {
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
            using (var view = new MosaicTestView(false)) {
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
            using (var view = new MosaicTestView(true)) {
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
