using System;
using Windows.Foundation.Collections;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Mosaic;
using Fmg.Uwp.App.Presentation;

namespace Fmg.Uwp.App.Model {

    public static class SharedData {

        /// <summary> Singleton </summary>
        public static MosaicInitData MosaicInitData { get; } = new MosaicInitData();
        /// <summary> Singleton </summary>
        public static MenuSettings MenuSettings { get; } = new MenuSettings();

        private const           string KEY__MOSAIC_INIT_DATA__SECTION      = nameof(MosaicInitData);
        private static readonly string KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M = nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.m);
        private static readonly string KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N = nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.n);
        private const           string KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE  = nameof(MosaicInitData.MosaicType);
        private const           string KEY__MOSAIC_INIT_DATA__COUNT_MINES = nameof(MosaicInitData.CountMines);
        private static readonly string KEY__MENU_SETTINGS__SECTION         = nameof(MenuSettings);
        private static readonly string KEY__MENU_SETTINGS__SPLIT_PANE_OPEN = nameof(MenuSettings) + '.' + nameof(MenuSettings.SplitPaneOpen);

        private static MosaicInitData LoadMosaicInitData(IPropertySet from) {
            if (from.ContainsKey(KEY__MOSAIC_INIT_DATA__SECTION))
                try {
                    var compositeMosaic = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MOSAIC_INIT_DATA__SECTION];
                    int mosaicTypeOrdinal = (int)compositeMosaic[KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE];
                    int sizeFieldM        = (int)compositeMosaic[KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M];
                    int sizeFieldN        = (int)compositeMosaic[KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N];
                    int countMines        = (int)compositeMosaic[KEY__MOSAIC_INIT_DATA__COUNT_MINES];

                    var loadData = new MosaicInitData {
                        MosaicType = EMosaicEx.FromOrdinal(mosaicTypeOrdinal),
                        SizeField  = new Matrisize(sizeFieldM, sizeFieldN),
                        CountMines = countMines
                    };
                    return loadData;
                } catch(Exception ex) {
                    Logger.Info($"Fail load data: {ex.Message}");
                    System.Diagnostics.Debug.Assert(false, ex.Message);
                }
            return new MosaicInitData();
        }

        private static void Save(IPropertySet to, MosaicInitData initData) {
            to[KEY__MOSAIC_INIT_DATA__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE ] = initData.MosaicType.Ordinal(),
                [KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M] = initData.SizeField.m,
                [KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N] = initData.SizeField.n,
                [KEY__MOSAIC_INIT_DATA__COUNT_MINES ] = initData.CountMines,
            };
        }

        private static MenuSettings LoadMenuSettings(IPropertySet from) {
            if (from.ContainsKey(KEY__MENU_SETTINGS__SECTION))
                try {
                    var compositeMosaic = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MENU_SETTINGS__SECTION];
                    var isSplitPaneOpen = (bool)compositeMosaic[KEY__MENU_SETTINGS__SPLIT_PANE_OPEN];

                    var menuSettings = new MenuSettings {
                        SplitPaneOpen = isSplitPaneOpen
                    };
                    return menuSettings;
                } catch(Exception ex) {
                    Logger.Info($"Fail load data: {ex.Message}");
                    System.Diagnostics.Debug.Assert(false, ex.Message);
                }
            return new MenuSettings();
        }

        private static void Save(IPropertySet to, MenuSettings menuSettings) {
            to[KEY__MENU_SETTINGS__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MENU_SETTINGS__SPLIT_PANE_OPEN] = menuSettings.SplitPaneOpen,
            };
        }


        public static void Load(IPropertySet from) {
            MosaicInitData.CopyFrom(LoadMosaicInitData(from));
            MenuSettings  .CopyFrom(LoadMenuSettings(from));
        }

        public static void Save(IPropertySet to) {
            Save(to, MosaicInitData);
            Save(to, MenuSettings);
        }

    }

}
