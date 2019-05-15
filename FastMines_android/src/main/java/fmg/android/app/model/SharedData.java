package fmg.android.app.model;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import fmg.android.app.presentation.MenuSettings;
import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;

public final class SharedData {
    private SharedData() {}

    private static final MosaicInitData mosaicInitData = new MosaicInitData();
    private static final   MenuSettings menuSettings   = new MenuSettings();

    /** Singleton */
    public static MosaicInitData getMosaicInitData() { return mosaicInitData; }
    /** Singleton */
    public static MenuSettings getMenuSettings() { return menuSettings; }

    private static final String KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M = "MosaicInitData.sizeField.M";
    private static final String KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N = "MosaicInitData.sizeField.N";
    private static final String KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE  = "MosaicInitData.mosaicType";
    private static final String KEY__MOSAIC_INIT_DATA__MINES_COUNT  = "MosaicInitData.minesCount";
    private static final String KEY__MENU_SETTINGS__SPLIT_PANE_OPEN = "MenuSettings.splitPaneOpen";

    public static MosaicInitData loadMosaicInitData(Bundle from) {
        MosaicInitData newData = new MosaicInitData();
        if (from != null) try {
            newData.setSizeField(new Matrisize(       from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M),
                                                      from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N)));
            newData.setMosaicType(EMosaic.fromOrdinal(from.getInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE)));
            newData.setMinesCount(                    from.getInt(KEY__MOSAIC_INIT_DATA__MINES_COUNT));
        } catch(Exception ex) {
            newData = new MosaicInitData(); // reset
            Log.e("fmg", "Can not read mosaic init data from Bundle", ex);
        }
        return newData;
    }

    public static void save(Bundle to, MosaicInitData initData) {
        to.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, initData.getSizeField().m);
        to.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, initData.getSizeField().n);
        to.putInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE, initData.getMosaicType().ordinal());
        to.putInt(KEY__MOSAIC_INIT_DATA__MINES_COUNT, initData.getMinesCount());
    }


    public static MosaicInitData loadMosaicInitData(SharedPreferences from) {
        MosaicInitData newData = new MosaicInitData();
        if (from != null) try {
            newData.setSizeField(new Matrisize(       from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, MosaicInitData.DEFAULT_SIZE_FIELD_M),
                                                      from.getInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, MosaicInitData.DEFAULT_SIZE_FIELD_M)));
            newData.setMosaicType(EMosaic.fromOrdinal(from.getInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE, MosaicInitData.DEFAULT_MOSAIC_TYPE.ordinal())));
            newData.setMinesCount(                    from.getInt(KEY__MOSAIC_INIT_DATA__MINES_COUNT, MosaicInitData.DEFAULT_MINES_COUNT));
        } catch(Exception ex) {
            newData = new MosaicInitData(); // reset
            Log.e("fmg", "Can not read mosaic init data from SharedPreferences", ex);
        }
        return newData;
    }

    public static void save(SharedPreferences to, MosaicInitData initData) {
        SharedPreferences.Editor editor = to.edit();
        editor.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_M, initData.getSizeField().m);
        editor.putInt(KEY__MOSAIC_INIT_DATA__SIZE_FIELD_N, initData.getSizeField().n);
        editor.putInt(KEY__MOSAIC_INIT_DATA__MOSAIC_TYPE, initData.getMosaicType().ordinal());
        editor.putInt(KEY__MOSAIC_INIT_DATA__MINES_COUNT, initData.getMinesCount());
        editor.commit();
    }




    public static MenuSettings loadMenuSettings(Bundle from) {
        MenuSettings newData = new MenuSettings();
        if (from != null) try {
            newData.setSplitPaneOpen(from.getBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN));
        } catch(Exception ex) {
            newData = new MenuSettings(); // reset
            Log.e("fmg", "Can not read menu settings from Bundle", ex);
        }
        return newData;
    }

    public static void save(Bundle to, MenuSettings menuSettings) {
        to.putBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, menuSettings.isSplitPaneOpen());
    }


    public static MenuSettings loadMenuSettings(SharedPreferences from) {
        MenuSettings newData = new MenuSettings();
        if (from != null) try {
            newData.setSplitPaneOpen(from.getBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, MenuSettings.DEFAULT_SPLIT_PANE_OPEN));
        } catch(Exception ex) {
            newData = new MenuSettings(); // reset
            Log.e("fmg", "Can not read menu settings from SharedPreferences", ex);
        }
        return newData;
    }

    public static void save(SharedPreferences to, MenuSettings menuSettings) {
        SharedPreferences.Editor editor = to.edit();
        editor.putBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, menuSettings.isSplitPaneOpen());
        editor.commit();
    }

}
