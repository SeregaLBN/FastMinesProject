package fmg.core.app;

public abstract class AProjSettings {

    protected AProjSettings() { }

    public static final String PROJECT_NAME = "FastMines";

    protected static String   settingsFile = PROJECT_NAME + ".settings";
    protected static String statisticsFile = PROJECT_NAME + ".statistics";
    protected static String  championsFile = PROJECT_NAME + ".best";


    private static boolean IsDebug;
    public static boolean isDebug() { return IsDebug; }
    protected static void setDebug(boolean isDebug) { AProjSettings.IsDebug = isDebug; }

    static {
        /** /
        try {
            // is very vendor specific
            boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

            // this is highly system depending
            boolean debugMode = System.getProperty("java.vm.info", "").contains("sharing");

            AProjSettings.IsDebug = isDebug || debugMode;
            if (AProjSettings.IsDebug)
                fmg.common.Logger.DEBUG_WRITER = System.out::println;

        } catch(Error ex) {
            // android: java.lang.NoClassDefFoundError: Failed resolution of: Ljava/lang/management/ManagementFactory;
            if (!(ex instanceof NoClassDefFoundError) || !ex.getMessage().contains("ManagementFactory"))
                fmg.common.Logger.error("AProjSettings", ex);
        }
        /**/
    }

    public static String getSettingsFileName() {
        return settingsFile;
    }

    public static String getStatisticsFileName() {
        return statisticsFile;
    }

    public static String getChampionsFileName() {
        return championsFile;
    }

}
