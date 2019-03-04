package fmg.android.app.presentation;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

import fmg.android.utils.AsyncRunner;
import fmg.common.Color;
import fmg.common.geom.util.FigureHelper;
import fmg.core.img.AnimatedImageModel;

public final class SmoothHelper {
    private SmoothHelper() {}

    private static class SmoothContext {
        int dir = 0; // direction smooth transition (0 - exit; 1 - forward; -1 - reverse)
        Color clrStart;
        double currentStepAngle = 0;
    }

    private static Map<View, SmoothContext> mapViewSmooth = new HashMap<>();

    public static void runColorSmoothTransition(View view, AnimatedImageModel model) {
        if (!mapViewSmooth.containsKey(view)) {
            SmoothContext ctx = new SmoothContext();
            ctx.clrStart = model.getBackgroundColor(); //Cast.toColor(((ColorDrawable)view.getBackground()).getColor());//Color.Coral;
            mapViewSmooth.put(view, ctx);
        }
        final long fullTimeMSec = 150, repeatTimeMSec = 10;
        final double deltaStepAngle = 360.0 * repeatTimeMSec / fullTimeMSec;
        final Color clrStop = Color.BlueViolet();

        SmoothContext ctx = mapViewSmooth.get(view);
        boolean isExecuted = (ctx.dir != 0);
        if (isExecuted) // if already executed
            return;

//        ctx.dir = 1; // forward direction smooth transition
        ctx.dir = -1; // backward direction smooth transition
        ctx.currentStepAngle = 360;

        AsyncRunner.Repeat(() -> {
            assert ctx.dir != 0;
            if (ctx.dir == 0)
                return;
            boolean forward = ctx.dir == 1;
            Color clrFrom = forward ? ctx.clrStart : clrStop;
            Color clrTo   = forward ? clrStop  : ctx.clrStart;
            Color clrCurr;
            ctx.currentStepAngle += ctx.dir * deltaStepAngle;
            if (forward ? (ctx.currentStepAngle >= 360)
                        : (ctx.currentStepAngle <= 0))
            {
                ctx.dir = forward ? -1 // forward direction smooth transition.
                                  : 0; // exit smooth transition
                clrCurr = clrTo;
            } else {
                double rad = FigureHelper.toRadian(ctx.currentStepAngle / 4);
                double koef = forward ? Math.sin(rad)
                                      : //Math.cos(rad);
                                        1 - Math.sin(rad);
                clrCurr = new Color((int)(clrFrom.getA() + koef * (clrTo.getA() - clrFrom.getA())),
                                    (int)(clrFrom.getR() + koef * (clrTo.getR() - clrFrom.getR())),
                                    (int)(clrFrom.getG() + koef * (clrTo.getG() - clrFrom.getG())),
                                    (int)(clrFrom.getB() + koef * (clrTo.getB() - clrFrom.getB())));
            }
            model.setBackgroundColor(clrCurr);
            //view.setBackgroundColor(Cast.toColor(clrCurr));
        }, repeatTimeMSec, () -> ctx.dir == 0);
    }

}
