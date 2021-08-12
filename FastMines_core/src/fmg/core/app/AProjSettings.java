package fmg.core.app;

public abstract class AProjSettings {

    protected AProjSettings() { }

    public static final String PROJECT_NAME = "FastMines";
    public static final String CORE_VERSION = "2.2.2";

    protected static String  settingsFile = PROJECT_NAME + ".settings";
    protected static String   playersFile = PROJECT_NAME + ".players";
    protected static String championsFile = PROJECT_NAME + ".best";


    private static boolean IsDebug;
    public static boolean isDebug() { return IsDebug; }
    protected static void setDebug(boolean isDebug) { AProjSettings.IsDebug = isDebug; }

    static {
        /**/
        boolean isDebug = false;
        try {
            // this is highly system depending
            isDebug = System.getProperty("java.vm.info", "").contains("sharing");

            if (!isDebug)
                // is very vendor specific
                isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

        } catch(Error ex) {
            // android: java.lang.NoClassDefFoundError: Failed resolution of: Ljava/lang/management/ManagementFactory;
            if (!(ex instanceof NoClassDefFoundError) || !ex.getMessage().contains("ManagementFactory"))
                fmg.common.Logger.error("AProjSettings", ex);
        } finally {
            AProjSettings.IsDebug = isDebug;
            if (AProjSettings.IsDebug)
                fmg.common.Logger.DEBUG_WRITER = System.out::println;

        }
        /**/
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
