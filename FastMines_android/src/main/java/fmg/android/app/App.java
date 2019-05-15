package fmg.android.app;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;

import java.beans.PropertyChangeEvent;

import fmg.android.app.presentation.MenuSettings;
import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicInitData;
import fmg.android.app.model.SharedData;
import fmg.android.utils.ProjSettings;

/** FastMines application */
public class App extends Application implements LifecycleObserver {

    public static final String  MosaicPreferenceFileName = "MosaicInitData";
    public static final String AppMenuPreferenceFileName = "AppMenuData";

    public MosaicInitData getMosaicInitData() { return SharedData.getMosaicInitData(); }
    public void setMosaicInitData(MosaicInitData initData) { SharedData.getMosaicInitData().copyFrom(initData); }

    public MenuSettings getMenuSettings() { return SharedData.getMenuSettings(); }
    public void setMenuSettings(MenuSettings menuSettings) { SharedData.getMenuSettings().copyFrom(menuSettings); }

    @Override
    public void onCreate() {
        super.onCreate();
        ProjSettings.init();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        load();

        getMenuSettings().addListener(  this::onMenuSettingsPropertyChanged);
        getMosaicInitData().addListener(this::onMosaicInitDataPropertyChanged);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        LoggerSimple.put("App in foreground");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        LoggerSimple.put("App in background");
        save();
    }

    private void save() {
        SharedData.save(getSharedMenuSettingsPreferences()  , getMenuSettings());
        SharedData.save(getSharedMosaicInitDataPreferences(), getMosaicInitData());
    }

    private void load() {
        setMenuSettings(  SharedData.loadMenuSettings(  getSharedMenuSettingsPreferences()));
        setMosaicInitData(SharedData.loadMosaicInitData(getSharedMosaicInitDataPreferences()));
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
