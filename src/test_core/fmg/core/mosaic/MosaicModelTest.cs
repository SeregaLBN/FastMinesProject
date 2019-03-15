using System;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel;
using System.Collections.Generic;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using NUnit.Framework;
using fmg.common;
using fmg.common.geom;
using fmg.common.ui;
using fmg.core.types;
using fmg.core.img;
using fmg.common.notifier;
//using DummyImage = System.Object;
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<object>;

namespace fmg.core.mosaic {

    public class MosaicModelTest {

        /// <summary> double precision </summary>
        internal const double P = 0.001;

        internal const int TEST_SIZE_W = 456;
        internal const int TEST_SIZE_H = 789;

        internal static void StaticInitializer() {
            //Factory.DEFERR_INVOKER = doRun => Task.Run(doRun);
            Factory.DEFERR_INVOKER = doRun => Task.Delay(10).ContinueWith(t => doRun());
        }

        [OneTimeSetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put(">" + nameof(MosaicModelTest) + "::" + nameof(Setup));

            StaticInitializer();

            //Observable.Just("UI factory inited...").Subscribe(LoggerSimple.Put);
        }

        [SetUp]
        public void Before() {
            LoggerSimple.Put("======================================================");
        }

        [OneTimeTearDown]
        public void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(MosaicModelTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        [Test]
        public async Task MosaicGameModelPropertyChangedTest() {
            LoggerSimple.Put("> MosaicGameModelPropertyChangedTest");

            using (var model = new MosaicGameModel()) {
                Assert.IsTrue(model.Matrix.Any());
                Assert.IsTrue(ReferenceEquals(model.CellAttr, model.Matrix.First().Attr));

                var modifiedProperties = new List<string>();
                void onModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    LoggerSimple.Put("  MosaicGameModelPropertyChangedTest: onModelPropertyChanged: ev.name=" + ev.PropertyName);
                    modifiedProperties.Add(ev.PropertyName);
                }
                model.PropertyChanged += onModelPropertyChanged;

                modifiedProperties.Clear();
                model.SizeField = new Matrisize(15, 10);
                await Task.Delay(TimeSpan.FromMilliseconds(200));
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.SizeField)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.Matrix)));

                modifiedProperties.Clear();
                model.Area = 12345;
                await Task.Delay(TimeSpan.FromMilliseconds(200));
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.Area)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.CellAttr)));

                model.PropertyChanged -= onModelPropertyChanged;
            }
        }

        [Test]
      //[Retry(100)]
        public async Task MosaicDrawModelPropertyChangedTest() {
            LoggerSimple.Put("> mosaicDrawModelPropertyChangedTest");

            using (var model = new MosaicTestModel()) {
                var subject = new Subject<PropertyChangedEventArgs>();

                var modifiedProperties = new Dictionary<string /* property name */, int /* count */>();
                void onModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    var name = ev.PropertyName;
                    LoggerSimple.Put("  " + nameof(MosaicDrawModelPropertyChangedTest) +
                                     ": " + nameof(onModelPropertyChanged) +
                                     ": ev.name=" + name);
                    modifiedProperties[name] = 1 + (modifiedProperties.ContainsKey(name) ? modifiedProperties[name] : 0);
                    subject.OnNext(ev);
                }
                model.PropertyChanged += onModelPropertyChanged;

                var signalWait = false;
                using (var signal = new Signal()) {
                    using (subject.Timeout(TimeSpan.FromMilliseconds(100))
                        .Subscribe(ev => {
                            LoggerSimple.Put("OnNext: ev=" + ev);
                        }, ex => {
                            LoggerSimple.Put("OnError: " + ex);
                            signal.Set();
                        }))
                    {
                        modifiedProperties.Clear();
                        Factory.DEFERR_INVOKER(() => ChangeModel(model));

                        signalWait = await signal.Wait(TimeSpan.FromSeconds(1));

                    }
                }

                Assert.IsTrue(signalWait);

                LoggerSimple.Put("  " + nameof(MosaicDrawModelPropertyChangedTest) + ": checking...");
                Assert.AreEqual(true, 1 <= modifiedProperties[nameof(IImageModel.Size)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicGameModel.Area)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicGameModel.CellAttr)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicGameModel.MosaicType)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicGameModel.Matrix)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicTestModel.BackgroundColor)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicTestModel.BkFill)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicTestModel.ColorText)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicTestModel.PenBorder)]);

                model.PropertyChanged -= onModelPropertyChanged;
            }
        }

        [Test]
        public void MosaicDrawModelAsIsTest() {
            using (var model = new MosaicTestModel()) {
                Assert.AreEqual(EMosaic.eMosaicSquare1, model.MosaicType);
                Assert.AreEqual(new Matrisize(10, 10), model.SizeField);
                Assert.AreEqual(model.CellAttr.GetSize(model.SizeField), model.Size);
            }
        }

        [Test]
        public void AutoFitTrueCheckAffectsToPaddingTest() {
            using (var model = new MosaicTestModel()) {
                // set property
                model.AutoFit = true;
                model.Size = new SizeDouble(1000, 1000);
                model.Padding = new BoundDouble(100);

                // change property
                model.Size = new SizeDouble(500, 700);

                // check dependency
                Assert.AreEqual(50.0, model.Padding.Left);
                Assert.AreEqual(50.0, model.Padding.Right);
                Assert.AreEqual(70.0, model.Padding.Top);
                Assert.AreEqual(70.0, model.Padding.Bottom);
            }
        }

        [Test]
        public void AutoFitTrueCheckAffectsTest() {
            MosaicTestModel createTestModel() {
                var model = new MosaicTestModel {
                    // set property
                    AutoFit = true,
                    Size = new SizeDouble(1000, 1000)
                };

                // default check
                var size = model.Size;
                Assert.AreEqual(1000, size.Width , P);
                Assert.AreEqual(1000, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(1000, mosaicSize.Width , P);
                Assert.AreEqual(1000, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(0, mosaicOffset.Width , P);
                Assert.AreEqual(0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , P);
                Assert.AreEqual(0, padding.Top   , P);
                Assert.AreEqual(0, padding.Right , P);
                Assert.AreEqual(0, padding.Bottom, P);

                return model;
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(500, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(100, mosaicOffset.Width , P);
                Assert.AreEqual(  0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , P);
                Assert.AreEqual(0, padding.Top   , P);
                Assert.AreEqual(0, padding.Right , P);
                Assert.AreEqual(0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Padding = new BoundDouble(150, 75, 50, 25);
                model.Size = new SizeDouble(700, 500);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(450, mosaicSize.Width , P);
                Assert.AreEqual(450, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(160 , mosaicOffset.Width , P);
                Assert.AreEqual(37.5, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(105 , padding.Left  , P);
                Assert.AreEqual(37.5, padding.Top   , P);
                Assert.AreEqual(35  , padding.Right , P);
                Assert.AreEqual(12.5, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(525, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(87.5, mosaicOffset.Width , P);
                Assert.AreEqual(   0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , P);
                Assert.AreEqual(0, padding.Top   , P);
                Assert.AreEqual(0, padding.Right , P);
                Assert.AreEqual(0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(350, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(175, mosaicOffset.Width , P);
                Assert.AreEqual(  0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , P);
                Assert.AreEqual(0, padding.Top   , P);
                Assert.AreEqual(0, padding.Right , P);
                Assert.AreEqual(0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.Padding = new BoundDouble(150, 75, 50, 25);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(280, mosaicSize.Width , P);
                Assert.AreEqual(400, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(260, mosaicOffset.Width , P);
                Assert.AreEqual( 75, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(150, padding.Left  , P);
                Assert.AreEqual( 75, padding.Top   , P);
                Assert.AreEqual( 50, padding.Right , P);
                Assert.AreEqual( 25, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.Padding = new BoundDouble(-150, -75, -50, -25);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(420, mosaicSize.Width , P);
                Assert.AreEqual(600, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual( 90, mosaicOffset.Width , P);
                Assert.AreEqual(-75, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(-150, padding.Left  , P);
                Assert.AreEqual(- 75, padding.Top   , P);
                Assert.AreEqual(- 50, padding.Right , P);
                Assert.AreEqual(- 25, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.Padding = new BoundDouble(-150, -75, -50, -25);
                model.Area = 100;

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(105, size.Width , P);
                Assert.AreEqual(150, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(105, mosaicSize.Width , P);
                Assert.AreEqual(150, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(0, mosaicOffset.Width , P);
                Assert.AreEqual(0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , P);
                Assert.AreEqual(0, padding.Top   , P);
                Assert.AreEqual(0, padding.Right , P);
                Assert.AreEqual(0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.Padding = new BoundDouble(150, 75, 50, 25);
                model.Area = 100;

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(305, size.Width , P);
                Assert.AreEqual(250, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(105, mosaicSize.Width , P);
                Assert.AreEqual(150, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(150, mosaicOffset.Width , P);
                Assert.AreEqual( 75, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(150, padding.Left  , P);
                Assert.AreEqual( 75, padding.Top   , P);
                Assert.AreEqual( 50, padding.Right , P);
                Assert.AreEqual( 25, padding.Bottom, P);
            }


            using (var model = createTestModel()) {
                // change property
                model.MosaicOffset = new SizeDouble(200, 300);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(1000, size.Width , P);
                Assert.AreEqual(1000, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(1000, mosaicSize.Width , P);
                Assert.AreEqual(1000, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(200, mosaicOffset.Width , P);
                Assert.AreEqual(300, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual( 200, padding.Left  , P);
                Assert.AreEqual( 300, padding.Top   , P);
                Assert.AreEqual(-200, padding.Right , P);
                Assert.AreEqual(-300, padding.Bottom, P);
            }
        }

        [Test]
        public void AutoFitFalseCheckAffectsTest() {
            MosaicTestModel createTestModel() {
                var model = new MosaicTestModel {
                    // set property
                    AutoFit = false,
                    Size = new SizeDouble(1000, 1000)
                };

                // default check
                var size = model.Size;
                Assert.AreEqual(1000, size.Width , P);
                Assert.AreEqual(1000, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(1000, mosaicSize.Width , P);
                Assert.AreEqual(1000, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(0, mosaicOffset.Width , P);
                Assert.AreEqual(0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , P);
                Assert.AreEqual(0, padding.Top   , P);
                Assert.AreEqual(0, padding.Right , P);
                Assert.AreEqual(0, padding.Bottom, P);

                return model;
            }

            using (var model = createTestModel()) {
                // change property
                model.MosaicOffset = new SizeDouble(200, 300);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(1000, size.Width , P);
                Assert.AreEqual(1000, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(1000, mosaicSize.Width , P);
                Assert.AreEqual(1000, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(200, mosaicOffset.Width , P);
                Assert.AreEqual(300, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual( 200, padding.Left  , P);
                Assert.AreEqual( 300, padding.Top   , P);
                Assert.AreEqual(-200, padding.Right , P);
                Assert.AreEqual(-300, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.MosaicOffset = new SizeDouble(10, 15);
                model.Size = new SizeDouble(700, 500);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(500, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(100, mosaicOffset.Width , P);
                Assert.AreEqual(  0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(100, padding.Left  , P);
                Assert.AreEqual(  0, padding.Top   , P);
                Assert.AreEqual(100, padding.Right , P);
                Assert.AreEqual(  0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(525, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(87.5, mosaicOffset.Width , P);
                Assert.AreEqual(   0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(87.5, padding.Left  , P);
                Assert.AreEqual(   0, padding.Top   , P);
                Assert.AreEqual(87.5, padding.Right , P);
                Assert.AreEqual(   0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(350, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(175, mosaicOffset.Width , P);
                Assert.AreEqual(  0, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(175, padding.Left  , P);
                Assert.AreEqual(  0, padding.Top   , P);
                Assert.AreEqual(175, padding.Right , P);
                Assert.AreEqual(  0, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.MosaicOffset = new SizeDouble(-15, -40);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(350, mosaicSize.Width , P);
                Assert.AreEqual(500, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(-15, mosaicOffset.Width , P);
                Assert.AreEqual(-40, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(-15, padding.Left  , P);
                Assert.AreEqual(-40, padding.Top   , P);
                Assert.AreEqual(365, padding.Right , P);
                Assert.AreEqual( 40, padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.MosaicOffset = new SizeDouble(-15, -40);
                model.Area = 225;

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(157.5, mosaicSize.Width , P);
                Assert.AreEqual(225  , mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(-15, mosaicOffset.Width , P);
                Assert.AreEqual(-40, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(-15  , padding.Left  , P);
                Assert.AreEqual(-40  , padding.Top   , P);
                Assert.AreEqual(557.5, padding.Right , P);
                Assert.AreEqual(315  , padding.Bottom, P);
            }

            using (var model = createTestModel()) {
                // change property
                model.Size = new SizeDouble(700, 500);
                model.MosaicType = EMosaic.eMosaicSquare2;
                model.SizeField = new Matrisize(10, 15);
                model.MosaicOffset = new SizeDouble(-15, -40);
                model.Area = 225;
                model.Padding = new BoundDouble(150, 75, 50, 25);

                // check dependency (evenly expanded)
                var size = model.Size;
                Assert.AreEqual(700, size.Width , P);
                Assert.AreEqual(500, size.Height, P);

                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(280, mosaicSize.Width , P);
                Assert.AreEqual(400, mosaicSize.Height, P);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(260, mosaicOffset.Width , P);
                Assert.AreEqual( 75, mosaicOffset.Height, P);

                var padding = model.Padding;
                Assert.AreEqual(150, padding.Left  , P);
                Assert.AreEqual( 75, padding.Top   , P);
                Assert.AreEqual( 50, padding.Right , P);
                Assert.AreEqual( 25, padding.Bottom, P);
            }
        }

        internal static void ChangeModel(MosaicTestModel m) {
            m.MosaicType = EMosaic.eMosaicQuadrangle1;
            m.SizeField = new Matrisize(22, 33);
            m.Size = new SizeDouble(TEST_SIZE_W, TEST_SIZE_H);
          //m.Area = 1234;
            m.Padding = new BoundDouble(10);
            m.BackgroundColor = Color.DimGray;
            m.BkFill.Mode = 1;
            m.ColorText.SetColorClose(1, Color.LightSalmon);
            m.ColorText.SetColorOpen(2, Color.MediumSeaGreen);
            m.PenBorder.ColorLight = Color.MediumPurple;
            m.PenBorder.Width = 2;
        }

        [Test]
        public async Task MosaicNoChangedTest() {
            LoggerSimple.Put("> MosaicNoChangedTest");

            using (var model = new MosaicTestModel()) {
                var size = model.Size; // implicit call setter Size
                Assert.IsNotNull(size);
                await Task.Delay(TimeSpan.FromMilliseconds(50));

                var modifiedProperties = new List<string>();
                void onModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    LoggerSimple.Put("  MosaicNoChangedTest: onModelPropertyChanged: ev.name=" + ev.PropertyName);
                    modifiedProperties.Add(ev.PropertyName);
                }
                model.PropertyChanged += onModelPropertyChanged;

                model.Size = model.Size;
                model.Area = model.Area;
                model.SizeField = model.SizeField;
                model.Padding = model.Padding;

                await Task.Delay(TimeSpan.FromMilliseconds(200));
                Assert.IsFalse(modifiedProperties.Any());

                model.PropertyChanged -= onModelPropertyChanged;
            }
        }

    }

}
