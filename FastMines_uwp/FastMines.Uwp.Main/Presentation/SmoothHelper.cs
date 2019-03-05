using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using fmg.common;
using fmg.core.img;
using fmg.common.geom.util;
using fmg.uwp.utils;

namespace FastMines.Uwp.Main.Presentation {

    public static class SmoothHelper {

        public static void ApplyButtonColorSmoothTransition(Button bttn, AnimatedImageModel model) {
            int dir = 0; // direction smooth transition (0 - exit; 1 - forward; -1 - reverse)
            const double fullTimeMsec = 700, repeatTimeMsec = 30;
            const double deltaStepAngle = 360.0 / (fullTimeMsec / repeatTimeMsec);
            double currStepAngle = 0;
            var clrStart = model.BackgroundColor; //Color.Coral;
            var clrStop = Color.BlueViolet;


            void handler(bool forward) {
                dir = forward ? 1  // start entered
                             : -1; // start exited
                void run() {
                    Color clrFrom = forward ? clrStart : clrStop;
                    Color clrTo   = forward ? clrStop  : clrStart;
                    Color clrCurr;
                    currStepAngle += dir * deltaStepAngle;
                    if (forward ? (currStepAngle >= 360)
                                : (currStepAngle <= 0))
                    {
                        dir = 0; // stop
                        clrCurr = clrTo;
                    } else {
                        var rad = (currStepAngle / 4).ToRadian();
                        var koef = forward ? Math.Sin(rad)
                                           : //Math.Cos(rad);
                                             1 - Math.Sin(rad);
                        clrCurr = new Color((byte)(clrFrom.A + koef * (clrTo.A - clrFrom.A)),
                                            (byte)(clrFrom.R + koef * (clrTo.R - clrFrom.R)),
                                            (byte)(clrFrom.G + koef * (clrTo.G - clrFrom.G)),
                                            (byte)(clrFrom.B + koef * (clrTo.B - clrFrom.B)));
                    }
                    model.BackgroundColor = clrCurr;
                }
                ((Action)run).Repeat(TimeSpan.FromMilliseconds(repeatTimeMsec), () => forward ? (dir != 1) : (dir != -1));
            }

            bttn.PointerEntered += (s, ev3) => handler(true);
            bttn.PointerExited  += (s, ev3) => handler(false);
        }

        /// <summary> set pseudo-async ListView.Visibility = target </summary>
        public static void ApplySmoothVisibilityOverScale(ListView lv, Visibility target, Func<double> calcHeight, Action postAction = null) {
            if (lv.Visibility == target)
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

            var toVisible = (target == Visibility.Visible);
            if (toVisible) {
                transformer.ScaleX = transformer.ScaleY = 0.01;
                lv.Height = 0.1;        // first  - set min height
                lv.Visibility = target; // second - set Visibility.Visible before smoothing
            } else {
                transformer.ScaleX = transformer.ScaleY = 1;
            }

            var angle = 0.0;
            void run() {
                angle += 12.345;

                if (angle < 90) { // repeat?
                    var scale = toVisible
                        ? Math.Sin(angle.ToRadian())
                        : //Math.Cos(angle.ToRadian());
                          1 - Math.Sin(angle.ToRadian());
                    transformer.ScaleX = transformer.ScaleY = scale;
                    lv.Height = h1 * scale;
                } else {
                    // stop it

                    if (!toVisible)
                        lv.Visibility = target;    // first - set Visibility.Collapsed after smoothing

                    // restore
                    lv.RenderTransform = original; // mark to stop repeat
                    lv.Height = h0;                // second - restore original height

                    postAction?.Invoke();
                }
            }
            ((Action)run).Repeat(TimeSpan.FromMilliseconds(50), () => ReferenceEquals(lv.RenderTransform, original));
        }

    }

}
