using System;
using Windows.Foundation.Collections;
using Fmg.Common;
using Fmg.Core.App;
using Fmg.Core.Types.Model;
using Fmg.Uwp.App.Model;
using Fmg.Uwp.App.Presentation;

namespace Fmg.Uwp.App.Serializers {

    /// <summary> Application data (de)serializer. For save / restore <see cref="AppData"/> </summary>
    public class AppDataSerializer : ISerializer {

        /// <summary> Singleton </summary>
        public static MosaicInitData MosaicInitData { get; } = new MosaicInitData();
        /// <summary> Singleton </summary>
        public static MenuSettings MenuSettings { get; } = new MenuSettings();

        private static readonly string KEY__MENU_SETTINGS__SECTION         = nameof(MenuSettings);
        private static readonly string KEY__MENU_SETTINGS__SPLIT_PANE_OPEN = nameof(MenuSettings) + '.' + nameof(MenuSettings.SplitPaneOpen);

        public AppData Load(IPropertySet from) {
            if (from.ContainsKey(KEY__MENU_SETTINGS__SECTION)) try {
                var mid = new MosaicInitDataSerializer().Load(from);

                var compositeMosaic = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MENU_SETTINGS__SECTION];
                var isSplitPaneOpen = (bool)compositeMosaic[KEY__MENU_SETTINGS__SPLIT_PANE_OPEN];

                return new AppData() {
                    MosaicInitData = mid,
                    SplitPaneOpen = isSplitPaneOpen
                };
            } catch(Exception ex) {
                Logger.Info($"Fail load data: {ex.Message}");
                System.Diagnostics.Debug.Assert(false, ex.Message);
            }
            return new AppData();
        }

        public void Save(AppData data, IPropertySet to) {
            new MosaicInitDataSerializer().Save(data.MosaicInitData, to);
            to[KEY__MENU_SETTINGS__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MENU_SETTINGS__SPLIT_PANE_OPEN] = data.SplitPaneOpen,
            };
        }

    }

}
