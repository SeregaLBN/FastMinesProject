package fmg.android.app.serializers;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.core.app.model.MosaicBackupData;
import fmg.core.app.model.MosaicInitData;
import fmg.core.app.serializers.ISerializer;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EMosaic;
import fmg.core.types.EOpen;
import fmg.core.types.EState;

/** Mosaic cells data (de)serializer. For save/restore {@link MosaicBackupData} */
public class MosaicBackupDataSerializer implements ISerializer {

    private static final int VERSION = 1;

    private static final String KEY__MOSAIC_BACKUP_DATA = MosaicBackupData.class.getSimpleName();
    private static final String KEY__MOSAIC_BACKUP_DATA__VERSION             = KEY__MOSAIC_BACKUP_DATA + ".version";
    private static final String KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_M        = KEY__MOSAIC_BACKUP_DATA + ".sizeField.M";
    private static final String KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_N        = KEY__MOSAIC_BACKUP_DATA + ".sizeField.N";
    private static final String KEY__MOSAIC_BACKUP_DATA__MOSAIC_TYPE         = KEY__MOSAIC_BACKUP_DATA + ".mosaicType";
    private static final String KEY__MOSAIC_BACKUP_DATA__CELL_STATES__STATUS = KEY__MOSAIC_BACKUP_DATA + ".cellStates.status";
    private static final String KEY__MOSAIC_BACKUP_DATA__CELL_STATES__OPEN   = KEY__MOSAIC_BACKUP_DATA + ".cellStates.open";
    private static final String KEY__MOSAIC_BACKUP_DATA__CELL_STATES__CLOSE  = KEY__MOSAIC_BACKUP_DATA + ".cellStates.close";
    private static final String KEY__MOSAIC_BACKUP_DATA__CELL_STATES__DOWN   = KEY__MOSAIC_BACKUP_DATA + ".cellStates.down";
    private static final String KEY__MOSAIC_BACKUP_DATA__AREA                = KEY__MOSAIC_BACKUP_DATA + ".area";
    private static final String KEY__MOSAIC_BACKUP_DATA__CLICK_COUNT         = KEY__MOSAIC_BACKUP_DATA + ".clickCount";

    MosaicBackupData load(SharedPreferences from) {
        if (from != null) try {
            MosaicBackupData data = new MosaicBackupData();
            int version = from.getInt(KEY__MOSAIC_BACKUP_DATA__VERSION, 0);
            if (version == 0)
                return null;
            if (version != 1)
                throw new IllegalArgumentException("MosaicBackupDataSerializer: Version #" + version + " is not supported");

            data.mosaicType = EMosaic.fromOrdinal(from.getInt  (KEY__MOSAIC_BACKUP_DATA__MOSAIC_TYPE , MosaicInitData.DEFAULT_MOSAIC_TYPE.ordinal()));
            data.sizeField = new Matrisize(       from.getInt  (KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_M, MosaicInitData.DEFAULT_SIZE_FIELD_M),
                                                  from.getInt  (KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_N, MosaicInitData.DEFAULT_SIZE_FIELD_M));
            data.area =                           from.getFloat(KEY__MOSAIC_BACKUP_DATA__AREA        , (float)MosaicInitData.AREA_MINIMUM);
            data.clickCount =                     from.getInt  (KEY__MOSAIC_BACKUP_DATA__CLICK_COUNT , 0);

            List<EState> states = Stream.of(from.getString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__STATUS, "").split(",")).map(EState ::valueOf).collect(Collectors.toList());
            List<EOpen > opens  = Stream.of(from.getString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__OPEN  , "").split(",")).map(EOpen  ::valueOf).collect(Collectors.toList());
            List<EClose> closes = Stream.of(from.getString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__CLOSE , "").split(",")).map(EClose ::valueOf).collect(Collectors.toList());
            List<Boolean> downs = Stream.of(from.getString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__DOWN  , "").split(",")).map(Boolean::valueOf).collect(Collectors.toList());

            assert (states.size() == data.sizeField.m * data.sizeField.n);
            assert (states.size() == opens.size());
            assert (states.size() == closes.size());

            data.cellStates = new ArrayList<>(states.size());
            for (int i = 0; i < states.size(); ++i) {
                BaseCell.StateCell stateCell = new BaseCell.StateCell();
                stateCell.setStatus(states.get(i));
                stateCell.setOpen(  opens .get(i));
                stateCell.setClose( closes.get(i));
                stateCell.setDown(  downs .get(i));
                data.cellStates.add(stateCell);
            }

            return data;

        } catch(Exception ex) {
            Logger.error("Can`t load mosaic backup data from SharedPreferences", ex);
        }
        return null;
    }

    void save(MosaicBackupData data, SharedPreferences.Editor editor) {
        if (data == null)
            return;
        editor.putInt(   KEY__MOSAIC_BACKUP_DATA__VERSION     , VERSION);
        editor.putInt(   KEY__MOSAIC_BACKUP_DATA__MOSAIC_TYPE , data.mosaicType.ordinal());
        editor.putInt(   KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_M, data.sizeField.m);
        editor.putInt(   KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_N, data.sizeField.n);
        editor.putString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__STATUS, data.cellStates.stream().map(c -> c.getStatus().name()).collect(Collectors.joining(",")));
        editor.putString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__OPEN  , data.cellStates.stream().map(c -> c.getOpen  ().name()).collect(Collectors.joining(",")));
        editor.putString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__CLOSE , data.cellStates.stream().map(c -> c.getClose ().name()).collect(Collectors.joining(",")));
        editor.putString(KEY__MOSAIC_BACKUP_DATA__CELL_STATES__DOWN  , data.cellStates.stream().map(c -> Boolean.toString(c.isDown())).collect(Collectors.joining(",")));
        editor.putFloat( KEY__MOSAIC_BACKUP_DATA__AREA        , (float)data.area);
        editor.putInt(   KEY__MOSAIC_BACKUP_DATA__CLICK_COUNT , data.clickCount);
    }

}