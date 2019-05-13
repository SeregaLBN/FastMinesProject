package fmg.swing.utils;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.swing.img.Animator;

public final class StaticInitializer {

    private StaticInitializer() {}

    static {
        UiInvoker.DEFERRED = javax.swing.SwingUtilities::invokeLater;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;


        UIDefaults uiDef = UIManager.getDefaults();
        java.awt.Color clr = uiDef.getColor("Panel.background");
        if (clr != null)
            MosaicDrawModel.DefaultBkColor = Cast.toColor(clr);
    }

    public static void init() {
        // implicit call static block
    }

}
