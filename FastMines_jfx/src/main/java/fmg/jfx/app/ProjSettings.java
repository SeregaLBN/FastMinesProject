package fmg.jfx.app;

import fmg.common.ui.UiInvoker;
import fmg.core.app.AProjSettings;
import fmg.core.types.draw.PenBorder2;
import fmg.jfx.img.Animator;
import fmg.jfx.utils.Cast;
import fmg.jfx.utils.Timer;

public final class ProjSettings extends AProjSettings {

    private ProjSettings() {}

    static {
        UiInvoker.Deferred = javafx.application.Platform::runLater;
        UiInvoker.Animator = Animator::getSingleton;
        UiInvoker.TimeCreator = Timer::new;

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

        PenBorder2.DefaultWidth = Cast.dpToPx(2.25);
    }

    public static void init() {
        // implicit call static block
    }

}
