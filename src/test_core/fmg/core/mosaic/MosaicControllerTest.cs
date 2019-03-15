using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.ComponentModel;
using System.Linq;
using System.Reactive.Linq;
using NUnit.Framework;
using fmg.common;
using fmg.common.geom;
using fmg.common.ui;
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

    public class MosaicControllerTest {

        /// <summary> double precision </summary>
        private const double P = MosaicModelTest.P;

        [SetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> MosaicControllerTest::Setup");

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
            LoggerSimple.Put("< " + nameof(MosaicControllerTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        [Test]
        public async Task PropertyChangedTest() {
            using (var ctrlr = new MosaicTestController()) {
                var modifiedProperties = new Dictionary<string /* property name */, int /* count */>();

                using (var signal = new Signal()) {
                    var ob = Observable
                        .FromEventPattern<PropertyChangedEventHandler, PropertyChangedEventArgs>(h => ctrlr.PropertyChanged += h, h => ctrlr.PropertyChanged -= h);
                    using (ob.Subscribe(
                        ev => {
                            var name = ev.EventArgs.PropertyName;
                            LoggerSimple.Put("  PropertyChangedTest: onCtrlPropertyChanged: ev.name=" + name);
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
                            Factory.DEFERR_INVOKER(() => MosaicModelTest.ChangeModel(ctrlr.Model));

                            Assert.IsTrue(await signal.Wait(TimeSpan.FromSeconds(1)));
                        }
                    }
                }

                LoggerSimple.Put("  PropertyChangedTest: checking...");
                Assert.AreEqual(1, modifiedProperties[nameof(ctrlr.Image)]);
            }
        }

        [Test]
        public void ReadinessAtTheStartTest() {
            const int defArea = 500;
            using (var ctrlr = new MosaicTestController()) {
                Assert.AreEqual(defArea, ctrlr.Model.Area, P);
                Assert.AreEqual(null, ctrlr.CellDown);
                Assert.AreEqual(0, ctrlr.CountClick);
                Assert.AreEqual(0, ctrlr.CountFlag);
                Assert.AreEqual(10, ctrlr.CountMinesLeft);
                Assert.AreEqual(0, ctrlr.CountOpen);
                Assert.AreEqual(0, ctrlr.CountUnknown);
                Assert.AreEqual(EGameStatus.eGSReady, ctrlr.GameStatus);
                Assert.NotNull(ctrlr.Image);
                Assert.NotNull(ctrlr.Matrix);
                Assert.IsTrue(ctrlr.Matrix.Any());
                Assert.AreEqual(EMosaic.eMosaicSquare1, ctrlr.MosaicType);
                Assert.AreEqual(EPlayInfo.ePlayerUnknown, ctrlr.PlayInfo);
                Assert.NotNull(ctrlr.RepositoryMines);
                Assert.IsFalse(ctrlr.RepositoryMines.Any());
                Assert.AreEqual(Math.Sqrt(defArea) * 10, ctrlr.Size.Width, P);
                Assert.AreEqual(Math.Sqrt(defArea) * 10, ctrlr.Size.Height, P);
                Assert.AreEqual(new Matrisize(10, 10), ctrlr.SizeField);
            }
        }

    }

}
