using System.Threading.Tasks;
using System.Collections.Generic;
using NUnit.Framework;
using fmg.common;
using fmg.common.geom;
using fmg.common.notifier;
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
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(Setup));

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
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(PropertyChangedTest));

            using (var view = new MosaicTestView()) {
                await new PropertyChangeExecutor<MosaicTestView>(view).Run(100, 1000,
                    () => {
                        view.Model.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H);
                    }, modifiedProperties => {
                        LoggerSimple.Put("  checking...");
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(view.Model)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(view.Model)]);
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(view.Size)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(view.Size)]);
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(view.Image)));
                        Assert.AreEqual(3, modifiedProperties.Count);
                    });
            }
        }

        [Test]
        public void ReadinessAtTheStartTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(ReadinessAtTheStartTest));

            using (var view = new MosaicTestView()) {
                Assert.AreEqual(0, view.DrawCount);
                Assert.NotNull(view.Image);
                Assert.AreEqual(1, view.DrawCount);
            }
        }

        [Test]
        public async Task MultipleChangeModelOneDrawViewTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(MultipleChangeModelOneDrawViewTest));

            using (var view = new MosaicTestView()) {
                Assert.AreEqual(0, view.DrawCount);

                DummyImage img = null;

                var m = view.Model;
                await new PropertyChangeExecutor<MosaicTestView>(view).Run(100, 1000,
                    () => {
                        MosaicModelTest.ChangeModel(m);
                    }, modifiedProperties => {
                        LoggerSimple.Put("  checking...");
                        img = view.Image;
                        Assert.NotNull(img);
                        Assert.AreEqual(1, view.DrawCount);
                    });

                // test no change
                await new PropertyChangeExecutor<MosaicTestView>(view).Run(100, 1000,
                    () => {
                        m.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H);
                    }, modifiedProperties => {
                        LoggerSimple.Put("  checking...");
                        Assert.AreEqual(true, ReferenceEquals(img, view.Image));
                        Assert.AreEqual(1, view.DrawCount);
                    });

                // test change
                await new PropertyChangeExecutor<MosaicTestView>(view).Run(100, 1000,
                    () => {
                        m.Size = new SizeDouble(TEST_SIZE_W + 1, TEST_SIZE_H);
                    }, modifiedProperties => {
                        LoggerSimple.Put("  checking...");
                        Assert.AreNotEqual(img, view.Image);
                        Assert.NotNull(view.Image);
                        Assert.AreEqual(2, view.DrawCount);
                    });
            }
        }

        [Test]
        public async Task OneNotificationOfImageChangedTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(OneNotificationOfImageChangedTest));

            using (var view = new MosaicTestView()) {
                await new PropertyChangeExecutor<MosaicTestView>(view).Run(100, 1000,
                    () => {
                        MosaicModelTest.ChangeModel(view.Model);
                    }, modifiedProperties => {
                        LoggerSimple.Put("  checking...");
                        Assert.IsTrue(     modifiedProperties.ContainsKey(nameof(view.Image)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(view.Image)]);
                        Assert.AreEqual(0, view.DrawCount);
                        var img = view.Image; // call the implicit draw method
                        Assert.AreEqual(1, view.DrawCount);
                    });
            }
        }

    }

}
