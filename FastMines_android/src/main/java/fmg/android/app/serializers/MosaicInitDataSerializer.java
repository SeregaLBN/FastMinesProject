package fmg.android.app.serializers;

import android.content.SharedPreferences;

import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.core.app.serializers.ISerializer;
import fmg.core.app.model.MosaicInitData;
import fmg.core.types.EMosaic;

/** Mosaic data (de)serializer. For save/restore {@link MosaicInitData} */
public class MosaicInitDataSerializer implements ISerializer {

    private static final int VERSION = 1;

    private static final String KEY__MOSAIC_INIT_DATA = MosaicInitData.class.getSimpleName();
    private static final String KEY__MOSAIC_INIT_DATA__VERSION      = KEY__MOSAIC_INIT_DATA + ".version";
    private static final String KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M = KEY__MOSAIC_INIT_DATA + ".sizeField.M";
    private static final String KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N = KEY__MOSAIC_INIT_DATA + ".sizeField.N";
    private static final String KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE  = KEY__MOSAIC_INIT_DATA + ".mosaicType";
    private static final String KEY__MOSAIC_INIT_DATA__COUNT_MINES  = KEY__MOSAIC_INIT_DATA + ".countMines";

    /*
    boolean load(Bundle from, MosaicInitData to) {
        if (from == null)
            return false;

        try {
            int version = from.getInt(KEY__MOSAIC_INIT_DATA__VERSION, 1);
            if (version != 1)
                throw new IllegalArgumentException("Version #" + version + " is not supported");

            MosaicInitData data = new MosaicInitData();
            data.setSizeField(new Matrisize(       from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, MosaicInitData.DEFAULT_SIZE_FIELD_M),
                                                   from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, MosaicInitData.DEFAULT_SIZE_FIELD_M)));
            data.setMosaicType(EMosaic.fromOrdinal(from.getInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE , MosaicInitData.DEFAULT_MOSAIC_TYPE.ordinal())));
            data.setCountMines(                    from.getInt(KEY__MOSAIC_INIT_DATA__COUNT_MINES , MosaicInitData.DEFAULT_COUNT_MINES));

            to.copyFrom(data);

            return true;
        } catch(Exception ex) {
            Logger.error("Can not read mosaic init data from Bundle", ex);
            return false;
        }
    }

    void save(MosaicInitData data, Bundle to) {
        to.putInt(KEY__MOSAIC_INIT_DATA__VERSION     , VERSION);
        to.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, data.getSizeField().m);
        to.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, data.getSizeField().n);
        to.putInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE , data.getMosaicType().ordinal());
        to.putInt(KEY__MOSAIC_INIT_DATA__COUNT_MINES , data.getCountMines());
    }
    /**/

    MosaicInitData load(SharedPreferences from) {
        MosaicInitData data = new MosaicInitData();
        if (from != null) try {
            int version = from.getInt(KEY__MOSAIC_INIT_DATA__VERSION, 1);
            if (version != 1)
                throw new IllegalArgumentException("MosaicInitDataSerializer: Version #" + version + " is not supported");

            data.setSizeField(new Matrisize(       from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, MosaicInitData.DEFAULT_SIZE_FIELD_M),
                                                   from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, MosaicInitData.DEFAULT_SIZE_FIELD_M)));
            data.setMosaicType(EMosaic.fromOrdinal(from.getInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE , MosaicInitData.DEFAULT_MOSAIC_TYPE.ordinal())));
            data.setCountMines(                    from.getInt(KEY__MOSAIC_INIT_DATA__COUNT_MINES , MosaicInitData.DEFAULT_COUNT_MINES));
        } catch(Exception ex) {
            Logger.error("Can not read mosaic init data from SharedPreferences", ex);
            data = new MosaicInitData(); // reset
        }
        return data;
    }

    void save(MosaicInitData data, SharedPreferences.Editor editor) {
        editor.putInt(KEY__MOSAIC_INIT_DATA__VERSION     , VERSION);
        editor.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, data.getSizeField().m);
        editor.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, data.getSizeField().n);
        editor.putInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE , data.getMosaicType().ordinal());
        editor.putInt(KEY__MOSAIC_INIT_DATA__COUNT_MINES , data.getCountMines());
    }

}