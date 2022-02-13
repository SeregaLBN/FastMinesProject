using System;
using Windows.Foundation.Collections;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.App.Model;
using Fmg.Core.App.Serializers;
using Fmg.Core.Types;

namespace Fmg.Uwp.App.Serializers {

    /// <summary> Mosaic data (de)serializer. For save/restore <see cref="MosaicInitData"/> </summary>
    public class MosaicInitDataSerializer : ISerializer {

        private const int VERSION = 1;

        private const           string KEY__MOSAIC_INIT_DATA__SECTION      = nameof(MosaicInitData);
        private const           string KEY__MOSAIC_INIT_DATA__VERSION      = ".version";
        private static readonly string KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M = nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.m);
        private static readonly string KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N = nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.n);
        private const           string KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE  = nameof(MosaicInitData.MosaicType);
        private const           string KEY__MOSAIC_INIT_DATA__COUNT_MINES  = nameof(MosaicInitData.CountMines);

        public MosaicInitData Load(IPropertySet from) {
            if (from.ContainsKey(KEY__MOSAIC_INIT_DATA__SECTION)) try {
                var section = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MOSAIC_INIT_DATA__SECTION];
            
                if (!section.ContainsKey(KEY__MOSAIC_INIT_DATA__VERSION))
                    return new MosaicInitData();

                int version = (int)section[KEY__MOSAIC_INIT_DATA__VERSION];
                if (version != 1)
                    throw new ArgumentException("MosaicInitDataSerializer: Version #" + version + " is not supported");

                int mosaicTypeOrdinal = (int)section[KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE];
                int sizeFieldM        = (int)section[KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M];
                int sizeFieldN        = (int)section[KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N];
                int countMines        = (int)section[KEY__MOSAIC_INIT_DATA__COUNT_MINES];

                return new MosaicInitData {
                    MosaicType = EMosaicEx.FromOrdinal(mosaicTypeOrdinal),
                    SizeField  = new Matrisize(sizeFieldM, sizeFieldN),
                    CountMines = countMines
                };
            } catch(Exception ex) {
                Logger.Error($"Can`t load mosaic init data: {ex.Message}");
            }
            return new MosaicInitData();
        }

        public void Save(MosaicInitData data, IPropertySet to) {
            to[KEY__MOSAIC_INIT_DATA__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MOSAIC_INIT_DATA__VERSION     ] = VERSION,
                [KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE ] = data.MosaicType.Ordinal(),
                [KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M] = data.SizeField.m,
                [KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N] = data.SizeField.n,
                [KEY__MOSAIC_INIT_DATA__COUNT_MINES ] = data.CountMines,
            };
        }

    }

}
