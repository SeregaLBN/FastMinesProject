package fmg.android.app;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.android.app.presentation.MenuSettings;
import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicInitData;
import fmg.android.app.model.SharedData;
import fmg.android.utils.ProjSettings;

/** FastMines application */
public class App extends Application implements LifecycleObserver {

    public static final String  MosaicPreferenceFileName = "MosaicInitData";
    public static final String AppMenuPreferenceFileName = "AppMenuData";

    private final PropertyChangeListener   onMenuSettingsPropertyChangedListener = this::onMenuSettingsPropertyChanged;
    private final PropertyChangeListener onMosaicInitDataPropertyChangedListener = this::onMosaicInitDataPropertyChanged;

    private MosaicInitData getMosaicInitData() { return SharedData.getMosaicInitData(); }
    private MenuSettings   getMenuSettings()   { return SharedData.getMenuSettings(); }

    @Override
    public void onCreate() {
        super.onCreate();
        ProjSettings.init();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        load();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        LoggerSimple.put("App in foreground");

        getMenuSettings  ().addListener(onMenuSettingsPropertyChangedListener);
        getMosaicInitData().addListener(onMosaicInitDataPropertyChangedListener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        LoggerSimple.put("App in background");
        save();

        getMenuSettings  ().removeListener(onMenuSettingsPropertyChangedListener);
        getMosaicInitData().removeListener(onMosaicInitDataPropertyChangedListener);
    }

    private void save() {
        SharedData.saveMenuSettings(  getSharedMenuSettingsPreferences()  );
        SharedData.saveMosaicInitData(getSharedMosaicInitDataPreferences());
    }

    private void load() {
        SharedData.loadMenuSettings(  getSharedMenuSettingsPreferences());
        SharedData.loadMosaicInitData(getSharedMosaicInitDataPreferences());
    }

    private SharedPreferences getSharedMenuSettingsPreferences() {
        return this.getSharedPreferences(AppMenuPreferenceFileName, Context.MODE_PRIVATE);
    }

    private SharedPreferences getSharedMosaicInitDataPreferences() {
        return this.getSharedPreferences(MosaicPreferenceFileName, Context.MODE_PRIVATE);
    }

    private void onMenuSettingsPropertyChanged(PropertyChangeEvent ev) {
        LoggerSimple.put("  FastMinesApp::onMenuSettingsPropertyChanged: ev={0}", ev);
    }

    private void onMosaicInitDataPropertyChanged(PropertyChangeEvent ev) {
        LoggerSimple.put("  FastMinesApp::onMosaicInitDataPropertyChanged: ev={0}", ev);
    }

}
