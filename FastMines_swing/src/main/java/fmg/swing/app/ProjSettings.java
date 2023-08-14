package fmg.swing.app;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Logger;
import fmg.common.ui.UiInvoker;
import fmg.core.app.AProjSettings;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.types.draw.PenBorder2;
import fmg.swing.img.Animator;
import fmg.swing.utils.Cast;
import fmg.swing.utils.Timer;

public final class ProjSettings extends AProjSettings {

    private ProjSettings() {}

    static {
        boolean isDebug = false;
        try {
            // this is highly system depending
            isDebug = System.getProperty("java.vm.info", "").contains("sharing");

            if (!isDebug)
                // is very vendor specific
                isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

        } catch(Error ex) {
            Logger.error("AProjSettings", ex);

        } finally {
            setDebugOutput(isDebug);
            if (isDebug)
                fmg.common.Logger.DEBUG_WRITER = System.out::println;
        }


        UiInvoker.Deferred = javax.swing.SwingUtilities::invokeLater;
        UiInvoker.Animator = Animator::get;
        UiInvoker.TimeCreator = Timer::new;

        PenBorder2.DefaultWidth = Cast.dpToPx(2.25);


        try {
            UIDefaults uiDef = UIManager.getDefaults();
            java.awt.Color clr = uiDef.getColor("Panel.background"); // TabbedPane.highlight    Button.background    Label.background    Panel.background
            if (clr != null)
                MosaicModel2.DefaultCellColor = Cast.toColor(clr);

            clr = uiDef.getColor("Button.background");
            if (clr != null)
                MosaicModel2.DefaultBkColor = Cast.toColor(clr);
        } catch (Exception ex) {
            Logger.error("ProjSettings", ex);
        }

        String prefix = System.getProperty("user.dir") + System.getProperty("file.separator");
        settingsFile  = prefix +  settingsFile;
        playersFile   = prefix +   playersFile;
        championsFile = prefix + championsFile;
    }

    public static void init() {
        // implicit call static block
    }

}
