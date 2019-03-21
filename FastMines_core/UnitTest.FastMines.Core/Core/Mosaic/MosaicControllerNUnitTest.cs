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

    public class MosaicControllerNUnitTest {

        /// <summary> double precision </summary>
        private const double P = MosaicModelTest.P;

        [SetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicControllerTest) + "::" + nameof(Setup));

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
            LoggerSimple.Put("> " + nameof(MosaicControllerTest) + "::" + nameof(PropertyChangedTest));

            using (var ctrlr = new MosaicTestController()) {
                await new PropertyChangeExecutor<MosaicTestController>(ctrlr).Run(100, 1000,
                    () => {
                        MosaicModelTest.ChangeModel(ctrlr.Model);
                    }, modifiedProperties => {
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(ctrlr.Image)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(ctrlr.Image)]);
                    });
            }
        }

        [Test]
        public void ReadinessAtTheStartTest() {
            LoggerSimple.Put("> " + nameof(MosaicControllerTest) + "::" + nameof(ReadinessAtTheStartTest));

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
