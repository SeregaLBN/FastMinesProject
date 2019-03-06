package fmg.android.app.presentation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.LoggerSimple;
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

    public static void applySmoothVisibilityOverScaleOld(View menuView, boolean targetIsVisible, Supplier<Double> calcHeight, Runnable postAction) {
        final int MENU_ANIMATION_DURATION = 500;
        if (targetIsVisible) {
            menuView.setVisibility(View.VISIBLE);
            menuView.animate()
                    .alpha(1f)
                    .translationX(0)
                    .translationY(0)
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(MENU_ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (postAction != null)
                                postAction.run();
                        }
                    });
        } else {
            menuView.animate()
                    .alpha(0.2f)
                    .translationX(-menuView.getWidth() / 2)
                    .translationY(-menuView.getHeight() / 2)
                    .scaleX(0.01f)
                    .scaleY(0.01f)
                    .setDuration(MENU_ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            menuView.setVisibility(View.GONE);
                            if (postAction != null)
                                postAction.run();
                        }
                    });
        }
    }

    public static void applySmoothVisibilityOverScale(View menuView, boolean targetIsVisible, Supplier<Double> calcHeight, Runnable postAction) {
        if (targetIsVisible) {
            if (menuView.getVisibility() == View.VISIBLE)
                return;
        } else {
            if (menuView.getVisibility() != View.VISIBLE)
                return;
        }

        // save
        long original = menuView.animate().getDuration();
        if (original == 0)
            return; // already called for this menu
        menuView.animate()
                .setDuration(0); // mark as called

        double h = calcHeight.get();
//        LoggerSimple.put("height=" + h);
        if (targetIsVisible) {
            Converters.setViewHeight(menuView, 0.1); // first  - set min height
            menuView.setVisibility(View.VISIBLE);           // second - set Visibility.Visible before smoothing
        }

        AsyncRunner.RunWithDelay(() -> {
            double[] angle = { 0.0 };
            Runnable[] run = { null };
            run[0] = () -> {
                double a = (angle[0] += 12.345);

                if (a < 90) { // repeat?
                    double rad = FigureHelper.toRadian(a);
                    double scale = targetIsVisible
                            ? Math.sin(rad)
                            : //Math.cos(rad);
                             1 - Math.sin(rad);
                    double hScaled = h * scale;
                    menuView.animate()
                            .scaleX((float)scale)
                            .scaleY((float)scale)
                            .translationX((float)-(menuView.getWidth() * (1 - scale) / 2))
                            .translationY((float)-((h - hScaled) / 2));
                    Converters.setViewHeight(menuView, hScaled);
                } else {
                    // stop it

                    if (!targetIsVisible)
                        menuView.setVisibility(View.GONE); // first - set Visibility.Collapsed after smoothing

                    // restore
                    menuView.animate()
                            .translationX(targetIsVisible ? 0 : -menuView.getWidth())
                            .translationY(targetIsVisible ? 0 : (float)-h)
                            .scaleX(targetIsVisible ? 1 : 0.01f)
                            .scaleY(targetIsVisible ? 1 : 0.01f);
                    AsyncRunner.RunWithDelay(() -> menuView.animate().setDuration(original), 1); // mark to stop repeat
                    Converters.setViewHeight(menuView, ViewGroup.LayoutParams.WRAP_CONTENT);//h); // second - restore original height

                    if (postAction != null)
                        postAction.run();
                }
            };
            AsyncRunner.Repeat(run[0], 50, () -> menuView.animate().getDuration() == original);
        }, 2);
    }

    public static void runWidthSmoothTransition(MainMenuViewModel.SplitViewPane splitPane) {
        final long fullTimeMSec = 150, repeatTimeMSec = 10;
        final double deltaStepAngle = 360.0 * repeatTimeMSec / fullTimeMSec;

        if (splitPane.isSmoothInProgress()) { // if already executed
//            LoggerSimple.put("< SmoothHelper::runWidthSmoothTransition: exit - already executed");
            return;
        }

        AsyncRunner.Repeat(() -> {
//            LoggerSimple.put(" SmoothHelper::runWidthSmoothTransition: run lambda => currentStepAngle=" + splitPane.getCurrentStepAngle());
            boolean forward = splitPane.isOpen();
            splitPane.setCurrentStepAngle(
                    splitPane.getCurrentStepAngle()
                          + (forward ? +deltaStepAngle
                                     : -deltaStepAngle));
        }, repeatTimeMSec, () -> splitPane.isSmoothIsFinished());
    }

}
