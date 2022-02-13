using System;
using Windows.Foundation.Collections;
using Fmg.Common;
using Fmg.Core.App.Model;
using Fmg.Core.App.Serializers;
using Fmg.Uwp.App.Model;
using Fmg.Uwp.App.Presentation;

namespace Fmg.Uwp.App.Serializers {

    /// <summary> Application data (de)serializer. For save / restore <see cref="AppData"/> </summary>
    public class AppDataSerializer : ISerializer {

        /// <summary> Singleton </summary>
        public static MosaicInitData MosaicInitData { get; } = new MosaicInitData();
        /// <summary> Singleton </summary>
        public static MenuSettings MenuSettings { get; } = new MenuSettings();

        private const int VERSION = 1;

        private const string KEY__APP_DATA__VERSION              = "Version";
        private const string KEY__MENU_SETTINGS__SECTION         = nameof(MenuSettings);
        private const string KEY__MENU_SETTINGS__SPLIT_PANE_OPEN = nameof(MenuSettings.SplitPaneOpen);

        public AppData Load(IPropertySet from) {
            try {
                if (!from.ContainsKey(KEY__APP_DATA__VERSION))
                    return new AppData();

                int version = (int)from[KEY__APP_DATA__VERSION];
                if (version != VERSION)
                    throw new ArgumentException($"AppDataSerializer: Version #{version} is not supported");

                AppData data = new AppData();
                var mosaicPageBackupData = new MosaicPageBackupDataSerializer().Load(from);
                if (mosaicPageBackupData == null)
                    data.MosaicInitData = new MosaicInitDataSerializer().Load(from);
                else
                    data.MosaicPageBackupData = mosaicPageBackupData;

                var menuSection = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MENU_SETTINGS__SECTION];
                data.SplitPaneOpen = (bool)menuSection[KEY__MENU_SETTINGS__SPLIT_PANE_OPEN];

                return data;
            } catch(Exception ex) {
                Logger.Error($"Can`t load app settings: {ex.Message}");
            }
            return new AppData();
        }

        public void Save(AppData data, IPropertySet to) {
            to.Clear();

            to[KEY__APP_DATA__VERSION] = VERSION;

            var mosaicPageBackupData = data.MosaicPageBackupData;
            if (mosaicPageBackupData == null)
                new MosaicInitDataSerializer().Save(data.MosaicInitData, to);
            else
                new MosaicPageBackupDataSerializer().Save(mosaicPageBackupData, to);

            to[KEY__MENU_SETTINGS__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MENU_SETTINGS__SPLIT_PANE_OPEN] = data.SplitPaneOpen,
            };
        }

    }

}
