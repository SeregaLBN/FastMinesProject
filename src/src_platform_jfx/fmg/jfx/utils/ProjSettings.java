package fmg.jfx.utils;

import fmg.common.AProjSettings;
import fmg.common.ui.UiInvoker;
import fmg.jfx.img.Animator;

public final class ProjSettings extends AProjSettings {

    private ProjSettings() {}

    static {
        UiInvoker.DEFERRED = javafx.application.Platform::runLater;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;

//        try {
//            javafx.scene.layout.Region region = new javafx.scene.layout.Pane();
//            javafx.scene.layout.Background bk = region.getBackground(); // its NULL :(
//            java.util.List<javafx.scene.layout.BackgroundFill> fills = bk.getFills();
//            javafx.scene.layout.BackgroundFill fill = fills.iterator().next();
//            javafx.scene.paint.Paint p = fill.getFill();
//            Logger.info("ButtonColor = " + p);
//        } catch (Exception e) {
//            fmg.core.mosaic.MosaicDrawModel.DefaultBkColor = fmg.common.Color.Gray().brighter();
//        }
    }

    public static void init() {
        // implicit call static block
    }

}
