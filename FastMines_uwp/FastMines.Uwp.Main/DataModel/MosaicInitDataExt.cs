using System;
using Windows.Foundation.Collections;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;

namespace FastMines.Uwp.App.Model {

    public static class MosaicInitDataExt {

        /// <summary> Singleton </summary>
        public static MosaicInitData SharedData { get; } = new MosaicInitData();

        private const           string KEY__SECTION      = nameof(MosaicInitData);
        private static readonly string KEY__SIZE_FIELD_M = nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.m);
        private static readonly string KEY__SIZE_FIELD_N = nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.n);
        private const           string KEY__MOSAIC_TYPE  = nameof(MosaicInitData.MosaicType);
        private const           string KEY__MINES_COUNT  = nameof(MosaicInitData.MinesCount);

        public static MosaicInitData Load(IPropertySet from) {
            if (from.ContainsKey(KEY__SECTION))
            try {
                var compositeMosaic = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__SECTION];
                int mosaicTypeOrdinal = (int)compositeMosaic[KEY__MOSAIC_TYPE];
                int sizeFieldM        = (int)compositeMosaic[KEY__SIZE_FIELD_M];
                int sizeFieldN        = (int)compositeMosaic[KEY__SIZE_FIELD_N];
                int minesCount        = (int)compositeMosaic[KEY__MINES_COUNT];

                var loadData = new MosaicInitData();
                loadData.MosaicType = EMosaicEx.FromOrdinal(mosaicTypeOrdinal);
                loadData.SizeField = new Matrisize(sizeFieldM, sizeFieldN);
                loadData.MinesCount = minesCount;
                return loadData;
            } catch (Exception ex) {
                LoggerSimple.Put($"Fail load data: {ex.Message}");
                System.Diagnostics.Debug.Assert(false, ex.Message);
            }
            return new MosaicInitData();
        }

        public static void Save(IPropertySet to, MosaicInitData initData) {
            to[KEY__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MOSAIC_TYPE ] = initData.MosaicType.Ordinal(),
                [KEY__SIZE_FIELD_M] = initData.SizeField.m,
                [KEY__SIZE_FIELD_N] = initData.SizeField.n,
                [KEY__MINES_COUNT ] = initData.MinesCount,
            };
        }

    }

}
