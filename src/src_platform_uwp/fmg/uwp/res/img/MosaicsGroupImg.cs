using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.uwp.res.img {

   /// <summary> representable fmg.core.types.EMosaicGroup as image </summary>
   public class MosaicsGroupImg : PolarLightsImg<EMosaicGroup, WriteableBitmap> {

      public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      public EMosaicGroup MosaicGroup => Entity;

      private IEnumerable<PointDouble> GetCoords() {
         var sizeInner = Math.Min(Width, Height);
         double s = sizeInner - Padding*2; // size inner square
         var vertices = 3 + MosaicGroup.Ordinal(); // vertices count
         var points = (MosaicGroup != EMosaicGroup.eOthers)
            ? FigureHelper.GetRegularPolygonCoords(vertices, s/2, RotateAngle)
            : FigureHelper.GetRegularStarCoords(4, s/2, s/5, RotateAngle);

         // adding offset
         var offset = sizeInner / 2.0;
         return points.Select(p => {
            p.x += offset;
            p.y += offset;
            return p;
         });
      }

      protected override void DrawBody() {
         var w = Width;
         var h = Height;
         var bmp = Image;
         var isNew = (bmp == null);
         if (isNew)
            bmp = new WriteableBitmap(w, h);

         bmp.Clear(BkColor.ToWinColor());

         var points = GetCoords().PointsAsXyxyxySequence(true).ToArray();
         bmp.FillPolygon(points, FillColorAttenuate.ToWinColor());

         // draw perimeter border
         var clr = BorderColor;
         if (clr.A != Color.Transparent.A) {
            for (var i = 0; i < points.Length - 2; i += 2) {
               bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clr.ToWinColor(), BorderWidth);
            }
         }

         if (isNew)
            Image = bmp;
      }

   }
}
