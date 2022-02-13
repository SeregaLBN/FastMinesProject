using Fmg.Common.Geom;
using Fmg.Core.App.Model;

namespace Fmg.Uwp.App.Model {

    /// <summary> Data model of the MosaicPage to save/restore </summary>
    public class MosaicPageBackupData {

        public MosaicBackupData MosaicBackupData {  get; set; }
        public long PlayTime { get; set; }
        public SizeDouble MosaicOffset { get; set; }

    }

}
