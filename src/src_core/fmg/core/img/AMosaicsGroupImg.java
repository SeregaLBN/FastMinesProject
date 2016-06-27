package fmg.core.img;

import java.util.stream.Stream;

import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.EMosaicGroup;

/**
 * Abstract representable {@link fmg.core.types.EMosaicGroup} as image
 *
 * @param <TImage> plaform specific image
 */
public abstract class AMosaicsGroupImg<TImage> extends PolarLightsImg<EMosaicGroup, TImage> {

   protected AMosaicsGroupImg(EMosaicGroup group) { super(group); }

   public EMosaicGroup getMosaicGroup() { return getEntity(); }
   public void setMosaicGroup(EMosaicGroup value) { setEntity(value); }

   protected Stream<PointDouble> getCoords() {
      double sq = Math.min( // size inner square
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      int vertices = 3 + getMosaicGroup().ordinal(); // vertices count
      PointDouble center = new PointDouble(getWidth() / 2.0, getHeight() / 2.0);
      return (getMosaicGroup() != EMosaicGroup.eOthers)
            ? FigureHelper.getRegularPolygonCoords(vertices, sq/2, center, getRotateAngle())
            : FigureHelper.getRegularStarCoords(4, sq/2, sq/5, center, getRotateAngle());
   }

}
