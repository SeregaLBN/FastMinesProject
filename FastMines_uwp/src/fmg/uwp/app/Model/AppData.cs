using System.Linq;
using Fmg.Core.App.Model;
using Fmg.Uwp.App.Presentation;

namespace Fmg.Uwp.App.Model {

    /// <summary> Data model of the application to save/restore </summary>
    public class AppData {

        private MosaicInitData mosaicInitData;
        private MosaicPageBackupData mosaicPageBackupData;
        public bool SplitPaneOpen { get; set; }

        public AppData() {
            SetDefaults();
        }

        public void SetDefaults() {
            mosaicInitData = new MosaicInitData();
            mosaicPageBackupData = null;
            SplitPaneOpen = MenuSettings.DEFAULT_SPLIT_PANE_OPEN;
        }

        public MosaicInitData MosaicInitData {
            get {
                if (mosaicPageBackupData != null) {
                    MosaicBackupData mosaicBackupData = mosaicPageBackupData.MosaicBackupData;
                    return new MosaicInitData {
                        MosaicType = mosaicBackupData.MosaicType,
                        SizeField = mosaicBackupData.SizeField,
                        CountMines = mosaicBackupData.CellStates
                                                     .Where(c => c.Open == Core.Types.EOpen._Mine)
                                                     .Count()
                    };
                }
                return mosaicInitData;
            }
            set {
                mosaicInitData = value;
                if (mosaicInitData != null)
                    mosaicPageBackupData = null;
            }
        }

        public MosaicPageBackupData MosaicPageBackupData {
            get {
                return mosaicPageBackupData;
            }
            set {
                mosaicPageBackupData = value;
                if (mosaicPageBackupData != null)
                    mosaicInitData = null;
            }
        }

    }

}
