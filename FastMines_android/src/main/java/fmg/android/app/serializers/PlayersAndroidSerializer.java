package fmg.android.app.serializers;

import android.content.Context;

import java.io.File;

import fmg.android.app.FastMinesApp;
import fmg.core.app.AProjSettings;
import fmg.core.app.serializers.PlayersSerializer;

/** Players (de)serializer */
public class PlayersAndroidSerializer extends PlayersSerializer {

    @Override
    protected File getStatisticsFile() {
        Context context = FastMinesApp.get().getAppContext();
        return new File(context.getFilesDir(), AProjSettings.getStatisticsFileName());
    }

}
