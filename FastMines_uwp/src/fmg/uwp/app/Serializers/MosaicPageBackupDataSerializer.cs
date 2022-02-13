using System;
using Windows.Foundation.Collections;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.App.Model;
using Fmg.Core.App.Serializers;
using Fmg.Uwp.App.Model;

namespace Fmg.Uwp.App.Serializers {

    /// <summary> <see cref="Fmg.Uwp.App.MosaicPage"/> backup data (de)serializer. For save / restore <see cref="MosaicPageBackupData"/> </summary>
    public class MosaicPageBackupDataSerializer : ISerializer {

        private const int VERSION = 1;

        private const           string KEY__MOSAIC_PAGE_BACKUP_DATA__SECTION              = nameof(MosaicPageBackupData);
        private const           string KEY__MOSAIC_PAGE_BACKUP_DATA__VERSION              = "Version";
        private static readonly string KEY__MOSAIC_PAGE_BACKUP_DATA__PLAY_TIME            = nameof(MosaicPageBackupData.PlayTime);
        private static readonly string KEY__MOSAIC_PAGE_BACKUP_DATA__MOSAIC_OFFSET_WIDTH  = nameof(MosaicPageBackupData.MosaicOffset) + '.' + nameof(SizeDouble.Width);
        private static readonly string KEY__MOSAIC_PAGE_BACKUP_DATA__MOSAIC_OFFSET_HEIGHT = nameof(MosaicPageBackupData.MosaicOffset) + '.' + nameof(SizeDouble.Height);
        
        public MosaicPageBackupData Load(IPropertySet from) {
            if (!from.ContainsKey(KEY__MOSAIC_PAGE_BACKUP_DATA__SECTION))
                return null;

            try {
                var section = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MOSAIC_PAGE_BACKUP_DATA__SECTION];
                if (section == null)
                    return null;
                int version = (int)section[KEY__MOSAIC_PAGE_BACKUP_DATA__VERSION];
                if (version != VERSION)
                    throw new ArgumentException("MosaicPageBackupDataSerializer: Version #" + version + " is not supported");

                MosaicBackupData mosaicBackupData = new MosaicBackupDataSerializer().Load(from);
                if (mosaicBackupData == null)
                    return null;

                return new MosaicPageBackupData {
                    MosaicBackupData = mosaicBackupData,
                    PlayTime         = (long)section[KEY__MOSAIC_PAGE_BACKUP_DATA__PLAY_TIME],
                    MosaicOffset     = new SizeDouble((double)section[KEY__MOSAIC_PAGE_BACKUP_DATA__MOSAIC_OFFSET_WIDTH],
                                                      (double)section[KEY__MOSAIC_PAGE_BACKUP_DATA__MOSAIC_OFFSET_HEIGHT])
                };
            } catch(Exception ex) {
                Logger.Error("Can`t load MosaicPageBackupData from SharedPreferences " + ex);
            }
            return null;
        }

        public void Save(MosaicPageBackupData data, IPropertySet to) {
            to[KEY__MOSAIC_PAGE_BACKUP_DATA__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MOSAIC_PAGE_BACKUP_DATA__VERSION             ] = VERSION,
                [KEY__MOSAIC_PAGE_BACKUP_DATA__PLAY_TIME           ] = data.PlayTime,
                [KEY__MOSAIC_PAGE_BACKUP_DATA__MOSAIC_OFFSET_WIDTH ] = data.MosaicOffset.Width,
                [KEY__MOSAIC_PAGE_BACKUP_DATA__MOSAIC_OFFSET_HEIGHT] = data.MosaicOffset.Height,
            };
            new MosaicBackupDataSerializer().Save(data.MosaicBackupData, to);
        }

    }

}
