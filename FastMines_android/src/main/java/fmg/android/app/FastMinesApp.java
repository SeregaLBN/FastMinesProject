package fmg.android.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.UUID;

import fmg.android.app.serializers.ChampionsAndroidSerializer;
import fmg.android.app.serializers.PlayersAndroidSerializer;
import fmg.common.Logger;
import fmg.core.app.AProjSettings;
import fmg.core.app.model.Champions;
import fmg.core.app.model.MosaicInitData;
import fmg.core.app.model.Players;
import fmg.android.app.model.AppData;
import fmg.android.app.presentation.MenuSettings;
import fmg.android.app.serializers.AppDataSerializer;
import fmg.core.app.model.User;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** FastMines application */
public class FastMinesApp extends Application implements LifecycleObserver {

    private final PropertyChangeListener   onMenuSettingsPropertyChangedListener = this::onMenuSettingsPropertyChanged;
    private final PropertyChangeListener onMosaicInitDataPropertyChangedListener = this::onMosaicInitDataPropertyChanged;


    private Context context;
    private MenuSettings menuSettings;
    private MosaicInitData mosaicInitData;
    private Players players;
    private Champions champions;
    private boolean playersChanged;
    private boolean championsChanged;
    private final PropertyChangeListener   onPlayersPropertyChangedListener = this::onPlayersPropertyChanged;
    private final PropertyChangeListener onChampionsPropertyChangedListener = this::onChampionsPropertyChanged;

    public MosaicInitData getMosaicInitData() { return mosaicInitData; }
    public MenuSettings   getMenuSettings()   { return menuSettings; }

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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onClosed() {
        Logger.info("FastMinesApp::onClosed");
        champions.removeListener(onChampionsPropertyChangedListener);
        players.removeListener(onPlayersPropertyChangedListener);
        champions.close();
        players.close();
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

        SharedPreferences preferences = getAppPreferences();
        new AppDataSerializer().save(data, preferences);
    }

    private void load() {
        SharedPreferences preferences = getAppPreferences();
        AppData data = new AppDataSerializer().load(preferences);

        menuSettings = new MenuSettings();
        menuSettings.setSplitPaneOpen(data.isSplitPaneOpen());
        mosaicInitData = data.getMosaicInitData();

        players = new PlayersAndroidSerializer().load();
        players.addListener(onPlayersPropertyChangedListener);
        if (players.getRecords().isEmpty())
            // create default user for android
            players.addNewPlayer("You", null);

        champions = new ChampionsAndroidSerializer().load();
        //champions.subscribeTo(players);
        champions.addListener(onChampionsPropertyChangedListener);
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
