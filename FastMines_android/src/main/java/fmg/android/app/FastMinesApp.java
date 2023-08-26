package fmg.android.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.beans.PropertyChangeEvent;
import java.util.UUID;

import fmg.android.app.model.AppData;
import fmg.android.app.model.MosaicActivityBackupData;
import fmg.android.app.presentation.MenuSettings;
import fmg.android.app.serializers.AppDataSerializer;
import fmg.android.app.serializers.ChampionsAndroidSerializer;
import fmg.android.app.serializers.PlayersAndroidSerializer;
import fmg.common.Logger;
import fmg.core.app.AProjSettings;
import fmg.core.app.model.Champions;
import fmg.core.app.model.MosaicInitData;
import fmg.core.app.model.Players;
import fmg.core.app.model.User;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** FastMines application */
public class FastMinesApp extends Application implements LifecycleObserver {

    private Context context;
    private MenuSettings menuSettings;
    private MosaicInitData mosaicInitData;
    private MosaicActivityBackupData mosaicActivityBackupData;
    private Players players;
    private Champions champions;
    private boolean playersChanged;
    private boolean championsChanged;
    private Activity lastActivity;

    public MosaicInitData getMosaicInitData() { return mosaicInitData; }
    public MenuSettings   getMenuSettings()   { return menuSettings; }
    public boolean hasMosaicActivityBackupData() { return  mosaicActivityBackupData != null; }
    public MosaicActivityBackupData getAndResetMosaicActivityBackupData() {
        MosaicActivityBackupData res = mosaicActivityBackupData;
        mosaicActivityBackupData = null;
        return res;
    }

    private static FastMinesApp self;

    /** get single instance of application (singleton) */
    public static FastMinesApp get(/*Activity activity*/) {
        //return (FastMinesApp)activity.getApplicationContext();
        return self;
    }

    public FastMinesApp() {
        self = this;
    }

    public Context getAppContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ProjSettings.init();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        load();
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) { }
            @Override public void onActivityStarted(@NonNull Activity activity) {
                lastActivity = activity;
            }
            @Override public void onActivityResumed(@NonNull Activity activity) { }
            @Override public void onActivityPaused(@NonNull Activity activity) { }
            @Override public void onActivityStopped(@NonNull Activity activity) { }
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) { }
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onForegrounded() {
        Logger.info("FastMinesApp::onForegrounded");

        getMenuSettings  ().setListener(this::onMenuSettingsPropertyChanged);
        getMosaicInitData().setListener(this::onMosaicInitDataPropertyChanged);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onBackgrounded() {
        Logger.info("FastMinesApp::onBackgrounded");
        save();

        getMenuSettings  ().setListener(null);
        getMosaicInitData().setListener(null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onClosed() {
        Logger.info("FastMinesApp::onClosed");
        champions.setListener(null);
        players.setListener(null);
        menuSettings.close();
        mosaicInitData.close();
    }

    private void save() {
        if (championsChanged) {
            championsChanged = false;
            new ChampionsAndroidSerializer().save(champions);
        }
        if (playersChanged) {
            playersChanged = false;
            new PlayersAndroidSerializer().save(players);
        }

        AppData data = new AppData();
        data.setSplitPaneOpen(menuSettings.isSplitPaneOpen());
        data.setMosaicInitData(mosaicInitData);
        if (lastActivity instanceof MosaicActivity) {
            MosaicActivity mosaicActivity = (MosaicActivity)lastActivity;
            data.setMosaicActivityBackupData(mosaicActivity.getBackupData());
        }

        SharedPreferences preferences = getAppPreferences();
        new AppDataSerializer().save(data, preferences);
    }

    private void load() {
        SharedPreferences preferences = getAppPreferences();
        AppData data = new AppDataSerializer().load(preferences);

        menuSettings = new MenuSettings();
        menuSettings.setSplitPaneOpen(data.isSplitPaneOpen());
        mosaicInitData = data.getMosaicInitData();
        mosaicActivityBackupData = data.getMosaicActivityBackupData();

        players = new PlayersAndroidSerializer().load();
        players.setListener(this::onPlayersPropertyChanged);
        if (players.getRecords().isEmpty())
            // create default user for android
            players.addNewPlayer("You", null);

        champions = new ChampionsAndroidSerializer().load();
        //champions.subscribeTo(players);
        champions.setListener(this::onChampionsPropertyChanged);
    }

    private SharedPreferences getAppPreferences() {
        return getSharedPreferences(AProjSettings.getSettingsFileName(), Context.MODE_PRIVATE);
    }

    private void onMenuSettingsPropertyChanged(String propertyName) {
        Logger.info("FastMinesApp::onMenuSettingsPropertyChanged: propertyName={0}", propertyName);
    }

    private void onMosaicInitDataPropertyChanged(String propertyName) {
        Logger.info("FastMinesApp::onMosaicInitDataPropertyChanged: propertyName={0}", propertyName);
    }

    /** Сохранить чемпиона && Установить статистику */
    public int updateStatistic(EMosaic mosaic, ESkillLevel skill, boolean victory, long countOpenField, long playTime, int clickCount) {
        // логика сохранения...
        UUID userId = players.getRecords()
                             .get(0) // first user - default user
                             .user
                             .getId();
        // ...статистики
        players.updateStatistic(userId, mosaic, skill, victory, countOpenField, playTime, clickCount);

        // ...чемпиона
        if (victory) {
            User user = players.getUser(userId);
            return champions.add(user, playTime, mosaic, skill, clickCount);
        }

        return -1;
    }

    private void onPlayersPropertyChanged(PropertyChangeEvent ev) {
        playersChanged = true;
    }

    private void onChampionsPropertyChanged(PropertyChangeEvent ev) {
        championsChanged = true;
    }

}
