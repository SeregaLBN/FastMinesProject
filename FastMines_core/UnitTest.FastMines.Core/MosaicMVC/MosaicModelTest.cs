using System;
using System.Linq;
using System.Threading;
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
//using DummyImage = System.Object;
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<System.Object>;

namespace fmg.core.mosaic {

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

    public class MosaicModelTest {

        /// <summary> double precision </summary>
        private const double P = 0.001;

        [OneTimeSetUp]
        public void Setup() {
            LoggerSimple.Put(nameof(Setup) + "::" + nameof(MosaicModelTest));
            Factory.DEFERR_INVOKER = doRun => Task.Delay(10).ContinueWith(t => doRun());
        }

        [OneTimeTearDown]
        public void Closed() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("Closed " + nameof(MosaicModelTest));
        }

        [SetUp]
        public void Before() {
            LoggerSimple.Put("======================================================");
        }

        [Test]
        public async Task MosaicGameModelPropertyChangedTest() {
            using (var model = new MosaicGameModel()) {
                Assert.IsTrue(model.Matrix.Any());
                Assert.IsTrue(ReferenceEquals(model.CellAttr, model.Matrix.First().Attr));

                var modifiedProperties = new List<string>();
                void onModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
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
                    using (subject.Timeout(TimeSpan.FromMilliseconds(50))
                        .Subscribe(ev => {
                            LoggerSimple.Put("OnNext: ev=" + ev);
                        }, ex => {
                            LoggerSimple.Put("OnError: " + ex);
                            signal.Set();
                        }))
                    {
                        modifiedProperties.Clear();
                        model.Size = new SizeDouble(123, 456);

                        signalWait = await signal.Wait(TimeSpan.FromSeconds(1));

                        LoggerSimple.Put("  " + nameof(MosaicDrawModelPropertyChangedTest) + ": checking...");
                    }
                }

                Assert.IsTrue(signalWait);

                Assert.AreEqual(1, modifiedProperties[nameof(IImageModel.Size)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicGameModel.Area)]);
                Assert.AreEqual(1, modifiedProperties[nameof(MosaicGameModel.CellAttr)]);

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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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

            using (MosaicTestModel model = createTestModel()) {
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
    }

}
