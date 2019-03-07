package fmg.android.app.presentation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import fmg.android.utils.AsyncRunner;
import fmg.common.Color;
import fmg.common.geom.util.FigureHelper;
import fmg.core.img.AnimatedImageModel;

public final class SmoothHelper {
    private SmoothHelper() {}

    public static class Context {
        private boolean forward = true;
        private double currentStepAngle = forward ? 360 : 0; // smooth angle

        public boolean isForward() { return forward; }
        public void   setForward(boolean forward) {
            if (this.forward == forward)
                return;
            this.forward = forward;
        }

        public boolean isSmoothInProgress() {
            return (currentStepAngle > 0) &&  (currentStepAngle < 360);
        }

        public boolean isSmoothIsFinished() {
            return isForward()
                    ? (currentStepAngle >= 360)
                    : (currentStepAngle <= 0);
        }

        public void pullTrigger() { setCurrentStepAngle(forward ? 0 : 360); }
        public double getCurrentStepAngle() { return currentStepAngle; }
        public void setCurrentStepAngle(double currentStepAngle) {
            this.currentStepAngle = isForward()
                    ? Math.min(360, currentStepAngle)
                    : Math.max(0  , currentStepAngle);
        }

        public double getSmoothCoefficient() {
            double rad = FigureHelper.toRadian(getCurrentStepAngle() / 4);
            return //context.isForward() ?
                    Math.sin(rad)
                    //: 1 - Math.cos(rad)
                    ;
        }

    }

    private static class SmoothTransition {
        private final long fullTimeMSec;
        private final long repeatTimeMSec;

        public SmoothTransition(long fullTimeMSec/*= 250*/, long repeatTimeMSec/*= 10*/) {
            this.fullTimeMSec = fullTimeMSec;
            this.repeatTimeMSec = repeatTimeMSec;
        }

        void onStartExecute(Context context) { context.pullTrigger(); }
        void onIteration() {}
        void onEndExecution() {}

        public boolean execute(Context context) {
            final double deltaStepAngle = 360.0 * repeatTimeMSec / fullTimeMSec;
            final Color clrStop = Color.BlueViolet();

            if (context.isSmoothInProgress()) // if already executed
                return false;

            onStartExecute(context);

            AsyncRunner.Repeat(() -> {
                context.setCurrentStepAngle(context.getCurrentStepAngle() + (context.isForward() ? +1 : -1) * deltaStepAngle);

                onIteration();

                if (context.isSmoothIsFinished())
                    onEndExecution();
            }, repeatTimeMSec, () -> context.isSmoothIsFinished());

            return true;
        }
    }

    private static class ColorContext extends Context {
        Color clrStart;
    }

    private static Map<AnimatedImageModel, ColorContext> mapColorSmooth = new HashMap<>();

    public static void runColorSmoothTransition(AnimatedImageModel model) {
        if (!mapColorSmooth.containsKey(model)) {
            ColorContext context = new ColorContext();
            context.clrStart = model.getBackgroundColor(); //Color.Coral;
            mapColorSmooth.put(model, context);
        }

        ColorContext context = mapColorSmooth.get(model);
        final Color clrStop = Color.BlueViolet();

        context.setForward(true);
        new SmoothTransition(250, 10) {

            @Override
            void onIteration() {
                Color clrFrom = context.clrStart;
                Color clrTo   = clrStop;

                double coef = context.getSmoothCoefficient();
//                LoggerSimple.put("  forward={0}; currStepAngle={1}, coef={2}", context.isForward(), context.getCurrentStepAngle(), coef);

                Color clrCurr = new Color((int)(clrFrom.getA() + coef * (clrTo.getA() - clrFrom.getA())),
                                          (int)(clrFrom.getR() + coef * (clrTo.getR() - clrFrom.getR())),
                                          (int)(clrFrom.getG() + coef * (clrTo.getG() - clrFrom.getG())),
                                          (int)(clrFrom.getB() + coef * (clrTo.getB() - clrFrom.getB())));
                model.setBackgroundColor(clrCurr);
            }

            @Override
            void onEndExecution() {
                if (!context.isForward())
                    return;
                context.setForward(false);
                execute(context);
            }

        }.execute(context);
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

    public static void runSmoothTransition(Context context, long fullTimeMSec/*= 150*/, long repeatTimeMSec/*= 10*/) {
        new SmoothTransition(fullTimeMSec, repeatTimeMSec).execute(context);
    }

}
