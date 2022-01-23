package fmg.android.app.serializers;

import android.content.SharedPreferences;

import fmg.android.app.model.MosaicActivityBackupData;
import fmg.common.Logger;
import fmg.core.app.serializers.ISerializer;
import fmg.android.app.model.AppData;
import fmg.android.app.presentation.MenuSettings;

/** Application data (de)serializer. For save / restore {@link AppData} */
public class AppDataSerializer implements ISerializer {

    private static final int VERSION = 1;

    private static final String KEY__APP_DATA                                 = "AppData";
    private static final String KEY__APP_DATA__VERSION                        = KEY__APP_DATA + ".version";
    private static final String KEY__APP_DATA__MENU_SETTINGS__SPLIT_PANE_OPEN = MenuSettings.class.getSimpleName() + ".splitPaneOpen";

    /*
    public AppData load(Bundle from) {
        AppData data = new AppData();
        if (from != null) try {
            MosaicInitData mid = new MosaicInitData();
            new MosaicInitDataSerializer().load(from, mid);
            data.setMosaicInitData(mid);
            data.setSplitPaneOpen(from.getBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, true));
        } catch(Exception ex) {
            Logger.error("Can not read app settings from Bundle", ex);
            data = new AppData(); // reset
        }
        return data;
    }

    public void save(AppData data, Bundle to) {
        new MosaicInitDataSerializer().save(data.getMosaicInitData(), to);
        to.putBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, data.isSplitPaneOpen());
    }
    */

    public AppData load(SharedPreferences from) {
        AppData data = new AppData();
        if (from != null) try {
            int version = from.getInt(KEY__APP_DATA__VERSION, 1);
            if (version != VERSION)
                throw new IllegalArgumentException("AppDataSerializer: Version #" + version + " is not supported");

            MosaicActivityBackupData mosaicActivityBackupData = new MosaicActivityBackupDataSerializer().load(from);
            if (mosaicActivityBackupData == null)
                data.setMosaicInitData(new MosaicInitDataSerializer().load(from));
            else
                data.setMosaicActivityBackupData(mosaicActivityBackupData);

            data.setSplitPaneOpen(from.getBoolean(KEY__APP_DATA__MENU_SETTINGS__SPLIT_PANE_OPEN, MenuSettings.DEFAULT_SPLIT_PANE_OPEN));
        } catch(Exception ex) {
            Logger.error("Can not read app settings from SharedPreferences", ex);
            data = new AppData(); // reset
        }
        return data;
    }

    public void save(AppData data, SharedPreferences to) {
        SharedPreferences.Editor editor = to.edit();
        editor.putInt(KEY__APP_DATA__VERSION, VERSION);

        MosaicActivityBackupData mosaicActivityBackupData = data.getMosaicActivityBackupData();
        if (mosaicActivityBackupData == null)
            new MosaicInitDataSerializer().save(data.getMosaicInitData(), editor);
        else
            new MosaicActivityBackupDataSerializer().save(mosaicActivityBackupData, editor);

        editor.putBoolean(KEY__APP_DATA__MENU_SETTINGS__SPLIT_PANE_OPEN, data.isSplitPaneOpen());
        editor.apply();
    }

}
