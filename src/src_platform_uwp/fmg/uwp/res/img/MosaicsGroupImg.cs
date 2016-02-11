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
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var vertices = 3 + MosaicGroup.Ordinal(); // vertices count
         var points = (MosaicGroup != EMosaicGroup.eOthers)
            ? FigureHelper.GetRegularPolygonCoords(vertices, sq/2, RotateAngle)
            : FigureHelper.GetRegularStarCoords(4, sq/2, sq/5, RotateAngle);

         // adding offset
         var offsetX = Width / 2.0;
         var offsetY = Height / 2.0;
         return points.Select(p => {
            p.X += offsetX;
            p.Y += offsetY;
            return p;
         });
      }

      protected override WriteableBitmap CreateImage() {
         //LoggerSimple.Put("CreateImage: Width={0}; Height={1}: {2}", Width, Height, Entity);
         return new WriteableBitmap(Width, Height);
      }

      protected override void DrawBody() {
         var bmp = Image;

         bmp.Clear(BackgroundColor.ToWinColor());

         var points = GetCoords().PointsAsXyxyxySequence(true).ToArray();
         bmp.FillPolygon(points, ForegroundColorAttenuate.ToWinColor());

         // draw perimeter border
         var clr = BorderColor;
         if (clr.A != Color.Transparent.A) {
            for (var i = 0; i < points.Length - 2; i += 2) {
               bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clr.ToWinColor(), BorderWidth);
            }
         }
      }

   }
}
