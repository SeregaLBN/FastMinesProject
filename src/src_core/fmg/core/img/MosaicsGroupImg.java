package fmg.core.img;

import java.util.stream.Stream;

import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.EMosaicGroup;

/**
 * Representable {@link fmg.core.types.EMosaicGroup} as image
 *
 * @param <TImage> plaform specific image
 */
public abstract class MosaicsGroupImg<TImage> extends PolarLightsImg<EMosaicGroup, TImage> {

   public MosaicsGroupImg(EMosaicGroup group) { super(group); }
   public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight) { super(group, widthAndHeight); }
   public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight, Integer padding) { super(group, widthAndHeight, padding); }

   public EMosaicGroup getMosaicGroup() { return getEntity(); }
   public void setMosaicGroup(EMosaicGroup value) { setEntity(value); }

   protected Stream<PointDouble> getCoords() {
      double sq = Math.min( // size inner square
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      int vertices = 3 + getMosaicGroup().ordinal(); // vertices count
      Stream<PointDouble> points = (getMosaicGroup() != EMosaicGroup.eOthers)
            ? FigureHelper.getRegularPolygonCoords(vertices, sq/2, getRotateAngle())
            : FigureHelper.getRegularStarCoords(4, sq/2, sq/5, getRotateAngle());

      // adding offset
      double offsetX = getWidth() / 2.0;
      double offsetY = getHeight() / 2.0;
      return points.map(p -> {
         p.x += offsetX;
         p.y += offsetY;
         return p;
      });
   }

}
