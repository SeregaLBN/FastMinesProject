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
            int flag = 0;
            var clrFrom = model.BackgroundColor; //Color.Coral;
            var clrTo = Color.BlueViolet;
            double fullTimeMsec = 1500, repeatTimeMsec = 100;
            double currStepAngle = 0;
            double deltaStepAngle = 360.0 / (fullTimeMsec / repeatTimeMsec);
            bttn.PointerEntered += (s, ev3) => {
                flag = 1; // start entered
                Action r = () => {
                    Color clrCurr;
                    if (currStepAngle >= 360) {
                        flag = 0; // stop
                        clrCurr = clrTo;
                    } else {
                        currStepAngle += deltaStepAngle;
                        var sin = Math.Sin((currStepAngle / 4).ToRadian());
                        clrCurr = new Color((byte)(clrFrom.A + sin * (clrTo.A - clrFrom.A)),
                                            (byte)(clrFrom.R + sin * (clrTo.R - clrFrom.R)),
                                            (byte)(clrFrom.G + sin * (clrTo.G - clrFrom.G)),
                                            (byte)(clrFrom.B + sin * (clrTo.B - clrFrom.B)));
                    }
                    model.BackgroundColor = clrCurr;
                };
                r.RepeatNoWait(TimeSpan.FromMilliseconds(repeatTimeMsec), () => flag != 1);
            };
            bttn.PointerExited += (s, ev3) => {
                flag = 2; // start exited
                Action r = () => {
                    Color clrCurr;
                    if (currStepAngle <= 0) {
                        flag = 0; // stop
                        clrCurr = clrFrom;
                    } else {
                        currStepAngle -= deltaStepAngle;
                        var cos = Math.Cos((currStepAngle / 4).ToRadian());
                        clrCurr = new Color((byte)(clrTo.A - (1 - cos * (clrFrom.A - clrTo.A))),
                                            (byte)(clrTo.R - (1 - cos * (clrFrom.R - clrTo.R))),
                                            (byte)(clrTo.G - (1 - cos * (clrFrom.G - clrTo.G))),
                                            (byte)(clrTo.B - (1 - cos * (clrFrom.B - clrTo.B))));
                    }
                    model.BackgroundColor = clrCurr;
                };
                r.RepeatNoWait(TimeSpan.FromMilliseconds(repeatTimeMsec), () => flag != 2);
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
            r.RepeatNoWait(TimeSpan.FromMilliseconds(50), () => ReferenceEquals(lv.RenderTransform, original));
        }

    }

}
