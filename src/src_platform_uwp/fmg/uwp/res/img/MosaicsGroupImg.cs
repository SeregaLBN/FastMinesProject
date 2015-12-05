using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using Rect = Windows.Foundation.Rect;

namespace fmg.uwp.res.img {

   /// <summary> representable fmg.core.types.EMosaicGroup as image </summary>
   public class MosaicsGroupImg : PolarLightsImg<EMosaicGroup, WriteableBitmap> {

      public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      public EMosaicGroup MosaicGroup => Entity;

      protected override IEnumerable<PointDouble> GetCoords() {
         double s = Size - Padding*2; // size inner square
         var rays = 3 + MosaicGroup.Ordinal(); // rays count
         var points = (MosaicGroup != EMosaicGroup.eOthers)
            ? FigureHelper.GetRegularPolygonCoords(rays, s/2)
            : FigureHelper.GetRegularStarCoords(4, s/2, s/5);

         // adding offset
         var offset = Padding + s/2;
         points = points.Select(p => {
            p.x += offset;
            p.y += offset;
            return p;
         });
         return points;
      }

      protected override void DrawBody() {
         var w = Width;
         var h = Height;
         var bmp = new WriteableBitmap(w, h);

         var rotate = Rotate || (Math.Abs(RotateAngle) > 0.5);
         Action<WriteableBitmap> funcFillBk =
            img => { img.FillPolygon(new[] {0, 0, w, 0, w, h, 0, h, 0, 0}, BkColor.ToWinColor()); };
         if (!rotate) {
            funcFillBk(bmp);
         }

         var points = GetCoords().PointsAsXyxyxySequence(true).ToArray();
         bmp.FillPolygon(points, FillColorAttenuate.ToWinColor());

         {
            // draw perimeter border
            var clr = BorderColor;
            if (clr.A != Color.Transparent.A) {
               for (var i = 0; i < points.Length-2; i += 2) {
                  bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clr.ToWinColor(), BorderWidth);
               }
            }
         }

         if (rotate)
            bmp = bmp.RotateFree(RotateAngle);

         if (Image == null) {
            if (rotate) {
               var tmp = new WriteableBitmap(w, h);
               funcFillBk(tmp);
               var rc = new Rect(0, 0, w, h);
               tmp.Blit(rc, bmp, rc);
               bmp = tmp;
            }
            Image = bmp;
         } else {
            var rc = new Rect(0, 0, w, h);
            if (rotate) {
               funcFillBk(Image);
            }
            Image.Blit(rc, bmp, rc);
         }
      }

   }
}
