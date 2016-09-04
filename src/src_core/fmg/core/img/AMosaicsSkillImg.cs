using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.data.controller.types;

namespace fmg.core.img {

   /// <summary> Abstract representable <see cref="ESkillLevel"/> as image </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class AMosaicsSkillImg<TImage> : PolarLightsImg<TImage>
      where TImage : class
   {
      /// <param name="skill">may be null. if Null - representable image of typeof(ESkillLevel)</param>
      protected AMosaicsSkillImg(ESkillLevel? skill) {
         _mosaicSkill = skill;
      }

      private ESkillLevel? _mosaicSkill;
      public ESkillLevel? MosaicSkill {
         get { return _mosaicSkill; }
         set { SetProperty(ref _mosaicSkill, value); }
      }

      protected IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords() {
         return !_mosaicSkill.HasValue
            ? GetCoords_SkillLevelAsType()
            : GetCoords_SkillLevelAsValue();
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords_SkillLevelAsType() {
         const bool bigMaxStar = !true; // true - большая звезда - вне картинки; false - большая звезда - внутри картинки.
         const bool accelerateRevert = !true; // ускорение под конец анимации, иначе - в начале...

         var rays = 5;
         var stars = bigMaxStar ? 6 : 4;

         var angle = RotateAngle;
         //var angleAccumulative = angle;
         var anglePart = 360.0 / stars;

         var sqMax = Math.Min( // размер квадрата куда будет вписана звезда при 0°
               Width - Padding.LeftAndRight,
               Height - Padding.TopAndBottom);
         var sqMin = sqMax / (bigMaxStar ? 17 : 7); // размер квадрата куда будет вписана звезда при 360°
         var sqDiff = sqMax - sqMin;

         var centerMax = new PointDouble(Width / 2.0, Height / 2.0);
         var centerMin = new PointDouble(Padding.Left + sqMin / 2, Padding.Top + sqMin / 2);
         var centerDiff = new PointDouble(centerMax.X - centerMin.X, centerMax.Y - centerMin.Y);

         return Enumerable.Range(0, stars)
            .Select(starNum => {
               var angleStar = FixAngle(angle + starNum * anglePart);
               //angleAccumulative = Math.Sin((angle / 4).ToRadian()) * angleAccumulative; // accelerate / ускоряшка..

               var sq = angleStar * sqDiff / 360;
               // (un)comment next line to view result changes...
               sq = Math.Sin((angleStar / 4).ToRadian()) * sq; // accelerate / ускоряшка..
               sq = accelerateRevert
                     ? sqMin + sq
                     : sqMax - sq;

               double r1 = bigMaxStar ? sq * 2.2 : sq / 2; // external radius
               double r2 = r1 / 2.6; // internal radius

               var centerStar = new PointDouble(angleStar * centerDiff.X / 360,
                                                angleStar * centerDiff.Y / 360);
               // (un)comment next 2 lines to view result changes...
               centerStar.X = Math.Sin((angleStar / 4).ToRadian()) * centerStar.X; // accelerate / ускоряшка..
               centerStar.Y = Math.Sin((angleStar / 4).ToRadian()) * centerStar.Y; // accelerate / ускоряшка..
               centerStar.X = accelerateRevert
                     ? centerMin.X + centerStar.X
                     : centerMax.X - centerStar.X;
               centerStar.Y = accelerateRevert
                     ? centerMin.Y + centerStar.Y
                     : centerMax.Y - centerStar.Y;

               var clr = ForegroundColor;
               if (PolarLights) {
                  HSV hsv = new HSV(clr);
                  hsv.h += angleStar; // try: hsv.h -= angleStar;
                  clr = hsv.ToColor();
               }
               return new Tuple<double, Tuple<Color, IEnumerable<PointDouble>>>(sq, new Tuple<Color, IEnumerable<PointDouble>>(
                     clr,
                     FigureHelper.GetRegularStarCoords(rays,
                                                       r1, r2,
                                                       bigMaxStar ? centerMax : centerStar,
                                                       0 // try to view: angleAccumulative[0]
                                                    )));
            })
            .OrderBy(x => bigMaxStar ? -x.Item1 : x.Item1)
            .Select(x => x.Item2);
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords_SkillLevelAsValue() {
         var sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var r1 = sq/7; // external radius
         var r2 = sq/12; // internal radius

         var ordinal = MosaicSkill.Value.Ordinal();
         var rays = 5 + ordinal; // rays count
         var stars = 4 + ordinal; // number of stars on the perimeter of the circle

         var angle = RotateAngle;
         var angleAccumulative = angle;
         var anglePart = 360.0/stars;

         var center = new PointDouble(Width / 2.0, Height / 2.0);
         var zero = new PointDouble(0, 0);
         return Enumerable.Range(0, stars)
            .Select(starNum => {
               // (un)comment next line to view result changes...
               angleAccumulative = Math.Sin((angleAccumulative/4).ToRadian())*angleAccumulative; // accelerate / ускоряшка..

               // adding offset
               var offset = FigureHelper.GetPointOnCircle(sq/3, angleAccumulative + starNum * anglePart, zero);
               var centerStar = new PointDouble(center.X + offset.X, center.Y + offset.Y);

               var clr = ForegroundColor;
               if (PolarLights) {
                  HSV hsv = new HSV(clr);
                  hsv.h += starNum * anglePart;
                  clr = hsv.ToColor();
               }
               return new Tuple<Color, IEnumerable<PointDouble>>(clr, (MosaicSkill == ESkillLevel.eCustom)
                  ? FigureHelper.GetRegularPolygonCoords(3 + starNum % 4, r1, centerStar, -angleAccumulative)
                  : FigureHelper.GetRegularStarCoords(rays, r1, r2, centerStar, -angleAccumulative));
            })
            .Reverse(); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
      }

   }

}
