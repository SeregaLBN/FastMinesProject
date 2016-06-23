using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.data.controller.types;

namespace fmg.core.img {

   /// <summary> Representable <see cref="ESkillLevel"/> as image </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class MosaicsSkillImg<TImage> : RotatedImg<ESkillLevel, TImage>
      where TImage : class
   {
      protected MosaicsSkillImg(ESkillLevel group)
         : base(group) {}

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
         var center = new PointDouble(Width / 2.0, Height / 2.0);
         var zero = new PointDouble(0, 0);
         return Enumerable.Range(0, stars).Select(st => {
            // (un)comment next line to view result changes...
            angle = Math.Sin((angle/4).ToRadian())*angle; // accelerate / ускоряшка..

            // adding offset
            var offset = FigureHelper.GetPointOnCircle(sq/3, angle + st*starAngle, zero);
            var centerStar = new PointDouble(center.X + offset.X, center.Y + offset.Y);

            return (MosaicSkill == ESkillLevel.eCustom)
               ? FigureHelper.GetRegularPolygonCoords(3 + st % 4, r1, centerStar, -angle)
               : FigureHelper.GetRegularStarCoords(rays, r1, r2, centerStar, -angle);

         })
         .Reverse(); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
      }

   }

}
