package fmg.core.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.EMosaicGroup;

/**
 * Abstract representable {@link fmg.core.types.EMosaicGroup} as image
 * @param <TImage> plaform specific image
 */
public abstract class AMosaicsGroupImg<TImage> extends PolarLightsImg<TImage> {

   protected AMosaicsGroupImg(EMosaicGroup group) {
      _mosaicGroup = group;
   }

   public static final String PROPERTY_MOSAIC_GROUP = "MosaicGroup";

   private EMosaicGroup _mosaicGroup;
   public EMosaicGroup getMosaicGroup() { return _mosaicGroup; }
   public void setMosaicGroup(EMosaicGroup value) { setProperty(_mosaicGroup, value, PROPERTY_MOSAIC_GROUP); }

   protected Stream<PointDouble> getCoords() {
      double sq = Math.min( // size inner square
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      int vertices = 3 + getMosaicGroup().ordinal(); // vertices count
      PointDouble center = new PointDouble(getWidth() / 2.0, getHeight() / 2.0);

      double ra = getRotateAngle();
      if (getMosaicGroup() != EMosaicGroup.eOthers)
         return FigureHelper.getRegularPolygonCoords(vertices, sq/2, center, ra);

      return FigureHelper.getRegularStarCoords(4, sq/2, sq/5, center, ra);

    //return                           FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, getRotateAngle(), ra);
    //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, getRotateAngle(), 0), 2, center, ra);

    //return                           FigureHelper.getFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, getRotateAngle(), ra);
    //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, getRotateAngle(), 0), 2, center, ra);

    //Pair<Integer, Integer> nm = getNM(_nmIndex1);
    //return                           FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm.first, nm.second, sq / 2, center, _incrementSpeedAngle, ra);
    //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm.first, nm.second, sq / 2, center, _incrementSpeedAngle, 0), 2, center, ra);
   }

   protected Pair<Stream<PointDouble>, Stream<PointDouble>> getDoubleCoords() {
      double sq = Math.min( // size inner square
         getWidth() - getPadding().getLeftAndRight(),
         getHeight() - getPadding().getTopAndBottom());
      PointDouble center = new PointDouble(getWidth() / 2.0, getHeight() / 2.0);


      Pair<Integer, Integer> nm1 = getNM(_nmIndex1);
      Pair<Integer, Integer> nm2 = getNM(_nmIndex2);
      double isa = _incrementSpeedAngle;
      double ra = getRotateAngle();
      int sideNum = 2;
      double radius = sq / 3.7; // подобрал.., чтобы не вылазило за периметр изображения
      double sizeSide = sq / 3.5; // подобрал.., чтобы не вылазило за периметр изображения

      final boolean byRadius = false;
      // высчитываю координаты двух фигур.
      // с одинаковым размером одной из граней.
      Stream<PointDouble> resS1 = byRadius
            ? FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm1.first, nm1.second, radius, center, isa, 0)
            : FigureHelper.getFlowingToTheRightPolygonCoordsBySide(nm1.first, nm1.second, sizeSide, sideNum, center, isa, 0);
      List<PointDouble> res1 = FigureHelper.rotateBySide(resS1, sideNum, center, ra)
            .collect(Collectors.toList());
      Stream<PointDouble> resS2 = byRadius
            ? FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm2.first, nm2.second, radius, center, isa, 0)
            : FigureHelper.getFlowingToTheRightPolygonCoordsBySide(nm2.first, nm2.second, sizeSide, sideNum, center, isa, 0);
      List<PointDouble> res2 = FigureHelper.rotateBySide(resS2, sideNum, center, ra+180) // +180° - разворачиваю вторую фигуру, чтобы не пересекалась с первой фигурой
            .collect(Collectors.toList());

      // и склеиваю грани:
      //  * нахожу середины граней
      PointDouble p11 = res1.get(sideNum - 1); PointDouble p12 = res1.get(sideNum);
      PointDouble p21 = res2.get(sideNum - 1); PointDouble p22 = res2.get(sideNum);
      PointDouble centerPoint1 = new PointDouble((p11.x + p12.x) / 2, (p11.y + p12.y) / 2);
      PointDouble centerPoint2 = new PointDouble((p21.x + p22.x) / 2, (p21.y + p22.y) / 2);

      //  * и совмещаю их по центру изображения
      PointDouble offsetToCenter1 = new PointDouble(center.x - centerPoint1.x, center.y - centerPoint1.y);
      PointDouble offsetToCenter2 = new PointDouble(center.x - centerPoint2.x, center.y - centerPoint2.y);
      return new Pair<Stream<PointDouble>, Stream<PointDouble>>(
            FigureHelper.move(res1.stream(), offsetToCenter1),
            FigureHelper.move(res2.stream(), offsetToCenter2)
         );
   }

   private final int[] _nmArray = { 3, 4, 6 }; //  triangle -> quadrangle -> hexagon -> anew triangle -> ...
   private int _nmIndex1 = 0, _nmIndex2 = 1;
   private double _incrementSpeedAngle;

   private Pair<Integer, Integer> getNM(int index) {
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
      return new Pair<Integer, Integer>(n, m);
   }

   @Override
   protected void onTimer() {
      if (isRotate()) {
         boolean castling = false;
         double incrementSpeedAngle = _incrementSpeedAngle + 3*getRotateAngleDelta();
         if (incrementSpeedAngle >= 360) {
            incrementSpeedAngle -= 360;
            castling = true;
         } else {
            if (incrementSpeedAngle < 0) {
               incrementSpeedAngle += 360;
               castling = true;
            }
         }
         _incrementSpeedAngle = incrementSpeedAngle;
         if (castling) {
            _nmIndex1 = ++_nmIndex1 % _nmArray.length;
            _nmIndex2 = ++_nmIndex2 % _nmArray.length;
         }
      }
      super.onTimer();
   }

}
