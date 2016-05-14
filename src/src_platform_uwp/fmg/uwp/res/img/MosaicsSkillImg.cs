using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.data.controller.types;

namespace fmg.uwp.res.img {

   /// <summary> representable fmg.data.controller.types.ESkillLevel as image </summary>
   public class MosaicsSkillImg : core.img.RotatedImg<ESkillLevel, WriteableBitmap> {

      public MosaicsSkillImg(ESkillLevel group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      public ESkillLevel MosaicSkill => Entity;

      private IEnumerable<IEnumerable<PointDouble>> GetCoords() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var r1 = sq/7; // external radius
         var r2 = sq/12; // internal radius
         var ordinal = MosaicSkill.Ordinal();
         var rays = 5 + ordinal; // rays count
         var stars = 4 + ordinal; // number of stars on the perimeter of the circle
         var angle = RotateAngle;
         var starAngle = 360.0/stars;
         return Enumerable.Range(0, stars).Select(st => {
            var points = (MosaicSkill == ESkillLevel.eCustom)
               ? FigureHelper.GetRegularPolygonCoords(3 + st % 4, r1, -angle)
               : FigureHelper.GetRegularStarCoords(rays, r1, r2, -angle);

            // (un)comment next line to view result changes...
            angle = Math.Sin((angle/4).ToRadian())*angle; // accelerate / ускоряшка..

            // adding offset
            var offset = FigureHelper.GetPointOnCircle(sq/3, angle + st*starAngle);
            offset.X += Width / 2.0;
            offset.Y += Height / 2.0;
            return points.Select(p => {
               p.X += offset.X;
               p.Y += offset.Y;
               return p;
            });
         });
      }

      protected override WriteableBitmap CreateImage() {
         //LoggerSimple.Put("CreateImage: Width={0}; Height={1}: {2}", Width, Height, Entity);
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
