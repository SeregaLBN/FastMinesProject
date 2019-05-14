using System.Threading.Tasks;
using System.Collections.Generic;
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

    public abstract class MosaicViewTest {

        internal const int TEST_SIZE_W = MosaicModelTest.TEST_SIZE_W;
        internal const int TEST_SIZE_H = MosaicModelTest.TEST_SIZE_H;

        protected abstract void AssertEqual(int    expected, int    actual);
        protected abstract void AssertEqual(double expected, double actual, double delta);
        protected abstract void AssertEqual(object expected, object actual);
        protected abstract void AssertNotNull(object anObject);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertNotEqual(object expected, object actual);

        public virtual void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            LoggerSimple.Put("======================================================");
        }

        public virtual void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(MosaicViewTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual async Task PropertyChangedTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(PropertyChangedTest));

            await new PropertyChangeExecutor<MosaicTestView>(() => new MosaicTestView()).Run(100, 1000,
                view => {
                    view.Model.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H);
                }, (view, modifiedProperties) => {
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(view.Model)));
                    AssertEqual(1, modifiedProperties[            nameof(view.Model)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(view.Size)));
                    AssertEqual(1, modifiedProperties[            nameof(view.Size)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(view.Image)));
                    AssertEqual(1, modifiedProperties[            nameof(view.Image)]);
                    AssertEqual(3, modifiedProperties.Count);
                });
        }

        public virtual async Task ReadinessAtTheStartTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(ReadinessAtTheStartTest));

            await new PropertyChangeExecutor<MosaicTestView>(() => new MosaicTestView()).Run(10, 1000,
                view => {
                    AssertEqual(0, view.DrawCount);
                    AssertNotNull(view.Image);
                    AssertEqual(1, view.DrawCount);
                }, (view, modifiedProperties) => { });
        }

        public virtual async Task MultipleChangeModelOneDrawViewTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(MultipleChangeModelOneDrawViewTest));

            DummyImage img = null;
            MosaicTestView v = null;
            await new PropertyChangeExecutor<MosaicTestView>(() => v = new MosaicTestView(), false).Run(100, 1000,
                view => {
                    AssertEqual(0, view.DrawCount);
                    MosaicModelTest.ChangeModel(view.Model);
                }, (view, modifiedProperties) => {
                    img = view.Image;
                    AssertNotNull(img);
                    AssertEqual(1, view.DrawCount);
                });

            // test no change
            await new PropertyChangeExecutor<MosaicTestView>(() => v, false).Run(100, 1000,
                view => {
                    view.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H);
                }, (view, modifiedProperties) => {
                    AssertEqual(true, ReferenceEquals(img, view.Image));
                    AssertEqual(1, view.DrawCount);
                });

            // test change
            await new PropertyChangeExecutor<MosaicTestView>(() => v).Run(100, 1000,
                view => {
                    v.Size = new SizeDouble(TEST_SIZE_W + 1, TEST_SIZE_H);
                }, (view, modifiedProperties) => {
                    AssertNotEqual(img, view.Image);
                    AssertNotNull(view.Image);
                    AssertEqual(2, view.DrawCount);
                });
        }

        public virtual async Task OneNotificationOfImageChangedTest() {
            LoggerSimple.Put("> " + nameof(MosaicViewTest) + "::" + nameof(OneNotificationOfImageChangedTest));

            await new PropertyChangeExecutor<MosaicTestView>(() => new MosaicTestView()).Run(300, 1000,
                view => {
                    MosaicModelTest.ChangeModel(view.Model);
                }, (view, modifiedProperties) => {
                    AssertTrue(     modifiedProperties.ContainsKey(nameof(view.Image)));
                    AssertEqual(1, modifiedProperties[            nameof(view.Image)]);
                    AssertEqual(0, view.DrawCount);
                    var img = view.Image; // call the implicit draw method
                    AssertEqual(1, view.DrawCount);
                });
        }

    }

}
