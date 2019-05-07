using System;
using System.Threading.Tasks;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.common.notifier;
using fmg.core.types;
using DummyImage = System.Object;
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<object>;

namespace fmg.core.mosaic {

    class MosaicTestController : MosaicController<DummyImage, DummyImage, MosaicTestView, MosaicTestModel> {
        internal MosaicTestController() : base(new MosaicTestView()) { }
        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }
    }

    public abstract class MosaicControllerTest {

        /// <summary> double precision </summary>
        private const double P = MosaicModelTest.P;

        protected abstract void AssertEqual(int    expected, int    actual);
        protected abstract void AssertEqual(double expected, double actual, double delta);
        protected abstract void AssertEqual(object expected, object actual);
        protected abstract void AssertNotNull(object anObject);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertFalse(bool condition);

        public virtual void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicControllerTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            LoggerSimple.Put("======================================================");
        }

        public virtual void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(MosaicControllerTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual async Task PropertyChangedTest() {
            LoggerSimple.Put("> " + nameof(MosaicControllerTest) + "::" + nameof(PropertyChangedTest));

            await new PropertyChangeExecutor<MosaicTestController>(() => new MosaicTestController()).Run(100, 1000,
                ctrlr => {
                    MosaicModelTest.ChangeModel(ctrlr.Model);
                }, (ctrlr, modifiedProperties) => {
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(ctrlr.Image)));
                    AssertEqual(1, modifiedProperties[            nameof(ctrlr.Image)]);
                });
        }

        public virtual void ReadinessAtTheStartTest() {
            LoggerSimple.Put("> " + nameof(MosaicControllerTest) + "::" + nameof(ReadinessAtTheStartTest));

            const int defArea = 500;
            using (var ctrlr = new MosaicTestController()) {
                AssertEqual(defArea, ctrlr.Model.Area, P);
                AssertEqual(null, ctrlr.CellDown);
                AssertEqual(0, ctrlr.CountClick);
                AssertEqual(0, ctrlr.CountFlag);
                AssertEqual(10, ctrlr.CountMinesLeft);
                AssertEqual(0, ctrlr.CountOpen);
                AssertEqual(0, ctrlr.CountUnknown);
                AssertEqual(EGameStatus.eGSReady, ctrlr.GameStatus);
                AssertNotNull(ctrlr.Image);
                AssertNotNull(ctrlr.Matrix);
                AssertTrue(ctrlr.Matrix.Any());
                AssertEqual(EMosaic.eMosaicSquare1, ctrlr.MosaicType);
                AssertEqual(EPlayInfo.ePlayerUnknown, ctrlr.PlayInfo);
                AssertNotNull(ctrlr.RepositoryMines);
                AssertFalse(ctrlr.RepositoryMines.Any());
                AssertEqual(Math.Sqrt(defArea) * 10, ctrlr.Size.Width, P);
                AssertEqual(Math.Sqrt(defArea) * 10, ctrlr.Size.Height, P);
                AssertEqual(new Matrisize(10, 10), ctrlr.SizeField);
            }
        }

    }

}
