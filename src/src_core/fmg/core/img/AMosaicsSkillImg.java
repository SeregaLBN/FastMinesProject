package fmg.core.img;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.data.controller.types.ESkillLevel;

/**
 * Abstract representable {@link fmg.data.controller.types.ESkillLevel} as image
 *
 * @param <TImage> plaform specific image
 */
public abstract class AMosaicsSkillImg<TImage> extends PolarLightsImg<TImage> {

   /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
   protected AMosaicsSkillImg(ESkillLevel skill) {
      _mosaicSkill = skill;
   }

   public static final String PROPERTY_MOSAIC_SKILL = "MosaicSkill";

   private ESkillLevel _mosaicSkill;
   public ESkillLevel getMosaicSkill() { return _mosaicSkill; }
   public void setMosaicSkill(ESkillLevel value) { setProperty(_mosaicSkill, value, PROPERTY_MOSAIC_SKILL); }

   protected Stream<Stream<PointDouble>> getCoords() {
      return (_mosaicSkill == null)
            ? getCoords_SkillLevelAsType()
            : getCoords_SkillLevelAsValue();
   }

   protected Stream<Stream<PointDouble>> getCoords_SkillLevelAsType() {
      final boolean bigMaxStar = !true; // true - большая звезда - вне картинки; false - большая звезда - внутри картинки.
      final boolean accelerateRevert = !true; // ускорение под конец анимации, иначе - в начале...

      int rays = 5;
      int stars = bigMaxStar ? 6 : 4;

      double angle = getRotateAngle();
      double anglePart = 360.0/stars;

      double sqMax = Math.min( // размер квадрата куда будет вписана звезда при 0°
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      double sqMin = sqMax / (bigMaxStar ? 17 : 7); // размер квадрата куда будет вписана звезда при 360°
      double sqDiff = sqMax - sqMin;

      PointDouble centerMax = new PointDouble(getWidth() / 2.0, getHeight() / 2.0);
      PointDouble centerMin = new PointDouble(getPadding().left + sqMin/2, getPadding().top + sqMin/2);
      PointDouble centerDiff = new PointDouble(centerMax.x - centerMin.x, centerMax.y - centerMin.y);

      Stream<Pair<Double, Stream<PointDouble>>> res = IntStream.range(0, stars)
            .mapToObj(star -> {
               double angleStar = fixAngle(angle + star * anglePart);

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

               return new Pair<>(sq, FigureHelper.getRegularStarCoords(rays, r1, r2, bigMaxStar ? centerMax : centerStar, 0));
            });

      List<Pair<Double, Stream<PointDouble>>> resL = res.collect(Collectors.toList());
      Collections.sort(resL, (o1, o2) -> {
         if (o1.first < o2.first) return bigMaxStar ?  1 : -1;
         if (o1.first > o2.first) return bigMaxStar ? -1 :  1;
         return 0;
      });
      return resL.stream().map(x -> x.second);
   }

   protected Stream<Stream<PointDouble>> getCoords_SkillLevelAsValue() {
      double sq = Math.min( // size inner square
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      double r1 = sq/7; // external radius
      double r2 = sq/12; // internal radius
      int ordinal = getMosaicSkill().ordinal();
      int rays = 5 + ordinal; // rays count
      int stars = 4 + ordinal; // number of stars on the perimeter of the circle
      double[] angle = { getRotateAngle() };
      double starAngle = 360.0/stars;
      final PointDouble center = new PointDouble(getWidth() / 2.0, getHeight() / 2.0);
      final PointDouble zero = new PointDouble(0, 0);
      Stream<Stream<PointDouble>> res = IntStream.range(0, stars)
            .mapToObj(star -> {
               // (un)comment next line to view result changes...
               angle[0] = Math.sin(FigureHelper.toRadian(angle[0]/4))*angle[0]; // accelerate / ускоряшка..

               // adding offset
               PointDouble offset = FigureHelper.getPointOnCircle(sq / 3, angle[0] + (star * starAngle), zero);
               PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

               return (getMosaicSkill() == ESkillLevel.eCustom)
                     ? FigureHelper.getRegularPolygonCoords(3 + (star % 4), r1, centerStar, -angle[0])
                     : FigureHelper.getRegularStarCoords(rays, r1, r2, centerStar, -angle[0]);
            });
      List<Stream<PointDouble>> resL = res.collect(Collectors.toList());
      Collections.reverse(resL); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
      return resL.stream();
   }

}
