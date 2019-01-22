using System;
using System.Threading.Tasks;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Collections.Generic;
using NUnit.Framework;
using fmg.common;
using fmg.common.ui;
using fmg.common.geom;
using fmg.core.mosaic.cells;
using DummyImage = System.Object;
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<object>;

namespace fmg.core.mosaic {

    class MosaicTestView : MosaicView<DummyImage, DummyImage, MosaicTestModel> {
        internal MosaicTestView() : base(new MosaicTestModel()) { }
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

        internal const int TEST_SIZE_W = MosaicModelTest.TEST_SIZE_W;
        internal const int TEST_SIZE_H = MosaicModelTest.TEST_SIZE_H;

        [SetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> MosaicViewTest::Setup");

            MosaicModelTest.StaticInitializer();

          //Observable.Just("UI factory inited...").Subscribe(LoggerSimple.Put);
        }

        [SetUp]
        public void Before() {
            LoggerSimple.Put("======================================================");
        }

        [OneTimeTearDown]
        public void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(MosaicViewTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        [Test]
        public async Task PropertyChangedTest() {
            var modifiedProperties = new List<string>();
            void onViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                LoggerSimple.Put("  MosaicTestView::PropertyChangedTest: onViewPropertyChanged: ev.name=" + ev.PropertyName);
                modifiedProperties.Add(ev.PropertyName);
            }
            using (var view = new MosaicTestView()) {
                view.PropertyChanged += onViewPropertyChanged;

                Factory.DEFERR_INVOKER(() => view.Model.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));

                await Task.Delay(200);

                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Model)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Size)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(view.Image)));

                view.PropertyChanged -= onViewPropertyChanged;
            }
        }

        [Test]
        public void ReadinessAtTheStartTest() {
            using (var view = new MosaicTestView()) {
                Assert.AreEqual(0, view.DrawCount);
                Assert.NotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);
            }
        }

        [Test]
        public async Task MultipleChangeModelOneDrawViewTest() {
            using (var view = new MosaicTestView()) {
                Assert.AreEqual(0, view.DrawCount);

                var m = view.Model;
                Factory.DEFERR_INVOKER(() => MosaicModelTest.ChangeModel(m));
                await Task.Delay(100);

                var img = view.Image;
                Assert.NotNull(img);
                Assert.AreEqual(1, view.DrawCount);

                // test no change
                Factory.DEFERR_INVOKER(() => m.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H));
                await Task.Delay(100);
                Assert.AreEqual(true, ReferenceEquals(img, view.Image));
                Assert.AreEqual(1, view.DrawCount);

                // test change
                Factory.DEFERR_INVOKER(() => m.Size = new SizeDouble(TEST_SIZE_W + 1, TEST_SIZE_H));
                await Task.Delay(200);
                Assert.AreNotEqual(img, view.Image);
                Assert.NotNull(view.Image);
                Assert.AreEqual(2, view.DrawCount);
            }
        }

        [Test]
        public async Task OneNotificationOfImageChangedTest() {
            using (var view = new MosaicTestView()) {
                var modifiedProperties = new Dictionary<string /* property name */, int /* count */>();

                using (var signal = new Signal()) {
                    var ob = Observable
                        .FromEventPattern<PropertyChangedEventHandler, PropertyChangedEventArgs>(h => view.PropertyChanged += h, h => view.PropertyChanged -= h);
                    using (ob.Subscribe(
                        ev => {
                            var name = ev.EventArgs.PropertyName;
                            LoggerSimple.Put("  OneNotificationOfImageChangedTest: onViewPropertyChanged: ev.name=" + name);
                            modifiedProperties[name] = 1 + (modifiedProperties.ContainsKey(name) ? modifiedProperties[name] : 0);
                        }))
                    {
                        using (ob.Timeout(TimeSpan.FromMilliseconds(50))
                            .Subscribe(ev => {
                                LoggerSimple.Put("onNext:");
                            }, ex => {
                                LoggerSimple.Put("onError: " + ex);
                                signal.Set();
                            }))
                        {
                            Factory.DEFERR_INVOKER(() => MosaicModelTest.ChangeModel(view.Model));

                            Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(1)));
                        }
                    }
                }

                LoggerSimple.Put("  OneNotificationOfImageChangedTest: checking...");
                Assert.AreEqual(1, modifiedProperties[nameof(view.Image)]);
                Assert.AreEqual(0, view.DrawCount);
                var img = view.Image; // call the implicit draw method
                Assert.AreEqual(1, view.DrawCount);
            }
        }

    }

}
