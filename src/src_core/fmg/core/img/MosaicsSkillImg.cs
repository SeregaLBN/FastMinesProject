using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.data.controller.types;

namespace fmg.core.img {

   /// <summary> representable fmg.data.controller.types.ESkillLevel as image </summary>
   public abstract class MosaicsSkillImg<TImage> : RotatedImg<ESkillLevel, TImage>
      where TImage : class
   {

      protected MosaicsSkillImg(ESkillLevel group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      public ESkillLevel MosaicSkill => Entity;

      protected IEnumerable<IEnumerable<PointDouble>> GetCoords() {
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

   }

}
