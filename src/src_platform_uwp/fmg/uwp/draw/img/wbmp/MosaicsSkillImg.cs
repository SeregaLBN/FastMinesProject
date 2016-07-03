using System.Linq;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.data.controller.types;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {

   /// <summary> Representable <see cref="ESkillLevel"/> as image.
   /// <br/>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsSkillImg : AMosaicsSkillImg<WriteableBitmap> {

      static MosaicsSkillImg() {
         StaticRotateImgConsts.Init();
      }

      public MosaicsSkillImg(ESkillLevel group)
         : base(group)
      { }

      protected override WriteableBitmap CreateImage() {
         return new WriteableBitmap(Width, Height);
      }

      protected override void DrawBody() {
         var bmp = Image;
         bmp.Clear(BackgroundColor.ToWinColor());

         var stars = GetCoords();
         foreach (var coords in stars) {
            var points = coords.PointsAsXyxyxySequence(true).ToArray();
            bmp.FillPolygon(points, ForegroundColor.ToWinColor());

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
