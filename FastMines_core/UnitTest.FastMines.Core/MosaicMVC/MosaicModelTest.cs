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
//using DummyMosaicImageType = System.Object;
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<System.Object>;

namespace fmg.core.mosaic {

    public class MosaicModelTest {

        [OneTimeSetUp]
        public void Setup() {
            Factory.DEFERR_INVOKER = doRun => Task.Delay(10).ContinueWith(t => doRun());
            LoggerSimple.Put("Setup " + nameof(MosaicModelTest));
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
                    LoggerSimple.Put("  MosaicDrawModelPropertyChangedTest: onModelPropertyChanged: ev.name=" + name);
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

                        LoggerSimple.Put("  MosaicDrawModelPropertyChangedTest: checking...");
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
        public void AutoFitTrueCheckAffectsToMosaicSizeTest() {
            using (var model = new MosaicTestModel()) {
                // set property
                model.AutoFit = true;
                model.Size = new SizeDouble(1000, 1000);

                // check dependency (evenly expanded)
                var mosaicSize = model.MosaicSize;
                Assert.AreEqual(1000, mosaicSize.Width);
                Assert.AreEqual(1000, mosaicSize.Height);

                var mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(0, mosaicOffset.Width , 0);
                Assert.AreEqual(0, mosaicOffset.Height, 0);

                var padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , 0);
                Assert.AreEqual(0, padding.Top   , 0);
                Assert.AreEqual(0, padding.Right , 0);
                Assert.AreEqual(0, padding.Bottom, 0);


                // change property
                model.Size = new SizeDouble(700, 500);

                // check dependency (evenly expanded)
                mosaicSize = model.MosaicSize;
                Assert.AreEqual(500, mosaicSize.Width , 0.001);
                Assert.AreEqual(500, mosaicSize.Height, 0.001);

                mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(100, mosaicOffset.Width , 0.001);
                Assert.AreEqual(  0, mosaicOffset.Height, 0.001);

                padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , 0.001);
                Assert.AreEqual(0, padding.Top   , 0.001);
                Assert.AreEqual(0, padding.Right , 0.001);
                Assert.AreEqual(0, padding.Bottom, 0.001);


                // change property
                model.MosaicType = EMosaic.eMosaicSquare2;

                // check dependency (evenly expanded)
                mosaicSize = model.MosaicSize;
                Assert.AreEqual(525, mosaicSize.Width , 0.001);
                Assert.AreEqual(500, mosaicSize.Height, 0.001);

                mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(87.5, mosaicOffset.Width , 0.001);
                Assert.AreEqual(   0, mosaicOffset.Height, 0.001);

                padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , 0.001);
                Assert.AreEqual(0, padding.Top   , 0.001);
                Assert.AreEqual(0, padding.Right , 0.001);
                Assert.AreEqual(0, padding.Bottom, 0.001);


                // change property
                model.SizeField = new Matrisize(10, 15);

                // check dependency (evenly expanded)
                mosaicSize = model.MosaicSize;
                Assert.AreEqual(350, mosaicSize.Width , 0.001);
                Assert.AreEqual(500, mosaicSize.Height, 0.001);

                mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(175, mosaicOffset.Width , 0.001);
                Assert.AreEqual(  0, mosaicOffset.Height, 0.001);

                padding = model.Padding;
                Assert.AreEqual(0, padding.Left  , 0.001);
                Assert.AreEqual(0, padding.Top   , 0.001);
                Assert.AreEqual(0, padding.Right , 0.001);
                Assert.AreEqual(0, padding.Bottom, 0.001);


                // change property
                model.Padding = new BoundDouble(150, 75, 50, 25);

                // check dependency (evenly expanded)
                mosaicSize = model.MosaicSize;
                Assert.AreEqual(280, mosaicSize.Width , 0.001);
                Assert.AreEqual(400, mosaicSize.Height, 0.001);

                mosaicOffset = model.MosaicOffset;
                Assert.AreEqual(260, mosaicOffset.Width , 0.001);
                Assert.AreEqual( 75, mosaicOffset.Height, 0.001);

                padding = model.Padding;
                Assert.AreEqual(150, padding.Left  , 0.001);
                Assert.AreEqual( 75, padding.Top   , 0.001);
                Assert.AreEqual( 50, padding.Right , 0.001);
                Assert.AreEqual( 25, padding.Bottom, 0.001);


                // change property
                model.Padding = new BoundDouble(-150, -75, -50, -25);

                // check dependency (evenly expanded)
                mosaicSize = model.MosaicSize;
                Assert.AreEqual(420, mosaicSize.Width , 0.001);
                Assert.AreEqual(600, mosaicSize.Height, 0.001);

                mosaicOffset = model.MosaicOffset;
                Assert.AreEqual( 90, mosaicOffset.Width , 0.001);
                Assert.AreEqual(-75, mosaicOffset.Height, 0.001);

                padding = model.Padding;
                Assert.AreEqual(-150, padding.Left  , 0.001);
                Assert.AreEqual(- 75, padding.Top   , 0.001);
                Assert.AreEqual(- 50, padding.Right , 0.001);
                Assert.AreEqual(- 25, padding.Bottom, 0.001);
            }
        }

    }

}
