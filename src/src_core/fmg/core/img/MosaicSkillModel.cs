using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.core.types;

namespace fmg.core.img {

   /// <summary> MVC model of <see cref="ESkillLevel"/> representable as image </summary>
   public class MosaicSkillModel : AnimatedImageModel {

      private ESkillLevel _mosaicSkill;

      public MosaicSkillModel(ESkillLevel mosaicSkill) { _mosaicSkill = mosaicSkill; }

      public ESkillLevel MosaicSkill {
         get { return _mosaicSkill; }
         set { _notifier.setProperty(ref _mosaicSkill, value); }
      }


      public IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords => {
         return !_mosaicSkill.HasValue
            ? Coords_SkillLevelAsType
            : Coords_SkillLevelAsValue;
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords_SkillLevelAsType => {
         const bool bigMaxStar = !true; // true - большая звезда - вне картинки; false - большая звезда - внутри картинки.
         const bool accelerateRevert = !true; // ускорение под конец анимации, иначе - в начале...

         var rays = 5;
         var stars = bigMaxStar ? 6 : 8;
         var angle = RotateAngle;

         var pad = Padding;
         var sqMax = Math.Min( // размер квадрата куда будет вписана звезда при 0°
               Size.Width - pad.LeftAndRight,
               Size.Height - pad.TopAndBottom);
         var sqMin = sqMax / (bigMaxStar ? 17 : 7); // размер квадрата куда будет вписана звезда при 360°
         var sqExt = sqMax * 3;

         var centerMax = new PointDouble(pad.Left + (Size.Width  - pad.LeftAndRight) / 2.0,
                                         pad.Top  + (Size.Height - pad.TopAndBottom) / 2.0);
         var centerMin = new PointDouble(pad.Left + sqMin / 2, pad.Top + sqMin / 2);
         var centerExt = new PointDouble(Size.Width * 1.5, Size.Height * 1.5);

         return GetCoords_SkillLevelAsType_2(true, bigMaxStar, accelerateRevert, rays, stars / 2, angle, sqMin, sqMax, centerMin, centerMax)
            .Concat(
                GetCoords_SkillLevelAsType_2(false, bigMaxStar, accelerateRevert, rays, stars / 2, angle, sqMax, sqExt, centerMax, centerExt));
       //return GetCoords_SkillLevelAsType_2(false, bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMin, sqMax, centerMin, centerMax); // old
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords_SkillLevelAsType_2(
          bool accumulative,
          bool bigMaxStar,
          bool accelerateRevert,
          int rays,
          int stars,
          double angle,
          double sqMin,
          double sqMax,
          PointDouble centerMin,
          PointDouble centerMax
      ) {
         var angleAccumulative = angle;
         var anglePart = 360.0 / stars;
         var sqDiff = sqMax - sqMin;
         var centerDiff = new PointDouble(centerMax.X - centerMin.X, centerMax.Y - centerMin.Y);
         var fgClr = ForegroundColor;
         bool pl = PolarLights;

         return Enumerable.Range(0, stars)
            .Select(starNum => {
               var angleStar = FixAngle(angle + starNum * anglePart);
               if (accumulative)
                  angleAccumulative = Math.Sin((angle / 4).ToRadian()) * angleAccumulative; // accelerate / ускоряшка..

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

               var clr = !pl
                  ? fgClr
                  : new HSV(clr).AddHue(+angleStar).ToColor(); // try: -angleStar

               return new Tuple<double, Tuple<Color, IEnumerable<PointDouble>>>(sq, new Tuple<Color, IEnumerable<PointDouble>>(
                     clr,
                     FigureHelper.GetRegularStarCoords(rays,
                                                       r1, r2,
                                                       bigMaxStar ? centerMax : centerStar,
                                                       accumulative ? angleAccumulative : 0
                                                    )));
            })
            .OrderBy(x => bigMaxStar ? -x.Item1 : x.Item1)
            .Select(x => x.Item2);
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords_SkillLevelAsValue => {
         var pad = Padding;
         var sq = Math.Min( // size inner square
            Size.Width - pad.LeftAndRight,
            Size.Height - pad.TopAndBottom);
         var r1 = sq/7; // external radius
         var r2 = sq/12; // internal radius

         var skill = MosaicSkill;
         var ordinal = skill.Value.Ordinal();
         var rays = 5 + ordinal; // rays count
         var stars = 4 + ordinal; // number of stars on the perimeter of the circle

         var angle = RotateAngle;
         var angleAccumulative = angle;
         var anglePart = 360.0/stars;

         var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);
         var zero = new PointDouble(0, 0);
         var fgClr = ForegroundColor;
         bool pl = PolarLights;
         return Enumerable.Range(0, stars)
            .Select(starNum => {
               // (un)comment next line to view result changes...
               angleAccumulative = Math.Sin((angleAccumulative/4).ToRadian())*angleAccumulative; // accelerate / ускоряшка..

               // adding offset
               var offset = FigureHelper.GetPointOnCircle(sq/3, angleAccumulative + starNum * anglePart, zero);
               var centerStar = new PointDouble(center.X + offset.X, center.Y + offset.Y);

               var clr = !pl
                  ? fgClr
                  : new HSV(clr).AddHue(starNum * anglePart).ToColor();

               return new Tuple<Color, IEnumerable<PointDouble>>(clr, (skill == ESkillLevel.eCustom)
                  ? FigureHelper.GetRegularPolygonCoords(3 + starNum % 4, r1, centerStar, -angleAccumulative)
                  : FigureHelper.GetRegularStarCoords(rays, r1, r2, centerStar, -angleAccumulative));
            })
            .Reverse(); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
      }

   }

}
