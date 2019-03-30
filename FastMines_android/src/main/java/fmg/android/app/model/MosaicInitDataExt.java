package fmg.android.app.model;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;

public final class MosaicInitDataExt {
    private MosaicInitDataExt() {}

    private static final MosaicInitData sharedInitData = new MosaicInitData();
    /** Singleton */
    public static MosaicInitData getSharedData() { return sharedInitData; }

    private static final String KEY__SIZE_FIELD_M = "MosaicInitData.sizeField.M";
    private static final String KEY__SIZE_FIELD_N = "MosaicInitData.sizeField.N";
    private static final String KEY__MOSAIC_TYPE  = "MosaicInitData.mosaicType";
    private static final String KEY__MINES_COUNT  = "MosaicInitData.minesCount";

    public static MosaicInitData load(Bundle from) {
        MosaicInitData newData = new MosaicInitData();
        if (from != null) try {
            newData.setSizeField(new Matrisize(       from.getInt(KEY__SIZE_FIELD_M),
                                                      from.getInt(KEY__SIZE_FIELD_N)));
            newData.setMosaicType(EMosaic.fromOrdinal(from.getInt(KEY__MOSAIC_TYPE )));
            newData.setMinesCount(                    from.getInt(KEY__MINES_COUNT ));
        } catch(Exception ex) {
            newData = new MosaicInitData(); // reset
            Log.e("fmg", "Can not read mosaic init data from Bundle", ex);
        }
        return newData;
    }

    public static void save(Bundle to, MosaicInitData initData) {
        to.putInt(KEY__SIZE_FIELD_M, initData.getSizeField().m);
        to.putInt(KEY__SIZE_FIELD_N, initData.getSizeField().n);
        to.putInt(KEY__MOSAIC_TYPE , initData.getMosaicType().ordinal());
        to.putInt(KEY__MINES_COUNT , initData.getMinesCount());
    }


    public static MosaicInitData load(SharedPreferences from) {
        MosaicInitData newData = new MosaicInitData();
        if (from != null) try {
            newData.setSizeField(new Matrisize(       from.getInt(KEY__SIZE_FIELD_M, MosaicInitData.DEFAULT_SIZE_FIELD_M),
                                                      from.getInt(KEY__SIZE_FIELD_N, MosaicInitData.DEFAULT_SIZE_FIELD_M)));
            newData.setMosaicType(EMosaic.fromOrdinal(from.getInt(KEY__MOSAIC_TYPE , MosaicInitData.DEFAULT_MOSAIC_TYPE.ordinal())));
            newData.setMinesCount(                    from.getInt(KEY__MINES_COUNT , MosaicInitData.DEFAULT_MINES_COUNT));
        } catch(Exception ex) {
            newData = new MosaicInitData(); // reset
            Log.e("fmg", "Can not read mosaic init data from SharedPreferences", ex);
        }
        return newData;
    }

    public static void save(SharedPreferences to, MosaicInitData initData) {
        SharedPreferences.Editor editor = to.edit();
        editor.putInt(KEY__SIZE_FIELD_M, initData.getSizeField().m);
        editor.putInt(KEY__SIZE_FIELD_N, initData.getSizeField().n);
        editor.putInt(KEY__MOSAIC_TYPE , initData.getMosaicType().ordinal());
        editor.putInt(KEY__MINES_COUNT , initData.getMinesCount());
        editor.commit();
    }

}
