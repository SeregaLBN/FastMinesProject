package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/** MVC model of {@link EMosaicGroup} representable as image */
public class MosaicsGroupModel extends ImageProperties {

   public static final String PROPERTY_MOSAIC_GROUP = "MosaicGroup";

   public MosaicsGroupModel() {}
   public MosaicsGroupModel(EMosaicGroup mosaicGroup) { _mosaicGroup = mosaicGroup; }

   private EMosaicGroup _mosaicGroup;
   public EMosaicGroup getMosaicGroup() { return _mosaicGroup; }
   public void setMosaicGroup(EMosaicGroup value) { setProperty(_mosaicGroup, value, PROPERTY_MOSAIC_GROUP); }

   /**  triangle -> quadrangle -> hexagon -> anew triangle -> ... *    /
   private final int[] _nmArray = { 3, 4, 6 };
   private int _nmIndex1 = 0, _nmIndex2 = 1;
   private double _incrementSpeedAngle;

   public Pair<Integer, Integer> getNM(int index) {
      int n = _nmArray[index];
      int m = _nmArray[(index + 1) % _nmArray.length];

      // Во вторую половину вращения фиксирую значение N равно M.
      // Т.к. в прервую половину, с 0 до 180, N стремится к M - см. описание FigureHelper.getFlowingToTheRightPolygonCoordsByXxx...
      // Т.е. при значении 180 значение N уже достигло M.
      // Фиксирую для того, чтобы при следующем инкременте параметра index, значение N не менялось. Т.о. обеспечиваю плавность анимации.
      if (_incrementSpeedAngle >= 180) {
         if (getRotateAngleDelta() > 0)
            n = m;
      } else {
         if (getRotateAngleDelta() < 0)
            n = m;
      }
      return new Pair<>(n, m);
   }

   public double getIncrementSpeedAngle() { return _incrementSpeedAngle; }
   public void setIncrementSpeedAngle(double incrementSpeedAngle) { _incrementSpeedAngle = incrementSpeedAngle; }

   public int getNmIndex1() { return _nmIndex1; }
   public void setNmIndex1(int nmIndex1) { _nmIndex1 = nmIndex1; }

   public int getNmIndex2() { return _nmIndex2; }
   public void setNmIndex2(int nmIndex2) { _nmIndex2 = nmIndex2; }
   */

}
