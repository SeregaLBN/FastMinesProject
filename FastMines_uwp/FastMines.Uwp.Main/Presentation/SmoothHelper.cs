using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;
using fmg.common;
using fmg.core.img;
using fmg.common.geom.util;
using fmg.uwp.utils;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;

namespace FastMines.Uwp.Main.Presentation {

    public static class SmoothHelper {

        public static void ApplyButtonColorSmoothTransition(Button bttn, AnimatedImageModel model) {
            int dir = 0; // direction smooth transition (0 - exit; 1 - forward; -1 - reverse)
            var clrStart = model.BackgroundColor; //Color.Coral;
            var clrStop = Color.BlueViolet;
            double fullTimeMsec = 700, repeatTimeMsec = 30;
            double currStepAngle = 0;
            double deltaStepAngle = 360.0 / (fullTimeMsec / repeatTimeMsec);

            bttn.PointerEntered += (s, ev3) => {
                dir = 1; // start entered
                Action r = () => {
                    Color clrFrom = clrStart;
                    Color clrTo = clrStop;
                    Color clrCurr;
                    if (currStepAngle >= 360) {
                        dir = 0; // stop
                        clrCurr = clrTo;
                    } else {
                        currStepAngle += deltaStepAngle;
                        var rad = (currStepAngle / 4).ToRadian();
                        var koef = Math.Sin(rad);
                        clrCurr = new Color((byte)(clrFrom.A + koef * (clrTo.A - clrFrom.A)),
                                            (byte)(clrFrom.R + koef * (clrTo.R - clrFrom.R)),
                                            (byte)(clrFrom.G + koef * (clrTo.G - clrFrom.G)),
                                            (byte)(clrFrom.B + koef * (clrTo.B - clrFrom.B)));
                    }
                    model.BackgroundColor = clrCurr;
                };
                r.Repeat(TimeSpan.FromMilliseconds(repeatTimeMsec), () => dir != 1);
            };
            bttn.PointerExited += (s, ev3) => {
                dir = -1; // start exited
                Action r = () => {
                    Color clrFrom = clrStop;
                    Color clrTo = clrStart;
                    Color clrCurr;
                    if (currStepAngle <= 0) {
                        dir = 0; // stop
                        clrCurr = clrTo;
                    } else {
                        currStepAngle -= deltaStepAngle;
                        var rad = (currStepAngle / 4).ToRadian();
                        var koef = Math.Cos(rad);//1 - Math.Sin(rad); //
                        clrCurr = new Color((byte)(clrFrom.A + koef * (clrTo.A - clrFrom.A)),
                                            (byte)(clrFrom.R + koef * (clrTo.R - clrFrom.R)),
                                            (byte)(clrFrom.G + koef * (clrTo.G - clrFrom.G)),
                                            (byte)(clrFrom.B + koef * (clrTo.B - clrFrom.B)));
                    }
                    model.BackgroundColor = clrCurr;
                };
                r.Repeat(TimeSpan.FromMilliseconds(repeatTimeMsec), () => dir != -1);
            };
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
                lv.Visibility = target; // second - set Visibility.Visible before smooting
            } else {
                transformer.ScaleX = transformer.ScaleY = 1;
            }

            var angle = 0.0;
            Action r = () => {
                angle += 12.345;

                if (angle < 90) { // repeat?
                    var scale = toVisible
                        ? Math.Sin(angle.ToRadian())
                        : Math.Cos(angle.ToRadian());
                    transformer.ScaleX = transformer.ScaleY = scale;
                    lv.Height = h1 * scale;
                } else {
                    // stop it

                    if (!toVisible)
                        lv.Visibility = target;    // first - set Visibility.Collapsed after smooting

                    // restore
                    lv.RenderTransform = original; // mark to stop repeat
                    lv.Height = h0;                // second - restore original height

                    postAction?.Invoke();
                }
            };
            r.Repeat(TimeSpan.FromMilliseconds(50), () => ReferenceEquals(lv.RenderTransform, original));
        }

    }

}
