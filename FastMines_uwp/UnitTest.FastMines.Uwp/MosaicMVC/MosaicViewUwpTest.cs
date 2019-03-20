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

    using MosaicModel = MosaicDrawModel<DummyImage>;

    class TestMosaicView : MosaicView<DummyImage, DummyImage, MosaicModel> {
        internal TestMosaicView() : base(new MosaicModel()) { }
        protected override object CreateImage() { return new DummyImage(); }
        internal int DrawCount { get; private set; }
        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            System.Diagnostics.Debug.WriteLine(nameof(TestMosaicView) + "::" + nameof(DrawModified));
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

    [TestClass]
    public class MosaicViewTest {

        [TestInitialize]
        public void Setup() {
            StaticInitializer.Init();
        }

        [TestMethod]
        public void PropertyChangedTest() {
            using (var view = new TestMosaicView()) {
                var modifiedProperties = new List<string>();
                void onViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    LoggerSimple.Put("  MosaicTestView::PropertyChangedTest: onViewPropertyChanged: ev.name=" + ev.PropertyName);
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

        [TestMethod]
        public void ReadinessAtTheStartTest() {
            using (var view = new TestMosaicView()) {
                Assert.AreEqual(0, view.DrawCount);
                Assert.IsNotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);
            }
        }

        [TestMethod]
        public void MultipleChangeModelOneDrawViewTest() {
            using (var view = new TestMosaicView()) {
                Assert.AreEqual(0, view.DrawCount);

                var m = view.Model;
                ChangeModel(m);
                Assert.IsNotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);

                m.Area = 1234; // test no change
                Assert.IsNotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);

                m.Area = 1235; // test change
                Assert.IsNotNull(view.Image);
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

        [TestMethod]
        public void MultiNotificationOfImageChangedTest() {
            using (var view = new TestMosaicView()) {
                var imgChangeCount = 0;
                void onViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    System.Diagnostics.Debug.WriteLine(ev.PropertyName);
                    if (ev.PropertyName == nameof(view.Image))
                        ++imgChangeCount;
                }
                view.PropertyChanged += onViewPropertyChanged;

                var m = view.Model;
                ChangeModel(m);

                Assert.IsTrue(1 <= imgChangeCount, "LessOrEqual");

                view.PropertyChanged -= onViewPropertyChanged;
            }
        }

        [TestMethod]
        public async Task IdiotoTest() {
            var noTimeout = false;
            await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                CoreDispatcherPriority.High,
                async () => {
                    var signal = new Signal();
            //Assert.AreEqual(2, 3);
                    signal.Set();
                    noTimeout = await signal.Wait(TimeSpan.FromSeconds(5));
                    signal.Dispose();
                });
            Assert.IsTrue(noTimeout);
        }

        [TestMethod]
        public async Task OneNotificationOfImageInUiThreadChangedTest() {
            LoggerSimple.Put("> Test thread");
            var executed = false;
            var noTimeout = false;
            var imgChangeCount = 0;
            await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                CoreDispatcherPriority.High,
                async () => {
                    LoggerSimple.Put("UI thread");
                    using (var view = new TestMosaicView()) {

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
                                using (ob.Timeout(TimeSpan.FromMilliseconds(500))
                                    .Subscribe(ev => {
                                        LoggerSimple.Put("onNext:");
                                    }, ex => {
                                        LoggerSimple.Put("onError: " + ex);
                                        signal.Set();
                                    }))
                                {
                                    var m = view.Model;
                                    ChangeModel(m);
                                    executed = true;
                                    LoggerSimple.Put("> signal wait");
                                    noTimeout = await signal.Wait(TimeSpan.FromSeconds(5));
                                    LoggerSimple.Put("< signal wait: noTimeout=" + noTimeout);
                                }
                            }
                        }

                    }

                    //using (var view = new TestMosaicView(true)) {
                    //    var imgChangeCount = 0;

                    //    using (Observable
                    //        .FromEventPattern<PropertyChangedEventHandler, PropertyChangedEventArgs>(
                    //            h => view.PropertyChanged += h,
                    //            h => view.PropertyChanged -= h)
                    //        .Subscribe(
                    //            ev => {
                    //                LoggerSimple.Put("onViewPropertyChanged: PropertyName=" + ev.EventArgs.PropertyName);
                    //                if (ev.EventArgs.PropertyName == nameof(view.Image))
                    //                    ++imgChangeCount;
                    //            }))
                    //    {
                    //        var m = view.Model;
                    //        ChangeModel(m);

                    //        await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                    //            CoreDispatcherPriority.Low,
                    //            () => {
                    //                Assert.AreEqual(1, imgChangeCount);
                    //            });
                    //    }

                    //}
                });
            LoggerSimple.Put("< Test thread");
            Assert.IsTrue(executed);
            Assert.IsTrue(noTimeout);
            Assert.AreEqual(1, imgChangeCount);
            Assert.AreEqual(2, 3);
        }

    }

}
