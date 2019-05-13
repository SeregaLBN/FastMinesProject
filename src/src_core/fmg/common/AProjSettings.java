package fmg.common;

public abstract class AProjSettings {

    protected AProjSettings() { }

    private static boolean IsDebug;
    public static boolean isDebug() { return IsDebug; }
    protected static void setDebug(boolean isDebug) { AProjSettings.IsDebug = isDebug; }

    static {
        try {
            // is very vendor specific
            boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

            // this is highly system depending
            boolean debugMode = System.getProperty("java.vm.info", "").contains("sharing");

            AProjSettings.IsDebug = isDebug || debugMode;
            if (AProjSettings.IsDebug)
                LoggerSimple.DEFAULT_WRITER = System.out::println;

        } catch(Error ex) {
            // android: java.lang.NoClassDefFoundError: Failed resolution of: Ljava/lang/management/ManagementFactory;
            if (!(ex instanceof NoClassDefFoundError) || !ex.getMessage().contains("ManagementFactory"))
                System.err.println(ex);
        }
    }

}
