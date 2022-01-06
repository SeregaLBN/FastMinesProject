package fmg.android.app.serializers;

import android.content.SharedPreferences;

import fmg.common.Logger;
import fmg.core.app.serializers.ISerializer;
import fmg.android.app.model.AppData;
import fmg.android.app.presentation.MenuSettings;

/** Application data (de)serializer. For save / restore {@link AppData} */
public class AppDataSerializer implements ISerializer {

    private static final String KEY__MENU_SETTINGS__SPLIT_PANE_OPEN = MenuSettings.class.getSimpleName() + ".splitPaneOpen";

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
            data.setMosaicInitData(new MosaicInitDataSerializer().load(from));
            data.setSplitPaneOpen(from.getBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, MenuSettings.DEFAULT_SPLIT_PANE_OPEN));
        } catch(Exception ex) {
            Logger.error("Can not read app settings from SharedPreferences", ex);
            data = new AppData(); // reset
        }
        return data;
    }

    public void save(AppData data, SharedPreferences to) {
        SharedPreferences.Editor editor = to.edit();
        new MosaicInitDataSerializer().save(data.getMosaicInitData(), editor);
        editor.putBoolean(KEY__MENU_SETTINGS__SPLIT_PANE_OPEN, data.isSplitPaneOpen());
        editor.apply();
    }

}
