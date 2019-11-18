package fmg.swing.utils;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.AProjSettings;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.swing.img.Animator;

public final class ProjSettings extends AProjSettings {

    private ProjSettings() {}

    static {
        UiInvoker.DEFERRED = javax.swing.SwingUtilities::invokeLater;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;


        try {
            UIDefaults uiDef = UIManager.getDefaults();
            java.awt.Color clr = uiDef.getColor("Panel.background");
            if (clr != null)
                MosaicDrawModel.DefaultBkColor = Cast.toColor(clr);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void init() {
        // implicit call static block
    }

}
