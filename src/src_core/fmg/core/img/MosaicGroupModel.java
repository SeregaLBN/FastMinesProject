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
import fmg.core.types.EMosaicGroup;

/** MVC model of {@link EMosaicGroup} representable as image */
public class MosaicGroupModel extends AnimatedImageModel {

   public static final String PROPERTY_MOSAIC_GROUP = "MosaicGroup";

   public MosaicGroupModel(EMosaicGroup mosaicGroup) { _mosaicGroup = mosaicGroup; }

   public static boolean varMosaicGroupAsValueOthers1 = !true;

   private EMosaicGroup _mosaicGroup;
   public EMosaicGroup getMosaicGroup() { return _mosaicGroup; }
   public void setMosaicGroup(EMosaicGroup value) { _notifier.setProperty(_mosaicGroup, value, PROPERTY_MOSAIC_GROUP); }

   /**  triangle -> quadrangle -> hexagon -> anew triangle -> ... */
   private final int[] _nmArray = { 3, 4, 6 };
   private int _nmIndex1 = 0, _nmIndex2 = 1;
   private double _incrementSpeedAngle;

   public int[] getNmArray() { return _nmArray; }

   public double getIncrementSpeedAngle() { return _incrementSpeedAngle; }
   public void setIncrementSpeedAngle(double incrementSpeedAngle) { _incrementSpeedAngle = incrementSpeedAngle; }

   public int getNmIndex1() { return _nmIndex1; }
   public void setNmIndex1(int nmIndex1) { _nmIndex1 = nmIndex1; }

   public int getNmIndex2() { return _nmIndex2; }
   public void setNmIndex2(int nmIndex2) { _nmIndex2 = nmIndex2; }


   public Stream<Pair<Color, Stream<PointDouble>>> getCoords() {
      EMosaicGroup mosaicGroup = getMosaicGroup();
      return (mosaicGroup == null)
            ? getCoords_MosaicGroupAsType()
            : (mosaicGroup != EMosaicGroup.eOthers)
               ? Stream.of(new Pair<>(getForegroundColor(), getCoords_MosaicGroupAsValue()))
               : MosaicGroupModel.varMosaicGroupAsValueOthers1
                  ? getCoords_MosaicGroupAsValueOthers1()
                  : getCoords_MosaicGroupAsValueOthers2();
   }

   private Stream<PointDouble> getCoords_MosaicGroupAsValue() {
      BoundDouble pad = getPadding();
      double sq = Math.min( // size inner square
            getSize().width  - pad.getLeftAndRight(),
            getSize().height - pad.getTopAndBottom());
      EMosaicGroup mosaicGroup = getMosaicGroup();
      int vertices = 3 + mosaicGroup.ordinal(); // vertices count
      PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);

      double ra = getRotateAngle();
      if (mosaicGroup != EMosaicGroup.eOthers)
         return FigureHelper.getRegularPolygonCoords(vertices, sq/2, center, ra);

      return FigureHelper.getRegularStarCoords(4, sq/2, sq/5, center, ra);

    //return                           FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, ra, ra);
    //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, ra, 0), 2, center, ra);

    //return                           FigureHelper.getFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, ra, ra);
    //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, ra, 0), 2, center, ra);

    //Pair<Integer, Integer> nm = getNM(getNmIndex1());
    //return                           FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm.first, nm.second, sq / 2, center, getIncrementSpeedAngle(), ra);
    //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm.first, nm.second, sq / 2, center, getIncrementSpeedAngle(), 0), 2, center, ra);
   }

   private Stream<Pair<Color, Stream<PointDouble>>> getCoords_MosaicGroupAsValueOthers1() {
      BoundDouble pad = getPadding();
      double sq = Math.min( // size inner square
         getSize().width  - pad.getLeftAndRight(),
         getSize().height - pad.getTopAndBottom());
      PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);


      Pair<Integer, Integer> nm1 = getNM(getNmIndex1());
      Pair<Integer, Integer> nm2 = getNM(getNmIndex2());
      double isa = getIncrementSpeedAngle();
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
      Color fgClr = getForegroundColor();
      boolean pl = isPolarLights();
      return Stream.of(
            new Pair<>(        fgClr                       , FigureHelper.move(res1.stream(), offsetToCenter1)),
            new Pair<>(pl ?
                       new HSV(fgClr).addHue(180).toColor()
                       :       fgClr                       , FigureHelper.move(res2.stream(), offsetToCenter2))
         );
   }

   private Pair<Integer, Integer> getNM(int index) {
      int[] nmArray = getNmArray();
      int n = nmArray[index];
      int m = nmArray[(index + 1) % nmArray.length];

      // Во вторую половину вращения фиксирую значение N равно M.
      // Т.к. в прервую половину, с 0 до 180, N стремится к M - см. описание FigureHelper.getFlowingToTheRightPolygonCoordsByXxx...
      // Т.е. при значении 180 значение N уже достигло M.
      // Фиксирую для того, чтобы при следующем инкременте параметра index, значение N не менялось. Т.о. обеспечиваю плавность анимации.
      if (getIncrementSpeedAngle() >= 180) {
         if (getAnimeDirection())
            n = m;
      } else {
         if (!getAnimeDirection())
            n = m;
      }
      return new Pair<>(n, m);
   }

   private Stream<Pair<Color, Stream<PointDouble>>> getCoords_MosaicGroupAsType() {
      final boolean accelerateRevert = true; // ускорение под конец анимации, иначе - в начале...

      int shapes = 4; // 3х-, 4х-, 5ти- и 6ти-угольники

      double angle = getRotateAngle();
    //double[] angleAccumulative = { angle };
      double anglePart = 360.0/shapes;

      BoundDouble pad = getPadding();
      double sqMax = Math.min( // размер квадрата куда будет вписана фигура при 0°
            getSize().width  - pad.getLeftAndRight(),
            getSize().height - pad.getTopAndBottom());
      double sqMin = sqMax / 7; // размер квадрата куда будет вписана фигура при 360°
      double sqDiff = sqMax - sqMin;

      PointDouble center = new PointDouble(pad.left + (getSize().width  - pad.getLeftAndRight()) / 2.0,
                                           pad.top  + (getSize().height - pad.getTopAndBottom()) / 2.0);

      Color fgClr = getForegroundColor();
      boolean pl = isPolarLights();
      Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, shapes)
            .mapToObj(shapeNum -> {
               int vertices = 3+shapeNum;
               double angleShape = ImageModel.fixAngle(angle + shapeNum * anglePart);
             //angleAccumulative[0] = Math.sin(FigureHelper.toRadian(angle/4))*angleAccumulative[0]; // accelerate / ускоряшка..

               double sq = angleShape * sqDiff / 360;
               // (un)comment next line to view result changes...
               sq = Math.sin(FigureHelper.toRadian(angleShape/4))*sq; // accelerate / ускоряшка..
               sq = accelerateRevert
                     ? sqMin + sq
                     : sqMax - sq;

               double radius = sq/1.8;

               Color clr = !pl
                     ? fgClr
                     : new HSV(fgClr).addHue(+angleShape).toColor(); // try: -angleShape

               return new Pair<>(sq, new Pair<>(
                     clr,
                     FigureHelper.getRegularPolygonCoords(vertices,
                                                          radius,
                                                          center,
                                                          45 // try to view: angleAccumulative[0]
                                                          )));
            });

      List<Pair<Double, Pair<Color, Stream<PointDouble>>>> resL = res.collect(Collectors.toList());
      Collections.sort(resL, (o1, o2) -> {
         if (o1.first < o2.first) return 1;
         if (o1.first > o2.first) return -1;
         return 0;
      });
      return resL.stream().map(x -> x.second);
   }

   private Stream<Pair<Color, Stream<PointDouble>>> getCoords_MosaicGroupAsValueOthers2() {
      BoundDouble pad = getPadding();
      double sq = Math.min( // size inner square
            getSize().width  - pad.getLeftAndRight(),
            getSize().height - pad.getTopAndBottom());
      double radius = sq/2.7;

      int shapes = 3; // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур: треугольники, квадраты и шестигранники

      double angle = getRotateAngle();
      double anglePart = 360.0/shapes;

      final PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);
      final PointDouble zero = new PointDouble(0, 0);
      Color fgClr = getForegroundColor();
      boolean pl = isPolarLights();
      Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, shapes)
            .mapToObj(shapeNum -> {
               double angleShape = angle*shapeNum;

               // adding offset
               PointDouble offset = FigureHelper.getPointOnCircle(sq / 5, angleShape + shapeNum * anglePart, zero);
               PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

               Color clr = !pl
                     ? fgClr
                     : new HSV(fgClr).addHue(shapeNum * anglePart).toColor();

               int vertices;
               switch (shapeNum) { // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур:
               case 0: vertices = 6; break; // шестигранники
               case 1: vertices = 4; break; // квадраты
               case 2: vertices = 3; break; // и треугольники
               default: throw new RuntimeException();
               }
               return new Pair<>(
                     1.0, // const value (no sorting). Provided for the future...
                     new Pair<>(
                        clr,
                        FigureHelper.getRegularPolygonCoords(vertices, radius, centerStar, -angle)));
            });

      List<Pair<Double, Pair<Color, Stream<PointDouble>>>> resL = res.collect(Collectors.toList());
      Collections.sort(resL, (o1, o2) -> {
         if (o1.first < o2.first) return 1;
         if (o1.first > o2.first) return -1;
         return 0;
      });
      return resL.stream().map(x -> x.second);
   }

}
