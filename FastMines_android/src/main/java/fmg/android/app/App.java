package fmg.android.app;

import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.Logger;
import fmg.core.types.model.MosaicInitData;
import fmg.android.app.model.SharedData;
import fmg.android.app.presentation.MenuSettings;

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
        Logger.info("FastMinesApp::onMenuSettingsPropertyChanged: ev={0}", ev);
    }

    private void onMosaicInitDataPropertyChanged(PropertyChangeEvent ev) {
        Logger.info("FastMinesApp::onMosaicInitDataPropertyChanged: ev={0}", ev);
    }

}
