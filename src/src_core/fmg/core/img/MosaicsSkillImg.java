package fmg.core.img;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.data.controller.types.ESkillLevel;

/**
 * Representable {@link fmg.data.controller.types.ESkillLevel} as image
 *
 * @param <TImage> plaform specific image
 */
public abstract class MosaicsSkillImg<TImage> extends RotatedImg<ESkillLevel, TImage> {

   protected MosaicsSkillImg(ESkillLevel skill) { super(skill); }
   protected MosaicsSkillImg(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
   protected MosaicsSkillImg(ESkillLevel skill, int widthAndHeight, int padding) { super(skill, widthAndHeight, padding); }

   public ESkillLevel getMosaicSkill() { return getEntity(); }
   public void setMosaicSkill(ESkillLevel value) { setEntity(value); }

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
      return IntStream.range(0, stars)
            .mapToObj(st -> {
               Stream<PointDouble> points = (getMosaicSkill() == ESkillLevel.eCustom)
                     ? FigureHelper.getRegularPolygonCoords(3 + (st % 4), r1, -angle[0])
                     : FigureHelper.getRegularStarCoords(rays, r1, r2, -angle[0]);

               // (un)comment next line to view result changes...
               angle[0] = Math.sin(FigureHelper.toRadian(angle[0]/4))*angle[0]; // accelerate / ускоряшка..

               // adding offset
               PointDouble offset = FigureHelper.getPointOnCircle(sq / 3, angle[0] + (st * starAngle));
               offset.x += getWidth() / 2.0;
               offset.y += getHeight() / 2.0;
               return points.map(p -> {
                  p.x += offset.x;
                  p.y += offset.y;
                  return p;
               });
            });
   }

}
