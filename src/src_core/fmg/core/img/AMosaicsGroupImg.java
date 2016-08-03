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

      if (getMosaicGroup() != EMosaicGroup.eOthers)
         return FigureHelper.getRegularPolygonCoords(vertices, sq/2, center, getRotateAngle());

      //return FigureHelper.getRegularStarCoords(4, sq/2, sq/5, center, getRotateAngle());
      //return FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices + 1, sq / 2, center, RotateAngle, RotateAngle);
      //return FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices + 1, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, 0);
      int m = _nmArray[(_nmIndex1+1) % _nmArray.length];
      int n = (_incrementSpeedAngle >= 180) ? m : _nmArray[_nmIndex1];
      return FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(n, m, sq / 2, center, _incrementSpeedAngle, 0);//.RotateBySide(2, center, 0);
   }

   protected Pair<Stream<PointDouble>, Stream<PointDouble>> getDoubleCoords() {
      double sq = Math.min( // size inner square
         getWidth() - getPadding().getLeftAndRight(),
         getHeight() - getPadding().getTopAndBottom());
      PointDouble center = new PointDouble(getWidth() / 2.0, getHeight() / 2.0);


      double isa = _incrementSpeedAngle;
      int m1 = _nmArray[(_nmIndex1 + 1) % _nmArray.length];
      int n1 = (isa >= 180) ? m1 : _nmArray[_nmIndex1];
      int m2 = _nmArray[(_nmIndex2 + 1) % _nmArray.length];
      int n2 = (isa >= 180) ? m2 : _nmArray[_nmIndex2];
      double ra = getRotateAngle();

      int sideNum = 2;
      double sizeSide = sq / 3.5; // подобрал.., чтобы не вылазило за периметр изображения

      // высчитываю координаты двух фигур.
      // с одинаковым размером одной из граней.
      Stream<PointDouble> resS1 = FigureHelper.getFlowingToTheRightPolygonCoordsBySide(n1, m1, sizeSide, sideNum, center, isa, 0);
      List<PointDouble> res1 = FigureHelper.rotateBySide(resS1, sideNum, center, ra)
            .collect(Collectors.toList());
      Stream<PointDouble> resS2 = FigureHelper.getFlowingToTheRightPolygonCoordsBySide(n2, m2, sizeSide, sideNum, center, isa, 0);
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
