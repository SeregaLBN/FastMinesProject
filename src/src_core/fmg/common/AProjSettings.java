package fmg.common;

import java.lang.management.ManagementFactory;

public abstract class AProjSettings {

    protected AProjSettings() { }

    private static boolean IsDebug;
    public static boolean isDebug() { return IsDebug; }
    protected static void setDebug(boolean isDebug) { AProjSettings.IsDebug = isDebug; }

    static {
        // is very vendor specific
        boolean isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

        // this is highly system depending
        boolean debugMode = System.getProperty("java.vm.info", "").contains("sharing");

        AProjSettings.IsDebug = isDebug || debugMode;
        if (AProjSettings.IsDebug)
            LoggerSimple.DEFAULT_WRITER = System.out::println;
    }

}
