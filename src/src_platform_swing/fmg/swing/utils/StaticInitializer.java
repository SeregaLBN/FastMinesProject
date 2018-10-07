package fmg.swing.utils;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.ui.Factory;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.swing.img.Animator;

public final class StaticInitializer {

    static {
        Factory.DEFERR_INVOKER = javax.swing.SwingUtilities::invokeLater;
        Factory.GET_ANIMATOR = Animator::getSingleton;
        Factory.TIMER_CREATOR = Timer::new;


        UIDefaults uiDef = UIManager.getDefaults();
        java.awt.Color clr = uiDef.getColor("Panel.background");
        if (clr != null)
            MosaicDrawModel.DefaultBkColor = Cast.toColor(clr);
    }

    public static void init() {
        // implicit call static block
    }

}
