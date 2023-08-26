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
import fmg.core.img.IImageModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.img.MosaicSkillModel;

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

    private static class SmoothTransition<TContext extends Context> {
        protected final TContext context;
        private final long fullTimeMSec;
        private final long repeatTimeMSec;

        public SmoothTransition(TContext context, long fullTimeMSec/*= 250*/, long repeatTimeMSec/*= 10*/) {
            this.context = context;
            this.fullTimeMSec = fullTimeMSec;
            this.repeatTimeMSec = repeatTimeMSec;
        }

        void onIteration() {}
        void onEndExecution() {}

        public void execute() {
            final double deltaStepAngle = 360.0 * repeatTimeMSec / fullTimeMSec;

            if (context.isSmoothInProgress()) // if already executed
                return;

            context.setCurrentStepAngle(context.isForward() ? 0 : 360);
            AsyncRunner.Repeat(() -> {
                context.setCurrentStepAngle(context.getCurrentStepAngle() + (context.isForward() ? +1 : -1) * deltaStepAngle);

                onIteration();

                if (context.isSmoothIsFinished())
                    onEndExecution();
            }, repeatTimeMSec, context::isSmoothIsFinished);
        }
    }

    private static class ColorContext extends Context {
        Color clrStart;
        Color clrStop;
    }
    private static Map<IImageModel, ColorContext> mapColorSmooth = new HashMap<>();

    private static Color getBackgroundColor(IImageModel model) {
        if (model instanceof MosaicGroupModel)
            return ((MosaicGroupModel)model).getBackgroundColor();
        if (model instanceof MosaicSkillModel)
            return ((MosaicSkillModel)model).getBackgroundColor();
        throw new IllegalArgumentException();
    }

    private static void setBackgroundColor(IImageModel model, Color clr) {
        if (model instanceof MosaicGroupModel)
            ((MosaicGroupModel)model).setBackgroundColor(clr);
        else
        if (model instanceof MosaicSkillModel)
            ((MosaicSkillModel)model).setBackgroundColor(clr);
        else
            throw new IllegalArgumentException();
    }

    public static void runColorSmoothTransition(IImageModel model) {
        if (!mapColorSmooth.containsKey(model)) {
            ColorContext context = new ColorContext();
            context.clrStart = getBackgroundColor(model); //Color.Coral;
            context.clrStop = Color.BlueViolet();
            mapColorSmooth.put(model, context);
        }

        ColorContext context = mapColorSmooth.get(model);
        SmoothTransition<ColorContext> colorSmoothTransition = new SmoothTransition<ColorContext>(context, 250, 10) {
            @Override
            void onIteration() {
                Color clrFrom = context.clrStart;
                Color clrTo   = context.clrStop;

                double coef = context.getSmoothCoefficient();
                //Logger.info("  forward={0}; currStepAngle={1}, coef={2}", context.isForward(), context.getCurrentStepAngle(), coef);
                Color clrCurr = new Color((int)(clrFrom.getA() + coef * (clrTo.getA() - clrFrom.getA())),
                                          (int)(clrFrom.getR() + coef * (clrTo.getR() - clrFrom.getR())),
                                          (int)(clrFrom.getG() + coef * (clrTo.getG() - clrFrom.getG())),
                                          (int)(clrFrom.getB() + coef * (clrTo.getB() - clrFrom.getB())));
                setBackgroundColor(model, clrCurr);
            }

            @Override
            void onEndExecution() {
                if (!context.isForward())
                    return;
                context.setForward(false);
                execute();
            }
        };

        context.setForward(true);
        colorSmoothTransition.execute();
    }

    @Deprecated // example of view animate
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
        if (menuView.getVisibility() == (targetIsVisible ? View.VISIBLE : View.GONE))
            return;

        // save
        long original = menuView.animate().getDuration();
        if (original == 0)
            return; // already called for this menu
        menuView.animate()
                .setDuration(0); // mark as called

        double h = calcHeight.get();
//        Logger.info("height=" + h);
        if (targetIsVisible) {
            Converters.setViewHeight(menuView, 0.1); // first  - set min height
            menuView.setVisibility(View.VISIBLE);           // second - set Visibility.Visible before smoothing
        }

        Context context = new Context();
        context.setForward(targetIsVisible);

        new SmoothTransition<Context>(context, 360, 50) {

            @Override
            void onIteration() {
                double scale = context.getSmoothCoefficient();
                double hScaled = h * scale;
                menuView.animate()
                        .scaleX((float)scale)
                        .scaleY((float)scale)
                        .translationX((float)-(menuView.getWidth() * (1 - scale) / 2))
                        .translationY((float)-((h - hScaled) / 2));
                Converters.setViewHeight(menuView, hScaled);
            }

            @Override
            void onEndExecution() {
                if (!targetIsVisible)
                    menuView.setVisibility(View.GONE); // first - set Visibility.Collapsed after smoothing

                // restore
                menuView.animate()
                        .translationX(targetIsVisible ? 0 : -menuView.getWidth())
                        .translationY(targetIsVisible ? 0 : (float)-h)
                        .scaleX(targetIsVisible ? 1 : 0.01f)
                        .scaleY(targetIsVisible ? 1 : 0.01f);
                AsyncRunner.invokeFromUiDelayed(() -> menuView.animate().setDuration(original), 1); // mark to stop repeat
                Converters.setViewHeight(menuView, ViewGroup.LayoutParams.WRAP_CONTENT);//h); // second - restore original height

                if (postAction != null)
                    postAction.run();
            }

        }.execute();
    }

    public static void runSmoothTransition(Context context, long fullTimeMSec/*= 150*/, long repeatTimeMSec/*= 10*/) {
        new SmoothTransition<>(context, fullTimeMSec, repeatTimeMSec).execute();
    }

}
