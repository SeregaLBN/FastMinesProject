using System;
using System.Threading.Tasks;
using fmg.common;
using fmg.common.notifier;
using fmg.core.types;

namespace fmg.core.mosaic {

    public abstract class MosaicInitDataTest {

        protected abstract void AssertEqual(int    expected, int    actual);
        protected abstract void AssertEqual(object expected, object actual);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertFalse(bool condition);
        protected abstract void AssertFail();

        public virtual void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            LoggerSimple.Put("======================================================");
        }

        public virtual void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(MosaicInitDataTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        private MosaicInitData CreateMosaicInitData() {
            var initData = new MosaicInitData();
            AssertEqual(MosaicInitData.DEFAULT_MOSAIC_TYPE , initData.MosaicType);
            AssertEqual(MosaicInitData.DEFAULT_SIZE_FIELD_M, initData.SizeField.m);
            AssertEqual(MosaicInitData.DEFAULT_SIZE_FIELD_N, initData.SizeField.n);
            AssertEqual(MosaicInitData.DEFAULT_MINES_COUNT , initData.MinesCount);
            AssertEqual(MosaicInitData.DEFAULT_SKILL_LEVEL , initData.SkillLevel);
            return initData;
        }


        public virtual void CheckTheImpossibilitySetCustomSkillLevelTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckTheImpossibilitySetCustomSkillLevelTest));
            using (var initData = CreateMosaicInitData()) try {
                initData.SkillLevel = ESkillLevel.eCustom;
                AssertFail();
            } catch (Exception ex) {
                AssertEqual(typeof(ArgumentException), ex.GetType());
            }
        }

        public virtual async Task CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest));

            using (var initData = CreateMosaicInitData()) {
                await new PropertyChangeExecutor<MosaicInitData>(initData).Run(100, 1000,
                    () => {
                        initData.MosaicType = EMosaic.eMosaicRhombus1;
                    }, modifiedProperties => {
                        AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicType)));
                        AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicType)]);
                        AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MinesCount)));
                        AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MinesCount)]);
                        AssertEqual(2, modifiedProperties.Count);
                        AssertEqual(EMosaic.eMosaicRhombus1, initData.MosaicType);
                        AssertEqual(initData.SkillLevel.GetNumberMines(EMosaic.eMosaicRhombus1), initData.MinesCount);
                    });
            }
        }

        public virtual async Task CheckNoRepeatNotificationsTest() {
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
                        AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicType)));
                        AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicType)]);
                        AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicGroup)));
                        AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicGroup)]);
                        AssertTrue  ( !modifiedProperties.ContainsKey(nameof(MosaicInitData.MinesCount)));
                        AssertEqual(2, modifiedProperties.Count);
                        AssertEqual(EMosaic.eMosaicHexagon1, initData.MosaicType);
                        AssertEqual(initData.SkillLevel.GetNumberMines(EMosaic.eMosaicHexagon1), initData.MinesCount);
                    });
            }
        }

        public virtual async Task CheckChangedMosaicGroupTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckChangedMosaicGroupTest));

            using (var initData = CreateMosaicInitData()) {
                await new PropertyChangeExecutor<MosaicInitData>(initData).Run(100, 1000,
                    () => {
                        initData.MosaicType = EMosaic.eMosaicHexagon1;
                    }, modifiedProperties => {
                        AssertTrue(modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicGroup)));
                    });
            }
        }

        public virtual async Task CheckNoChangedMosaicGroupTest() {
            LoggerSimple.Put("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckNoChangedMosaicGroupTest));

            using (var initData = CreateMosaicInitData()) {
                await new PropertyChangeExecutor<MosaicInitData>(initData).Run(100, 1000,
                    () => {
                        initData.MosaicType = EMosaic.eMosaicRhombus1;
                    }, modifiedProperties => {
                        AssertFalse(modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicGroup)));
                    });
            }
        }

    }

}
