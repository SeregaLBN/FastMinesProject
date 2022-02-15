using System.Linq;
using System.Threading.Tasks;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using Fmg.Common.Notifier;
using MosaicTestModel = Fmg.Core.Mosaic.MosaicDrawModel<object>;

namespace Fmg.Core.Mosaic {

    public abstract class MosaicModelTest {

        /// <summary> double precision </summary>
        internal const double P = 0.001;

        internal const int TEST_SIZE_W = 456;
        internal const int TEST_SIZE_H = 789;

        protected abstract void AssertEqual(int    expected, int    actual);
        protected abstract void AssertEqual(double expected, double actual, double delta);
        protected abstract void AssertEqual(object expected, object actual);
        protected abstract void AssertNotNull(object anObject);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertFalse(bool condition);
        protected abstract void AssertLessOrEqual(int valToBeLess, int valToBeGreater);

        public virtual void Setup() {
            Logger.Info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            Logger.Info("======================================================");
        }

        public virtual void After() {
            Logger.Info("======================================================");
            Logger.Info("< " + nameof(MosaicModelTest) + " closed");
            Logger.Info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual async Task MosaicGameModelPropertyChangedTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(MosaicGameModelPropertyChangedTest));

            MosaicGameModel m = null;
            await new PropertyChangeExecutor<MosaicGameModel>(() => m = new MosaicGameModel(), false).Run(300, 5000,
                model => {
                    AssertTrue(model.Matrix.Any());
                    AssertTrue(ReferenceEquals(model.Shape, model.Matrix[0].Shape));

                    model.SizeField = new Matrisize(15, 10);
                }, (model, modifiedProperties) => {
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.SizeField)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.SizeField)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.Matrix)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.Matrix)]);
                    AssertEqual(2, modifiedProperties.Count);
                });

            await new PropertyChangeExecutor<MosaicGameModel>(() => m).Run(300, 5000,
                model => {
                    model.Area = 12345;
                }, (model, modifiedProperties) => {
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.Area)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.Area)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.Shape)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.Shape)]);
                    AssertEqual(2, modifiedProperties.Count);
                });
        }

        public virtual async Task MosaicDrawModelPropertyChangedTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(MosaicDrawModelPropertyChangedTest));

            await new PropertyChangeExecutor<MosaicTestModel>(() => new MosaicTestModel()).Run(200, 1000,
                model => {
                    ChangeModel(model);
                }, (model, modifiedProperties) => {
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(IImageModel.Size)));
                    AssertEqual(1, modifiedProperties[            nameof(IImageModel.Size)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.Area)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.Area)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.Shape)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.Shape)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.MosaicType)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.MosaicType)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicGameModel.Matrix)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicGameModel.Matrix)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicTestModel.BackgroundColor)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicTestModel.BackgroundColor)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicTestModel.CellFill)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicTestModel.CellFill)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicTestModel.CellColor)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicTestModel.CellColor)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicTestModel.ColorText)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicTestModel.ColorText)]);
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(MosaicTestModel.PenBorder)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicTestModel.PenBorder)]);
                });
        }

        public virtual async Task MosaicDrawModelAsIsTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(MosaicDrawModelAsIsTest));

            await new PropertyChangeExecutor<MosaicTestModel>(() => new MosaicTestModel()).Run(10, 1000,
                model => {
                    AssertEqual(EMosaic.eMosaicSquare1, model.MosaicType);
                    AssertEqual(new Matrisize(10, 10), model.SizeField);
                    AssertEqual(model.Shape.GetSize(model.SizeField), model.Size);
                }, (model, modifiedProperties) => { });
        }

        public virtual async Task AutoFitTrueCheckAffectsToPaddingTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(AutoFitTrueCheckAffectsToPaddingTest));

            await new PropertyChangeExecutor<MosaicTestModel>(() => new MosaicTestModel()).Run(10, 1000,
                model => {
                    // set property
                    model.AutoFit = true;
                    model.Size = new SizeDouble(1000, 1000);
                    model.Padding = new BoundDouble(100);

                    // change property
                    model.Size = new SizeDouble(500, 700);

                    // check dependency
                    AssertEqual(50.0, model.Padding.Left);
                    AssertEqual(50.0, model.Padding.Right);
                    AssertEqual(70.0, model.Padding.Top);
                    AssertEqual(70.0, model.Padding.Bottom);
                }, (model, modifiedProperties) => { });
        }

        public virtual async Task AutoFitTrueCheckAffectsTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(AutoFitTrueCheckAffectsTest));

            MosaicTestModel createTestModel() {
                var model = new MosaicTestModel {
                    // set property
                    AutoFit = true,
                    Size = new SizeDouble(1000, 1000)
                };

                // default check
                var size = model.Size;
                AssertEqual(1000, size.Width , P);
                AssertEqual(1000, size.Height, P);

                var mosaicSize = model.MosaicSize;
                AssertEqual(1000, mosaicSize.Width , P);
                AssertEqual(1000, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                AssertEqual(0, mosaicOffset.Width , P);
                AssertEqual(0, mosaicOffset.Height, P);

                var padding = model.Padding;
                AssertEqual(0, padding.Left  , P);
                AssertEqual(0, padding.Top   , P);
                AssertEqual(0, padding.Right , P);
                AssertEqual(0, padding.Bottom, P);

                return model;
            }

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(500, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(100, mosaicOffset.Width , P);
                    AssertEqual(  0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(0, padding.Left  , P);
                    AssertEqual(0, padding.Top   , P);
                    AssertEqual(0, padding.Right , P);
                    AssertEqual(0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Padding = new BoundDouble(150, 75, 50, 25);
                    model.Size = new SizeDouble(700, 500);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(450, mosaicSize.Width , P);
                    AssertEqual(450, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(160 , mosaicOffset.Width , P);
                    AssertEqual(37.5, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(105 , padding.Left  , P);
                    AssertEqual(37.5, padding.Top   , P);
                    AssertEqual(35  , padding.Right , P);
                    AssertEqual(12.5, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(525, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(87.5, mosaicOffset.Width , P);
                    AssertEqual(   0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(0, padding.Left  , P);
                    AssertEqual(0, padding.Top   , P);
                    AssertEqual(0, padding.Right , P);
                    AssertEqual(0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(350, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(175, mosaicOffset.Width , P);
                    AssertEqual(  0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(0, padding.Left  , P);
                    AssertEqual(0, padding.Top   , P);
                    AssertEqual(0, padding.Right , P);
                    AssertEqual(0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.Padding = new BoundDouble(150, 75, 50, 25);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(280, mosaicSize.Width , P);
                    AssertEqual(400, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(260, mosaicOffset.Width , P);
                    AssertEqual( 75, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(150, padding.Left  , P);
                    AssertEqual( 75, padding.Top   , P);
                    AssertEqual( 50, padding.Right , P);
                    AssertEqual( 25, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.Padding = new BoundDouble(-150, -75, -50, -25);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(420, mosaicSize.Width , P);
                    AssertEqual(600, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual( 90, mosaicOffset.Width , P);
                    AssertEqual(-75, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(-150, padding.Left  , P);
                    AssertEqual(- 75, padding.Top   , P);
                    AssertEqual(- 50, padding.Right , P);
                    AssertEqual(- 25, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.Padding = new BoundDouble(-150, -75, -50, -25);
                    model.Area = 100;

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(105, size.Width , P);
                    AssertEqual(150, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(105, mosaicSize.Width , P);
                    AssertEqual(150, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(0, mosaicOffset.Width , P);
                    AssertEqual(0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(0, padding.Left  , P);
                    AssertEqual(0, padding.Top   , P);
                    AssertEqual(0, padding.Right , P);
                    AssertEqual(0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.Padding = new BoundDouble(150, 75, 50, 25);
                    model.Area = 100;

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(305, size.Width , P);
                    AssertEqual(250, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(105, mosaicSize.Width , P);
                    AssertEqual(150, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(150, mosaicOffset.Width , P);
                    AssertEqual( 75, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(150, padding.Left  , P);
                    AssertEqual( 75, padding.Top   , P);
                    AssertEqual( 50, padding.Right , P);
                    AssertEqual( 25, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.MosaicOffset = new SizeDouble(200, 300);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(1000, size.Width , P);
                    AssertEqual(1000, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(1000, mosaicSize.Width , P);
                    AssertEqual(1000, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(200, mosaicOffset.Width , P);
                    AssertEqual(300, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual( 200, padding.Left  , P);
                    AssertEqual( 300, padding.Top   , P);
                    AssertEqual(-200, padding.Right , P);
                    AssertEqual(-300, padding.Bottom, P);
                }, (model, modifiedProperties) => { });
        }

        public virtual async Task AutoFitFalseCheckAffectsTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(AutoFitFalseCheckAffectsTest));

            MosaicTestModel createTestModel() {
                var model = new MosaicTestModel {
                    // set property
                    AutoFit = false,
                    Size = new SizeDouble(1000, 1000)
                };

                // default check
                var size = model.Size;
                AssertEqual(1000, size.Width , P);
                AssertEqual(1000, size.Height, P);

                var mosaicSize = model.MosaicSize;
                AssertEqual(1000, mosaicSize.Width , P);
                AssertEqual(1000, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                AssertEqual(0, mosaicOffset.Width , P);
                AssertEqual(0, mosaicOffset.Height, P);

                var padding = model.Padding;
                AssertEqual(0, padding.Left  , P);
                AssertEqual(0, padding.Top   , P);
                AssertEqual(0, padding.Right , P);
                AssertEqual(0, padding.Bottom, P);

                return model;
            }

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.MosaicOffset = new SizeDouble(200, 300);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(1000, size.Width , P);
                    AssertEqual(1000, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(1000, mosaicSize.Width , P);
                    AssertEqual(1000, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(200, mosaicOffset.Width , P);
                    AssertEqual(300, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual( 200, padding.Left  , P);
                    AssertEqual( 300, padding.Top   , P);
                    AssertEqual(-200, padding.Right , P);
                    AssertEqual(-300, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.MosaicOffset = new SizeDouble(10, 15);
                    model.Size = new SizeDouble(700, 500);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(500, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(100, mosaicOffset.Width , P);
                    AssertEqual(  0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(100, padding.Left  , P);
                    AssertEqual(  0, padding.Top   , P);
                    AssertEqual(100, padding.Right , P);
                    AssertEqual(  0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(525, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(87.5, mosaicOffset.Width , P);
                    AssertEqual(   0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(87.5, padding.Left  , P);
                    AssertEqual(   0, padding.Top   , P);
                    AssertEqual(87.5, padding.Right , P);
                    AssertEqual(   0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(350, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(175, mosaicOffset.Width , P);
                    AssertEqual(  0, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(175, padding.Left  , P);
                    AssertEqual(  0, padding.Top   , P);
                    AssertEqual(175, padding.Right , P);
                    AssertEqual(  0, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.MosaicOffset = new SizeDouble(-15, -40);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(350, mosaicSize.Width , P);
                    AssertEqual(500, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(-15, mosaicOffset.Width , P);
                    AssertEqual(-40, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(-15, padding.Left  , P);
                    AssertEqual(-40, padding.Top   , P);
                    AssertEqual(365, padding.Right , P);
                    AssertEqual( 40, padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.MosaicOffset = new SizeDouble(-15, -40);
                    model.Area = 225;

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(157.5, mosaicSize.Width , P);
                    AssertEqual(225  , mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(-15, mosaicOffset.Width , P);
                    AssertEqual(-40, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(-15  , padding.Left  , P);
                    AssertEqual(-40  , padding.Top   , P);
                    AssertEqual(557.5, padding.Right , P);
                    AssertEqual(315  , padding.Bottom, P);
                }, (model, modifiedProperties) => { });

            await new PropertyChangeExecutor<MosaicTestModel>(createTestModel).Run(10, 1000,
                model => {
                    // change property
                    model.Size = new SizeDouble(700, 500);
                    model.MosaicType = EMosaic.eMosaicSquare2;
                    model.SizeField = new Matrisize(10, 15);
                    model.MosaicOffset = new SizeDouble(-15, -40);
                    model.Area = 225;
                    model.Padding = new BoundDouble(150, 75, 50, 25);

                    // check dependency (evenly expanded)
                    var size = model.Size;
                    AssertEqual(700, size.Width , P);
                    AssertEqual(500, size.Height, P);

                    var mosaicSize = model.MosaicSize;
                    AssertEqual(280, mosaicSize.Width , P);
                    AssertEqual(400, mosaicSize.Height, P);

                    var mosaicOffset = model.MosaicOffset;
                    AssertEqual(260, mosaicOffset.Width , P);
                    AssertEqual( 75, mosaicOffset.Height, P);

                    var padding = model.Padding;
                    AssertEqual(150, padding.Left  , P);
                    AssertEqual( 75, padding.Top   , P);
                    AssertEqual( 50, padding.Right , P);
                    AssertEqual( 25, padding.Bottom, P);
                }, (model, modifiedProperties) => { });
        }

        internal static void ChangeModel(MosaicTestModel m) {
            m.MosaicType = EMosaic.eMosaicQuadrangle1;
            m.SizeField = new Matrisize(22, 33);
            m.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H);
          //m.Area = 1234;
            m.Padding = new BoundDouble(10);
            m.BackgroundColor = Color.DimGray;
            m.CellColor = Color.Gray;
            m.CellFill.Mode = 1;
            m.ColorText.SetColorClose(1, Color.LightSalmon);
            m.ColorText.SetColorOpen(2, Color.MediumSeaGreen);
            m.PenBorder.ColorLight = Color.MediumPurple;
            m.PenBorder.Width = 2;
        }

        public virtual async Task MosaicNoChangedTest() {
            Logger.Info("> " + nameof(MosaicModelTest) + "::" + nameof(MosaicNoChangedTest));

            MosaicTestModel m = null;
            // step 1: init
            await new PropertyChangeExecutor<MosaicTestModel>(() => m = new MosaicTestModel(), false).Run(300, 1000,
                model => {
                    var size = model.Size; // implicit call setter Size
                    AssertNotNull(size);
                }, (model, modifiedProperties) => {
                    AssertTrue (   modifiedProperties.ContainsKey(nameof(model.Size)));
                    AssertEqual(1, modifiedProperties[            nameof(model.Size)]);
                    AssertLessOrEqual(1, modifiedProperties.Count);
                });

            // step 2: check no changes
            await new PropertyChangeExecutor<MosaicTestModel>(() => m).Run(200, 1000,
                model => {
                    model.Size = model.Size;
                    model.Area = model.Area;
                    model.SizeField = model.SizeField;
                    model.Padding = model.Padding;
                }, (model, modifiedProperties) => {
                    AssertFalse(modifiedProperties.Any());
                });
        }

        public virtual async Task NoChangeOffsetTest() {
            async Task func(bool autofit) =>
                await new PropertyChangeExecutor<MosaicTestModel>(() => new MosaicTestModel()).Run(10, 1000,
                    model => {
                        // change property
                        model.AutoFit = autofit;
                        var size = model.Size;
                        size.Width *= 2;
                        model.Size = size;

                        // getsome properties
                        var pad = model.Padding;
                        var offset = model.MosaicOffset;

                        // try facked change
                        model.MosaicOffset = offset;

                        // verify
                        var pad2 = model.Padding;
                        var offset2 = model.MosaicOffset;
                        AssertEqual(pad, pad2);
                        AssertEqual(offset, offset2);
                    }, (model, modifiedProperties) => { });

            await func(true);
            await func(false);
        }

    }

}
