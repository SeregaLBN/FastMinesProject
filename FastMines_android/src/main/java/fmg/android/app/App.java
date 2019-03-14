package fmg.android.app;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;

import fmg.android.app.model.MosaicInitDataExt;
import fmg.android.utils.StaticInitializer;
import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicInitData;

/** FastMines application */
public class App extends Application implements LifecycleObserver {

    public static final String MosaicPreferenceFileName = "MosaicInitData";

    public MosaicInitData getInitData() { return MosaicInitDataExt.getSharedData(); }
    public void setInitData(MosaicInitData initData) { MosaicInitDataExt.getSharedData().copyFrom(initData); }


    @Override
    public void onCreate() {
        super.onCreate();
        StaticInitializer.init();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        load();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        LoggerSimple.put("App in background");
        save();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        LoggerSimple.put("App in foreground");
    }

    private void save() {
        MosaicInitDataExt.save(getSharedPreferences(), getInitData());
    }

    private void load() {
        setInitData(MosaicInitDataExt.load(getSharedPreferences()));
    }

    private SharedPreferences getSharedPreferences() {
        return this.getSharedPreferences(MosaicPreferenceFileName, Context.MODE_PRIVATE);
    }

}
