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
using fmg.core.img;
using DummyMosaicImageType = System.Object;

namespace fmg.core.mosaic {

    public class MosaicModelTest {

        [SetUp]
        public void Setup() {
            Factory.DEFERR_INVOKER = doRun => Task.Delay(10).ContinueWith(t => doRun());
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
        public async Task MosaicDrawModelPropertyChangedTest() {
            using (var model = new MosaicDrawModel<DummyMosaicImageType>()) {
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
            using (var model = new MosaicDrawModel<DummyMosaicImageType>()) {
                Assert.AreEqual(model.CellAttr.GetSize(model.SizeField), model.Size);
            }
        }

    }

}
