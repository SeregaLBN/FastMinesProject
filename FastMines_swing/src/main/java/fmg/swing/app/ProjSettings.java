package fmg.swing.app;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Logger;
import fmg.common.ui.UiInvoker;
import fmg.core.app.AProjSettings;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.swing.img.Animator;
import fmg.swing.utils.Cast;
import fmg.swing.utils.Timer;

public final class ProjSettings extends AProjSettings {

    private ProjSettings() {}

    static {
        UiInvoker.DEFERRED = javax.swing.SwingUtilities::invokeLater;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;


        try {
            UIDefaults uiDef = UIManager.getDefaults();
            java.awt.Color clr = uiDef.getColor("TabbedPane.highlight"); // TabbedPane.highlight    Button.background    Label.background    Panel.background
            if (clr != null)
                MosaicDrawModel.DefaultBkColor = Cast.toColor(clr);
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
