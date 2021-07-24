package fmg.android.app;

import android.app.Activity;
import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.android.app.model.AppData;
import fmg.android.app.serializers.AppDataSerializer;
import fmg.common.Logger;
import fmg.core.app.AProjSettings;
import fmg.core.types.model.MosaicInitData;
import fmg.android.app.presentation.MenuSettings;

/** FastMines application */
public class FastMinesApp extends Application implements LifecycleObserver {

    private final PropertyChangeListener   onMenuSettingsPropertyChangedListener = this::onMenuSettingsPropertyChanged;
    private final PropertyChangeListener onMosaicInitDataPropertyChangedListener = this::onMosaicInitDataPropertyChanged;

    private MenuSettings menuSettings;
    private MosaicInitData mosaicInitData;

    public MosaicInitData getMosaicInitData() { return mosaicInitData; }
    public MenuSettings   getMenuSettings()   { return menuSettings; }

    private static FastMinesApp self;

    public static FastMinesApp get(/*Activity activity*/) {
        //return (FastMinesApp)activity.getApplicationContext();
        return self;
    }

    public FastMinesApp() {
        self = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        ProjSettings.init();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        load();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onForegrounded() {
        Logger.info("FastMinesApp::onForegrounded");

        getMenuSettings  ().addListener(onMenuSettingsPropertyChangedListener);
        getMosaicInitData().addListener(onMosaicInitDataPropertyChangedListener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onBackgrounded() {
        Logger.info("FastMinesApp::onBackgrounded");
        save();

        getMenuSettings  ().removeListener(onMenuSettingsPropertyChangedListener);
        getMosaicInitData().removeListener(onMosaicInitDataPropertyChangedListener);
    }

    private void save() {
        AppData data = new AppData();
        data.setSplitPaneOpen(menuSettings.isSplitPaneOpen());
        data.setMosaicInitData(mosaicInitData);

        SharedPreferences preferences = getAppPreferences();
        new AppDataSerializer().save(data, preferences);
    }

    private void load() {
        SharedPreferences preferences = getAppPreferences();
        AppData data = new AppDataSerializer().load(preferences);

        menuSettings = new MenuSettings();
        menuSettings.setSplitPaneOpen(data.isSplitPaneOpen());
        mosaicInitData = data.getMosaicInitData();
    }

    private SharedPreferences getAppPreferences() {
        return getSharedPreferences(AProjSettings.getSettingsFileName(), Context.MODE_PRIVATE);
    }

    private void onMenuSettingsPropertyChanged(PropertyChangeEvent ev) {
        Logger.info("FastMinesApp::onMenuSettingsPropertyChanged: ev={0}", ev);
    }

    private void onMosaicInitDataPropertyChanged(PropertyChangeEvent ev) {
        Logger.info("FastMinesApp::onMosaicInitDataPropertyChanged: ev={0}", ev);
    }

}
