package fmg.android.app.serializers;

import android.content.Context;

import java.io.File;

import fmg.android.app.FastMinesApp;
import fmg.core.app.AProjSettings;
import fmg.core.app.serializers.ChampionsSerializer;

/** Champions (de)serializer */
public class ChampionsAndroidSerializer extends ChampionsSerializer {

    @Override
    protected File getChampionsFile() {
        Context context = FastMinesApp.get().getAppContext();
        return new File(context.getFilesDir(), AProjSettings.getChampionsFileName());
    }

}
