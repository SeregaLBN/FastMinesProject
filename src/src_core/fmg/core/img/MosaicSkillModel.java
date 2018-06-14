package fmg.core.img;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.ESkillLevel;

/** MVC model of {@link ESkillLevel} representable as image */
public class MosaicSkillModel extends AnimatedImageModel {

   public static final String PROPERTY_MOSAIC_SKILL = "MosaicSkill";

   public MosaicSkillModel(ESkillLevel mosaicSkill) { _mosaicSkill = mosaicSkill; }

   private ESkillLevel _mosaicSkill;
   public ESkillLevel getMosaicSkill() { return _mosaicSkill; }
   public void setMosaicSkill(ESkillLevel value) { _notifier.setProperty(_mosaicSkill, value, PROPERTY_MOSAIC_SKILL); }


   public Stream<Pair<Color, Stream<PointDouble>>> getCoords() {
      return (getMosaicSkill() == null)
            ? getCoords_SkillLevelAsType()
            : getCoords_SkillLevelAsValue();
   }

   private Stream<Pair<Color, Stream<PointDouble>>> getCoords_SkillLevelAsType() {
      final boolean bigMaxStar = !true; // true - большая звезда - вне картинки; false - большая звезда - внутри картинки.
      final boolean accelerateRevert = !true; // ускорение под конец анимации, иначе - в начале...

      int rays = 5;
      int stars = bigMaxStar ? 6 : 8;
      double angle = getRotateAngle();

      BoundDouble pad = getPadding();
      double sqMax = Math.min( // размер квадрата куда будет вписана звезда при 0°
            getSize().width  - pad.getLeftAndRight(),
            getSize().height - pad.getTopAndBottom());
      double sqMin = 1;//sqMax / (bigMaxStar ? 17 : 7); // размер квадрата куда будет вписана звезда при 360°
      double sqExt = sqMax * 3;

      PointDouble centerMax = new PointDouble(pad.left + (getSize().width  - pad.getLeftAndRight()) / 2.0,
                                              pad.top  + (getSize().height - pad.getTopAndBottom()) / 2.0);
      PointDouble centerMin = new PointDouble(pad.left + sqMin/2, pad.top + sqMin/2);
      PointDouble centerExt = new PointDouble(getSize().width * 1.5, getSize().height * 1.5);

      return Stream.concat(
         getCoords_SkillLevelAsType_2(true , bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMin, sqMax, centerMin, centerMax),
         getCoords_SkillLevelAsType_2(false, bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMax, sqExt, centerMax, centerExt));
    //return getCoords_SkillLevelAsType_2(false, bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMin, sqMax, centerMin, centerMax); // old
   }
   private Stream<Pair<Color, Stream<PointDouble>>> getCoords_SkillLevelAsType_2(
         boolean accumulative,
         boolean bigMaxStar,
         boolean accelerateRevert,
         int rays,
         int stars,
         double angle,
         double sqMin,
         double sqMax,
         PointDouble centerMin,
         PointDouble centerMax
      )
   {
      double[] angleAccumulative = { angle };
      double anglePart = 360.0/stars;
      double sqDiff = sqMax - sqMin;
      PointDouble centerDiff = new PointDouble(centerMax.x - centerMin.x, centerMax.y - centerMin.y);
      Color fgClr = getForegroundColor();
      boolean pl = isPolarLights();
      Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, stars)
            .mapToObj(starNum -> {
               double angleStar = ImageModel.fixAngle(angle + starNum * anglePart);
               if (accumulative)
                  angleAccumulative[0] = Math.sin(FigureHelper.toRadian(angle/4))*angleAccumulative[0]; // accelerate / ускоряшка..

               double sq = angleStar * sqDiff / 360;
               // (un)comment next line to view result changes...
               sq = Math.sin(FigureHelper.toRadian(angleStar/4))*sq; // accelerate / ускоряшка..
               sq = accelerateRevert
                     ? sqMin + sq
                     : sqMax - sq;

               double r1 = bigMaxStar ? sq*2.2 : sq/2; // external radius
               double r2 = r1/2.6; // internal radius

               PointDouble centerStar = new PointDouble(angleStar * centerDiff.x / 360,
                                                        angleStar * centerDiff.y / 360);
               // (un)comment next 2 lines to view result changes...
               centerStar.x = Math.sin(FigureHelper.toRadian(angleStar/4))*centerStar.x; // accelerate / ускоряшка..
               centerStar.y = Math.sin(FigureHelper.toRadian(angleStar/4))*centerStar.y; // accelerate / ускоряшка..
               centerStar.x = accelerateRevert
                     ? centerMin.x + centerStar.x
                     : centerMax.x - centerStar.x;
               centerStar.y = accelerateRevert
                     ? centerMin.y + centerStar.y
                     : centerMax.y - centerStar.y;

               Color clr = !pl
                     ? fgClr
                     : new HSV(fgClr).addHue(+angleStar).toColor();// try: -angleStar

               return new Pair<>(sq, new Pair<>(
                     clr,
                     FigureHelper.getRegularStarCoords(rays,
                                                       r1, r2,
                                                       bigMaxStar ? centerMax : centerStar,
                                                       accumulative ? angleAccumulative[0] : 0
                                                    )));
            });

      List<Pair<Double, Pair<Color, Stream<PointDouble>>>> resL = res.collect(Collectors.toList());
      Collections.sort(resL, (o1, o2) -> {
         if (o1.first < o2.first) return bigMaxStar ?  1 : -1;
         if (o1.first > o2.first) return bigMaxStar ? -1 :  1;
         return 0;
      });
      return resL.stream().map(x -> x.second);
   }

   private Stream<Pair<Color, Stream<PointDouble>>> getCoords_SkillLevelAsValue() {
      BoundDouble pad = getPadding();
      double sq = Math.min( // size inner square
            getSize().width  - pad.getLeftAndRight(),
            getSize().height - pad.getTopAndBottom());
      double r1 = sq/7; // external radius
      double r2 = sq/12; // internal radius

      ESkillLevel skill = getMosaicSkill();
      int ordinal = skill.ordinal();
      int rays = 5 + ordinal; // rays count
      int stars = 4 + ordinal; // number of stars on the perimeter of the circle

      double angle = getRotateAngle();
      double[] angleAccumulative = { angle };
      double anglePart = 360.0/stars;

      final PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);
      final PointDouble zero = new PointDouble(0, 0);
      Color fgClr = getForegroundColor();
      boolean pl = isPolarLights();
      Stream<Pair<Color, Stream<PointDouble>>> res = IntStream.range(0, stars)
            .mapToObj(starNum -> {
               // (un)comment next line to view result changes...
               angleAccumulative[0] = Math.sin(FigureHelper.toRadian(angleAccumulative[0]/4))*angleAccumulative[0]; // accelerate / ускоряшка..

               // adding offset
               PointDouble offset = FigureHelper.getPointOnCircle(sq / 3, angleAccumulative[0] + starNum * anglePart, zero);
               PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

               Color clr = !pl
                     ? fgClr
                     : new HSV(fgClr).addHue(starNum * anglePart).toColor();

               return new Pair<>(clr, (skill == ESkillLevel.eCustom)
                     ? FigureHelper.getRegularPolygonCoords(3 + (starNum % 4), r1, centerStar, -angleAccumulative[0])
                     : FigureHelper.getRegularStarCoords(rays, r1, r2, centerStar, -angleAccumulative[0]));
            });
      List<Pair<Color, Stream<PointDouble>>> resL = res.collect(Collectors.toList());
      Collections.reverse(resL); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
      return resL.stream();
   }

}
