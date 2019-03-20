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
using fmg.common.notifier;
using fmg.core.types;
using fmg.core.img;
//using DummyImage = System.Object;
using MosaicTestModel = fmg.core.mosaic.MosaicDrawModel<object>;

namespace fmg.core.mosaic {

    public class MosaicInitDataTest {

        [OneTimeSetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(Setup));

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
            LoggerSimple.Put("< " + nameof(MosaicInitDataTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        private static MosaicInitData CreateMosaicInitData() {
            var initData = new MosaicInitData();
            Assert.AreEqual(MosaicInitData.DEFAULT_MOSAIC_TYPE , initData.MosaicType);
            Assert.AreEqual(MosaicInitData.DEFAULT_SIZE_FIELD_M, initData.SizeField.m);
            Assert.AreEqual(MosaicInitData.DEFAULT_SIZE_FIELD_N, initData.SizeField.n);
            Assert.AreEqual(MosaicInitData.DEFAULT_MINES_COUNT , initData.MinesCount);
            Assert.AreEqual(MosaicInitData.DEFAULT_SKILL_LEVEL , initData.SkillLevel);
            return initData;
        }


        [Test]
        public void CheckTheImpossibilitySetCustomSkillLevelTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckTheImpossibilitySetCustomSkillLevelTest));
            using (var initData = CreateMosaicInitData()) try {
                initData.SkillLevel = ESkillLevel.eCustom;
                Assert.Fail();
            } catch (Exception ex) {
                Assert.AreEqual(typeof(ArgumentException), ex.GetType());
            }
        }

        [Test]
        public async Task CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest));

            using (var initData = CreateMosaicInitData()) {
                await new PropertyChangeExecutor<MosaicInitData>(initData).Run(100, 1000,
                    () => {
                        initData.MosaicType = EMosaic.eMosaicRhombus1;
                    }, modifiedProperties => {
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicType)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicType)]);
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(MosaicInitData.MinesCount)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(MosaicInitData.MinesCount)]);
                        Assert.AreEqual(2, modifiedProperties.Count);
                        Assert.AreEqual(EMosaic.eMosaicRhombus1, initData.MosaicType);
                        Assert.AreEqual(initData.SkillLevel.GetNumberMines(EMosaic.eMosaicRhombus1), initData.MinesCount);
                    });
            }
        }

        [Test]
        public async Task CheckNoRepeatNotificationsTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckNoRepeatNotificationsTest));

            using (var initData = CreateMosaicInitData()) {
                await new PropertyChangeExecutor<MosaicInitData>(initData).Run(100, 1000,
                    () => {
                        LoggerSimple.Put("    initData.minesCount={0}", initData.MinesCount);
                        initData.MosaicType = EMosaic.eMosaicRhombus1;
                        LoggerSimple.Put("    initData.minesCount={0}", initData.MinesCount);
                        initData.MosaicType = EMosaic.eMosaicHexagon1;
                        LoggerSimple.Put("    initData.minesCount={0}", initData.MinesCount);
                    }, modifiedProperties => {
                        Assert.IsTrue  (   modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicType)));
                        Assert.AreEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicType)]);
                        Assert.IsTrue  (  !modifiedProperties.ContainsKey(nameof(MosaicInitData.MinesCount)));
                        Assert.AreEqual(1, modifiedProperties.Count);
                        Assert.AreEqual(EMosaic.eMosaicHexagon1, initData.MosaicType);
                        Assert.AreEqual(initData.SkillLevel.GetNumberMines(EMosaic.eMosaicHexagon1), initData.MinesCount);
                    });
            }
        }

    }

}
