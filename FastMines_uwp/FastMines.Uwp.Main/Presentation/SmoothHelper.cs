using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using fmg.common;
using fmg.common.geom.util;
using fmg.core.img;
using fmg.uwp.utils;

namespace FastMines.Uwp.Main.Presentation {

    public static class SmoothHelper {

        public class Context {
            private bool forward = true;
            private double currentStepAngle; // smooth angle

            public bool Forward {
                get { return forward; }
                set {
                    if (forward == value)
                        return;
                    forward = value;
                }
            }

            public bool IsSmoothInProgress => (currentStepAngle > 0) && (currentStepAngle < 360);

            public bool IsSmoothIsFinished => Forward
                        ? (currentStepAngle >= 360)
                        : (currentStepAngle <= 0);

            public double CurrentStepAngle {
                get { return currentStepAngle; }
                set {
                    currentStepAngle = Forward
                            ? Math.Min(360, value)
                            : Math.Max(0, value);
                }
            }

            public double SmoothCoefficient { get {
                double rad = (CurrentStepAngle / 4).ToRadian();
                return //context.Forward ?
                        Math.Sin(rad)
                        //: 1 - Math.Cos(rad)
                        ;
            } }

        }

        private class SmoothTransition<TContext> where TContext : Context {
            protected readonly TContext context;
            private readonly long fullTimeMSec;
            private readonly long repeatTimeMSec;
            private readonly Action onIteration;
            private readonly Action onEndExecution;

            public SmoothTransition(TContext context,
                long fullTimeMSec/*= 250*/,
                long repeatTimeMSec/*= 10*/,
                Action onIteration = null,
                Action onEndExecution = null)
            {
                this.context = context;
                this.fullTimeMSec = fullTimeMSec;
                this.repeatTimeMSec = repeatTimeMSec;
                this.onIteration    = onIteration;
                this.onEndExecution = onEndExecution;
            }

            protected virtual void OnIteration()    { onIteration?.Invoke(); }
            protected virtual void OnEndExecution() { onEndExecution?.Invoke(); }

            public void Execute() {
                var deltaStepAngle = 360.0 * repeatTimeMSec / fullTimeMSec;

                if (context.IsSmoothInProgress) // if already executed
                    return;

                context.CurrentStepAngle = context.Forward ? 0 : 360;
                AsyncRunner.Repeat(() => {
                    context.CurrentStepAngle = context.CurrentStepAngle + (context.Forward ? +1 : -1) * deltaStepAngle;

                    OnIteration();

                    if (context.IsSmoothIsFinished)
                        OnEndExecution();
                }, TimeSpan.FromMilliseconds(repeatTimeMSec), () => context.IsSmoothIsFinished);
            }
        }

        private class ColorContext : Context {
            internal Color clrStart;
            internal Color clrStop;
        }
        public static void ApplyButtonColorSmoothTransition(Button bttn, AnimatedImageModel model) {
            var context = new ColorContext() {
                clrStart = model.BackgroundColor, //Color.Coral;
                clrStop = Color.BlueViolet
            };
            var colorSmoothTransition = new SmoothTransition<ColorContext>(context, 250, 10,
                //onIteration
                () => {
                    Color clrFrom = context.clrStart;
                    Color clrTo = context.clrStop;

                    double coef = context.SmoothCoefficient;
                    //LoggerSimple.Put("  forward={0}; currStepAngle={1}, coef={2}", context.Forward, context.CurrentStepAngle, coef);
                    var clrCurr = new Color((byte)(clrFrom.A + coef * (clrTo.A - clrFrom.A)),
                                            (byte)(clrFrom.R + coef * (clrTo.R - clrFrom.R)),
                                            (byte)(clrFrom.G + coef * (clrTo.G - clrFrom.G)),
                                            (byte)(clrFrom.B + coef * (clrTo.B - clrFrom.B)));
                    model.BackgroundColor = clrCurr;
                //},
                ////onEndExecution
                //() => {
                //    if (!context.Forward)
                //        return;
                //    context.Forward = false;
                //    Execute(context);
                });

            void handler(bool forward) {
                context.Forward = forward;
                colorSmoothTransition.Execute();
            }
            bttn.PointerEntered += (s, ev3) => handler(true);
            bttn.PointerExited  += (s, ev3) => handler(false);
        }

        /// <summary> set pseudo-async ListView.Visibility = targetIsVisible ? Visibility.Visible : Visibility.Collapsed </summary>
        public static void ApplySmoothVisibilityOverScale(ListView lv, bool targetIsVisible, Func<double> calcHeight, Action postAction = null) {
            if (lv.Visibility == (targetIsVisible ? Visibility.Visible : Visibility.Collapsed))
                return;

            // save
            var original = lv.RenderTransform;
            if (original is CompositeTransform)
                return; // already called for this list

            var h0 = lv.Height;
            var h1 = h0;
            if (double.IsNaN(h1))
                h1 = calcHeight();

            var transformer = new CompositeTransform();
            lv.RenderTransform = transformer;

            if (targetIsVisible) {
                transformer.ScaleX = transformer.ScaleY = 0.01;
                lv.Height = 0.1;                    // first  - set min height
                lv.Visibility = Visibility.Visible; // second - set Visibility.Visible before smoothing
            } else {
                transformer.ScaleX = transformer.ScaleY = 1;
            }

            var context = new Context {
                Forward = targetIsVisible
            };
            new SmoothTransition<Context>(context, 360, 50,

                //onIteration
                () => {
                    double scale = context.SmoothCoefficient;
                    transformer.ScaleX = transformer.ScaleY = scale;
                    double hScaled = h1 * scale;
                    lv.Height = hScaled;
                },

                //onEndExecution
               () => {
                   if (!targetIsVisible)
                       lv.Visibility = Visibility.Collapsed; // first - set Visibility.Collapsed after smoothing

                   // restore
                   lv.RenderTransform = original; // mark to stop repeat
                   lv.Height = h0;                // second - restore original height

                   postAction?.Invoke();
               }
            ).Execute();
        }

    }

}
