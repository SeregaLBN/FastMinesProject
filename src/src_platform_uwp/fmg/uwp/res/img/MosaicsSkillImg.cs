using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.data.controller.types;

namespace fmg.uwp.res.img {

   /// <summary> representable fmg.data.controller.types.ESkillLevel as image
   /// <br>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsSkillImg : core.img.MosaicsSkillImg<WriteableBitmap> {

      public MosaicsSkillImg(ESkillLevel group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      protected override WriteableBitmap CreateImage() {
         return new WriteableBitmap(Width, Height);
      }

      protected override void DrawBody() {
         var bmp = Image;
         bmp.Clear(BackgroundColor.ToWinColor());

         var stars = GetCoords();
         stars = stars.Reverse(); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
         foreach (var coords in stars) {
            var points = coords.PointsAsXyxyxySequence(true).ToArray();
            bmp.FillPolygon(points, ForegroundColorAttenuate.ToWinColor());

            // draw perimeter border
            var clr = BorderColor;
            if (clr.A != Color.Transparent.A) {
               var clrWin = clr.ToWinColor();
               var bw = BorderWidth;
               for (var i = 0; i < points.Length - 2; i += 2) {
                  bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clrWin, bw);
               }
            }
         }
      }

   }

}
