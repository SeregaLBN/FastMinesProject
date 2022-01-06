package fmg.core.app;

public abstract class AProjSettings {

    protected AProjSettings() { }

    public static final String PROJECT_NAME = "FastMines";
    public static final String CORE_VERSION = "2.2.2";

    protected static String  settingsFile = PROJECT_NAME + ".settings";
    protected static String   playersFile = PROJECT_NAME + ".players";
    protected static String championsFile = PROJECT_NAME + ".best";

    private static boolean isReleaseModeEnabled = true;
    private static boolean isDebugOutputEnabled;

    public static boolean isDebugOutput() {
        return isDebugOutputEnabled;
    }

    protected static void setDebugOutput(boolean debugOutput) {
        AProjSettings.isDebugOutputEnabled = debugOutput;
    }

    public static boolean isReleaseMode() {
        return isReleaseModeEnabled;
    }
    protected static void setReleaseMode(boolean isReleaseMode) {
        AProjSettings.isReleaseModeEnabled = isReleaseMode;
    }

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
