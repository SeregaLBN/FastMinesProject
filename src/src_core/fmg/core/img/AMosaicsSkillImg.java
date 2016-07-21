package fmg.core.img;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.data.controller.types.ESkillLevel;

/**
 * Abstract representable {@link fmg.data.controller.types.ESkillLevel} as image
 *
 * @param <TImage> plaform specific image
 */
public abstract class AMosaicsSkillImg<TImage> extends RotatedImg<TImage> {

   protected AMosaicsSkillImg(ESkillLevel skill) {
      _mosaicSkill = skill;
   }

   public static final String PROPERTY_MOSAIC_SKILL = "MosaicSkill";

   private ESkillLevel _mosaicSkill;
   public ESkillLevel getMosaicSkill() { return _mosaicSkill; }
   public void setMosaicSkill(ESkillLevel value) { setProperty(_mosaicSkill, value, PROPERTY_MOSAIC_SKILL); }

   protected Stream<Stream<PointDouble>> getCoords() {
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
            .mapToObj(st -> {
               // (un)comment next line to view result changes...
               angle[0] = Math.sin(FigureHelper.toRadian(angle[0]/4))*angle[0]; // accelerate / ускоряшка..

               // adding offset
               PointDouble offset = FigureHelper.getPointOnCircle(sq / 3, angle[0] + (st * starAngle), zero);
               PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

               return (getMosaicSkill() == ESkillLevel.eCustom)
                     ? FigureHelper.getRegularPolygonCoords(3 + (st % 4), r1, centerStar, -angle[0])
                     : FigureHelper.getRegularStarCoords(rays, r1, r2, centerStar, -angle[0]);
            });
      List<Stream<PointDouble>> resL = res.collect(Collectors.toList());
      Collections.reverse(resL); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
      return resL.stream();
   }

}
