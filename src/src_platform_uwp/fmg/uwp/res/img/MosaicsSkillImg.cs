using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.data.controller.types;
using Rect = Windows.Foundation.Rect;

namespace fmg.uwp.res.img {

   /// <summary> representable fmg.data.controller.types.ESkillLevel as image </summary>
   public class MosaicsSkillImg : RotatedImg<ESkillLevel, WriteableBitmap> {

      public MosaicsSkillImg(ESkillLevel group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      public ESkillLevel MosaicSkill => Entity;

      protected override IEnumerable<PointDouble> GetCoords() {
         double s = Size - Padding*2; // size inner Square
         var r1 = s/2; // external radius
         var r2 = s/5; // internal radius
         var rays = 4 + MosaicSkill.Ordinal(); // rays count
         var points = FigureHelper.GetRegularStarCoords(rays, r1, r2);

         if (Rotate || (Math.Abs(RotateAngle) > 0.5))
            points = points.Rotate(RotateAngle);

         // adding offset
         var offset = Padding + s/2;
         return points.Select(p => {
            p.x += offset;
            p.y += offset;
            return p;
         });
      }

      protected override void DrawBody() {
         var w = Width;
         var h = Height;
         var bmp = new WriteableBitmap(w, h);

         bmp.Clear(BkColor.ToWinColor());

         var points = GetCoords().PointsAsXyxyxySequence(true).ToArray();
         bmp.FillPolygon(points, FillColorAttenuate.ToWinColor());

         {
            // draw perimeter border
            var clr = BorderColor;
            if (clr.A != Color.Transparent.A) {
               for (var i = 0; i < points.Length - 2; i += 2) {
                  bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clr.ToWinColor(), BorderWidth);
               }
            }
         }

         if (Image == null) {
            Image = bmp;
         } else {
            var rc = new Rect(0, 0, w, h);
            Image.Blit(rc, bmp, rc);
         }
      }

   }
}
