package fmg.android.app.serializers;

import android.content.SharedPreferences;

import fmg.android.app.model.MosaicActivityBackupData;
import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.core.app.model.MosaicBackupData;
import fmg.core.app.serializers.ISerializer;

/** {@link fmg.android.app.MosaicActivity MosaicActivity} backup data (de)serializer. For save / restore {@link MosaicActivityBackupData} */
public class MosaicActivityBackupDataSerializer implements ISerializer {

    private static final int VERSION = 1;

    private static final String KEY__MOSAIC_ACTIVITY_BACKUP_DATA = MosaicActivityBackupData.class.getSimpleName();
    private static final String KEY__MOSAIC_ACTIVITY_BACKUP_DATA__VERSION              = KEY__MOSAIC_ACTIVITY_BACKUP_DATA + ".version";
    private static final String KEY__MOSAIC_ACTIVITY_BACKUP_DATA__PLAY_TIME            = KEY__MOSAIC_ACTIVITY_BACKUP_DATA + ".playTime";
    private static final String KEY__MOSAIC_ACTIVITY_BACKUP_DATA__MOSAIC_OFFSET_WIDTH  = KEY__MOSAIC_ACTIVITY_BACKUP_DATA + ".mosaicOffsetWidth";
    private static final String KEY__MOSAIC_ACTIVITY_BACKUP_DATA__MOSAIC_OFFSET_HEIGHT = KEY__MOSAIC_ACTIVITY_BACKUP_DATA + ".mosaicOffsetHeight";

    public MosaicActivityBackupData load(SharedPreferences from) {
        if (from != null) try {
            int version = from.getInt(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__VERSION, 0);
            if (version == 0)
                return null;
            if (version != VERSION)
                throw new IllegalArgumentException("MosaicActivityBackupDataSerializer: Version #" + version + " is not supported");

            MosaicBackupData mosaicBackupData = new MosaicBackupDataSerializer().load(from);
            if (mosaicBackupData == null)
                return null;

            MosaicActivityBackupData data = new MosaicActivityBackupData();
            data.mosaicBackupData = mosaicBackupData;
            data.playTime = from.getLong(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__PLAY_TIME, 0L);
            data.mosaicOffset = new SizeDouble(from.getFloat(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__MOSAIC_OFFSET_WIDTH, 0),
                                               from.getFloat(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__MOSAIC_OFFSET_HEIGHT, 0));
            return data;
        } catch(Exception ex) {
            Logger.error("Can`t load MosaicActivityBackupData from SharedPreferences", ex);
        }
        return null;
    }

    public void save(MosaicActivityBackupData data, SharedPreferences.Editor editor) {
        editor.putInt(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__VERSION, VERSION);
        new MosaicBackupDataSerializer().save(data.mosaicBackupData, editor);
        editor.putLong (KEY__MOSAIC_ACTIVITY_BACKUP_DATA__PLAY_TIME, data.playTime);
        editor.putFloat(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__MOSAIC_OFFSET_WIDTH, (float)data.mosaicOffset.width);
        editor.putFloat(KEY__MOSAIC_ACTIVITY_BACKUP_DATA__MOSAIC_OFFSET_HEIGHT, (float)data.mosaicOffset.height);
    }

}
