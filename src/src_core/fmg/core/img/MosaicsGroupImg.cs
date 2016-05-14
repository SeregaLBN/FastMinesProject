using System;
using System.Linq;
using System.Collections.Generic;
using fmg.core.types;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

   /// <summary> representable fmg.core.types.EMosaicGroup as image </summary>
   public abstract class MosaicsGroupImg<TImage> : PolarLightsImg<EMosaicGroup, TImage>
      where TImage : class
   {

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

   }

}
