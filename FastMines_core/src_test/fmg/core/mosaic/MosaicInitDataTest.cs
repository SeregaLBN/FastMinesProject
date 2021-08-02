using System;
using System.Threading.Tasks;
using Fmg.Common;
using Fmg.Common.Notifier;
using Fmg.Core.Types;
using Fmg.Core.App.Model;

namespace Fmg.Core.Mosaic {

    public abstract class MosaicInitDataTest {

        protected abstract void AssertEqual(int    expected, int    actual);
        protected abstract void AssertEqual(object expected, object actual);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertFalse(bool condition);
        protected abstract void AssertFail();

        public virtual void Setup() {
            Logger.Info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            Logger.Info("======================================================");
        }

        public virtual void After() {
            Logger.Info("======================================================");
            Logger.Info("< " + nameof(MosaicInitDataTest) + " closed");
            Logger.Info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        private MosaicInitData CreateMosaicInitData() {
            var initData = new MosaicInitData();
            AssertEqual(MosaicInitData.DEFAULT_MOSAIC_TYPE , initData.MosaicType);
            AssertEqual(MosaicInitData.DEFAULT_SIZE_FIELD_M, initData.SizeField.m);
            AssertEqual(MosaicInitData.DEFAULT_SIZE_FIELD_N, initData.SizeField.n);
            AssertEqual(MosaicInitData.DEFAULT_COUNT_MINES , initData.CountMines);
            AssertEqual(MosaicInitData.DEFAULT_SKILL_LEVEL , initData.SkillLevel);

            AssertEqual(MosaicInitData.DEFAULT_MOSAIC_TYPE.GetGroup(), initData.MosaicGroup);
            return initData;
        }


        public virtual async Task CheckTheImpossibilitySetCustomSkillLevelTest() {
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckTheImpossibilitySetCustomSkillLevelTest));
            try {
                await new PropertyChangeExecutor<MosaicInitData>(CreateMosaicInitData).Run(10, 1000,
                    initData => {
                        initData.SkillLevel = ESkillLevel.eCustom;
                        AssertFail();
                    }, (initData, modifiedProperties) => { });
            } catch (Exception ex) {
                AssertEqual(typeof(ArgumentException), ex.GetType());
            }
        }

        public virtual async Task CheckIfMosaicTypeIsChangedThenCountMinesWillAlsoBeChangedTest() {
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckIfMosaicTypeIsChangedThenCountMinesWillAlsoBeChangedTest));

            await new PropertyChangeExecutor<MosaicInitData>(() => CreateMosaicInitData()).Run(300, 5000,
                initData => {
                    initData.MosaicType = EMosaic.eMosaicRhombus1;
                }, (initData, modifiedProperties) => {
                    AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicType)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicType)]);
                    AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.CountMines)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.CountMines)]);
                    AssertEqual(2, modifiedProperties.Count);
                    AssertEqual(EMosaic.eMosaicRhombus1, initData.MosaicType);
                    AssertEqual(initData.SkillLevel.GetNumberMines(EMosaic.eMosaicRhombus1), initData.CountMines);
                });
        }

        public virtual async Task CheckNoRepeatNotificationsTest() {
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckNoRepeatNotificationsTest));

            await new PropertyChangeExecutor<MosaicInitData>(() => CreateMosaicInitData()).Run(300, 5000,
                initData => {
                    Logger.Info("    initData.countMines={0}", initData.CountMines);
                    initData.MosaicType = EMosaic.eMosaicRhombus1;
                    Logger.Info("    initData.countMines={0}", initData.CountMines);
                    initData.MosaicType = EMosaic.eMosaicHexagon1;
                    Logger.Info("    initData.countMines={0}", initData.CountMines);
                }, (initData, modifiedProperties) => {
                    AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicType)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicType)]);
                    AssertTrue  (  modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicGroup)));
                    AssertEqual(1, modifiedProperties[            nameof(MosaicInitData.MosaicGroup)]);
                    AssertTrue  ( !modifiedProperties.ContainsKey(nameof(MosaicInitData.CountMines)));
                    AssertEqual(2, modifiedProperties.Count);
                    AssertEqual(EMosaic.eMosaicHexagon1, initData.MosaicType);
                    AssertEqual(initData.SkillLevel.GetNumberMines(EMosaic.eMosaicHexagon1), initData.CountMines);
                });
        }

        public virtual async Task CheckChangedMosaicGroupTest() {
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckChangedMosaicGroupTest));

            await new PropertyChangeExecutor<MosaicInitData>(() => CreateMosaicInitData()).Run(300, 5000,
                initData => {
                    initData.MosaicType = EMosaic.eMosaicHexagon1;
                }, (initData, modifiedProperties) => {
                    AssertTrue(modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicGroup)));
                });
        }

        public virtual async Task CheckNoChangedMosaicGroupTest() {
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckNoChangedMosaicGroupTest));

            await new PropertyChangeExecutor<MosaicInitData>(() => CreateMosaicInitData()).Run(300, 5000,
                initData => {
                    initData.MosaicType = EMosaic.eMosaicRhombus1;
                }, (initData, modifiedProperties) => {
                    AssertFalse(modifiedProperties.ContainsKey(nameof(MosaicInitData.MosaicGroup)));
                });
        }

        public virtual async Task CheckRestoreIndexInGroupTest() {
            Logger.Info("> " + nameof(MosaicInitDataTest) + "::" + nameof(CheckRestoreIndexInGroupTest));

            await new PropertyChangeExecutor<MosaicInitData>(CreateMosaicInitData).Run(10, 1000,
                initData => {
                    const int checkOrdinal = 3;

                    // 1. select another mosaic in current group
                    var mosaicsInOldGroup = initData.MosaicGroup.GetMosaics();
                    initData.MosaicType = mosaicsInOldGroup[checkOrdinal];

                    // 2. change group
                    initData.MosaicGroup = EMosaicGroup.eTriangles;

                    // 3. check ordinal in new group
                    AssertEqual(checkOrdinal, initData.MosaicType.GetOrdinalInGroup());
                }, (initData, modifiedProperties) => { });
        }

    }

}
