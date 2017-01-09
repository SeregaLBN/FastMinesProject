using System;
using System.Linq;
using System.Reflection;
using Windows.Graphics.Display;
using fmg.common.geom;

namespace fmg.uwp.utils {

   public static class ScreenResolutionHelper {

      /// <summary> get current display size </summary>
      public static Size GetScreenSize() {
         var displayInformation = DisplayInformation.GetForCurrentView();
         TypeInfo t = typeof(DisplayInformation).GetTypeInfo();
         var props = t.DeclaredProperties.Where(x => x.Name.StartsWith("Screen") && x.Name.EndsWith("InRawPixels")).ToArray();
         var w = props.Where(x => x.Name.Contains("Width")).First().GetValue(displayInformation);
         var h = props.Where(x => x.Name.Contains("Height")).First().GetValue(displayInformation);
         var size = new Size(System.Convert.ToInt32(w), System.Convert.ToInt32(h));
         switch (displayInformation.CurrentOrientation) {
         case DisplayOrientations.Landscape:
         case DisplayOrientations.LandscapeFlipped:
            size = new Size(Math.Max(size.Width, size.Height), Math.Min(size.Width, size.Height));
            break;
         case DisplayOrientations.Portrait:
         case DisplayOrientations.PortraitFlipped:
            size = new Size(Math.Min(size.Width, size.Height), Math.Max(size.Width, size.Height));
            break;
         }
         return size;
      }

      public static Bound GetScreenPadding() {
         // @TODO: not implemented...
         return new Bound();
      }

      public static Size GetDesktopSize() {
         var screenSize = GetScreenSize();
         var screenPadding = GetScreenPadding();
         return new Size(
            screenSize.Width - screenPadding.LeftAndRight,
            screenSize.Height - screenPadding.TopAndBottom);
      }

   }

}
