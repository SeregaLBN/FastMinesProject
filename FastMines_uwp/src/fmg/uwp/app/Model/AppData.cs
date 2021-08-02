using Fmg.Core.App.Model;
using Fmg.Uwp.App.Presentation;

namespace Fmg.Uwp.App.Model {

    /// <summary> Data model of the application to save/restore </summary>
    public class AppData {

        public MosaicInitData MosaicInitData { get; set; }
        public bool SplitPaneOpen { get; set; }

        public AppData() {
            SetDefaults();
        }

        public void SetDefaults() {
            MosaicInitData = new MosaicInitData();
            SplitPaneOpen = MenuSettings.DEFAULT_SPLIT_PANE_OPEN;
        }

    }

}
