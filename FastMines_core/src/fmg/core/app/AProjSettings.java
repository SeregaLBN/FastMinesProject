package fmg.core.app;

public abstract class AProjSettings {

    protected AProjSettings() { }

    public static final String PROJECT_NAME = "FastMines";
    public static final String CORE_VERSION = "2.2.2";

    protected static String  settingsFile = PROJECT_NAME + ".settings";
    protected static String   playersFile = PROJECT_NAME + ".players";
    protected static String championsFile = PROJECT_NAME + ".best";


    private static boolean isDebugEnabled;
    public static boolean isDebug() { return isDebugEnabled; }
    protected static void setDebug(boolean isDebug) { isDebugEnabled = isDebug; }

    public static String getSettingsFileName() {
        return settingsFile;
    }

    public static String getPlayersFileName() {
        return playersFile;
    }

    public static String getChampionsFileName() {
        return championsFile;
    }

}
