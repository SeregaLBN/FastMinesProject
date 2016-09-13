using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.core.types;

namespace fmg.core.img {

   /// <summary> Abstract representable <see cref="EMosaicGroup"/> as image </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class AMosaicsGroupImg<TImage> : BurgerMenuImg<TImage>
      where TImage : class
   {
      /// <param name="skill">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
      protected AMosaicsGroupImg(EMosaicGroup? group) {
         _mosaicGroup = group;
         ShowBurgerMenu = (group == null);
         LayersInBurgerMenu = 3;
         HorizontalBurgerMenu  = !true;
         RotateBurgerMenu = true;
      }

      private EMosaicGroup? _mosaicGroup;
      public EMosaicGroup? MosaicGroup {
         get { return _mosaicGroup; }
         set { SetProperty(ref _mosaicGroup, value); }
      }

      private const bool varMosaicGroupAsValueOthers1 = !true;
      protected IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords() {
         return (_mosaicGroup == null)
               ? GetCoords_MosaicGroupAsType()
               : (_mosaicGroup != EMosaicGroup.eOthers)
                  ? new Tuple<Color, IEnumerable<PointDouble>>[] { new Tuple<Color, IEnumerable<PointDouble>>(ForegroundColor, GetCoords_MosaicGroupAsValue()) }
                  : varMosaicGroupAsValueOthers1
                     ? GetCoords_MosaicGroupAsValueOthers1()
                     : GetCoords_MosaicGroupAsValueOthers2();
      }

      private IEnumerable<PointDouble> GetCoords_MosaicGroupAsValue() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var vertices = 3 + MosaicGroup.Value.Ordinal(); // verticles count
         var center = new PointDouble(Width / 2.0, Height / 2.0);

         var ra = RotateAngle;
         if (MosaicGroup != EMosaicGroup.eOthers)
            return FigureHelper.GetRegularPolygonCoords(vertices, sq / 2, center, ra);

         return FigureHelper.GetRegularStarCoords(4, sq / 2, sq / 5, center, ra);

         //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, RotateAngle, ra);
         //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, ra);

         //return FigureHelper.GetFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, RotateAngle, ra);
         //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 3.5, 2, center, RotateAngle, 0).RotateBySide(2, center, ra);

         //var nm = GetNM(_nmIndex1);
         //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(nm.Item1, nm.Item2, sq / 2, center, _incrementSpeedAngle, 0).RotateBySide(2, center, ra);
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords_MosaicGroupAsValueOthers1() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var center = new PointDouble(Width / 2.0, Height / 2.0);


         var nm1 = GetNM(_nmIndex1);
         var nm2 = GetNM(_nmIndex2);
         var isa = _incrementSpeedAngle;
         var ra = RotateAngle;
         var sideNum = 2;
         var radius = sq / 3.7; // подобрал.., чтобы не вылазило за периметр изображения
         var sizeSide = sq / 3.5; // подобрал.., чтобы не вылазило за периметр изображения

         const bool byRadius = false;
         // высчитываю координаты двух фигур.
         // с одинаковым размером одной из граней.
         var res1 = (byRadius
                     ? FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(nm1.Item1, nm1.Item2, radius, center, isa, 0)
                     : FigureHelper.GetFlowingToTheRightPolygonCoordsBySide(nm1.Item1, nm1.Item2, sizeSide, sideNum, center, isa, 0))
                  .RotateBySide(sideNum, center, ra)
                  .ToList();
         var res2 = (byRadius
                     ? FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(nm2.Item1, nm2.Item2, radius, center, isa, 0)
                     : FigureHelper.GetFlowingToTheRightPolygonCoordsBySide(nm2.Item1, nm2.Item2, sizeSide, sideNum, center, isa, 0))
                  .RotateBySide(sideNum, center, ra+180) // +180° - разворачиваю вторую фигуру, чтобы не пересекалась с первой фигурой
                  .ToList();

         // и склеиваю грани:
         //  * нахожу середины граней
         var p11 = res1[sideNum - 1]; var p12 = res1[sideNum];
         var p21 = res2[sideNum - 1]; var p22 = res2[sideNum];
         var centerPoint1 = new PointDouble((p11.X + p12.X) / 2, (p11.Y + p12.Y) / 2);
         var centerPoint2 = new PointDouble((p21.X + p22.X) / 2, (p21.Y + p22.Y) / 2);

         //  * и совмещаю их по центру изображения
         var offsetToCenter1 = new PointDouble(center.X - centerPoint1.X, center.Y - centerPoint1.Y);
         var offsetToCenter2 = new PointDouble(center.X - centerPoint2.X, center.Y - centerPoint2.Y);
         return new Tuple<Color, IEnumerable<PointDouble>>[] {
               new Tuple<Color, IEnumerable<PointDouble>>(        ForegroundColor                       , res1.Move(offsetToCenter1)),
               new Tuple<Color, IEnumerable<PointDouble>>(PolarLights ?
                                                          new HSV(ForegroundColor).AddHue(180).ToColor()
                                                          :       ForegroundColor                       , res2.Move(offsetToCenter2)),
            };
      }

      private readonly int[] _nmArray = { 3, 4, 6 }; //  triangle -> quadrangle -> hexagon -> anew triangle -> ...
      private int _nmIndex1 = 0, _nmIndex2 = 1;
      private double _incrementSpeedAngle;

      private Tuple<int, int> GetNM(int index) {
         int n = _nmArray[index];
         int m = _nmArray[(index + 1) % _nmArray.Length];

         // Во вторую половину вращения фиксирую значение N равно M.
         // Т.к. в прервую половину, с 0 до 180, N стремится к M - см. описание FigureHelper.GetFlowingToTheRightPolygonCoordsByXxx...
         // Т.е. при значении 180 значение N уже достигло M.
         // Фиксирую для того, чтобы при следующем инкременте параметра index, значение N не менялось. Т.о. обеспечиваю плавность анимации.
         if (_incrementSpeedAngle >= 180) {
            if (RotateAngleDelta > 0)
               n = m;
         } else {
            if (RotateAngleDelta < 0)
               n = m;
         }
         return new Tuple<int, int>(n, m);
      }

      protected override void OnTimer() {
         if (Rotate && varMosaicGroupAsValueOthers1 && (_mosaicGroup == EMosaicGroup.eOthers)) {
            bool castling = false;
            var incrementSpeedAngle = _incrementSpeedAngle + 3*RotateAngleDelta;
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
               _nmIndex1 = ++_nmIndex1 % _nmArray.Length;
               _nmIndex2 = ++_nmIndex2 % _nmArray.Length;
            }
         }
         base.OnTimer();
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords_MosaicGroupAsType() {
         const bool accelerateRevert = true; // ускорение под конец анимации, иначе - в начале...

         var shapes = EMosaicGroupEx.GetValues().Length;

         var angle = RotateAngle;
         //var angleAccumulative = angle;
         var anglePart = 360.0 / shapes;

         var sqMax = Math.Min( // размер квадрата куда будет вписана фигура при 0°
               Width - Padding.LeftAndRight,
               Height - Padding.TopAndBottom);
         var sqMin = sqMax / 7; // размер квадрата куда будет вписана фигура при 360°
         var sqDiff = sqMax - sqMin;

         var center = new PointDouble(Width / 2.0, Height / 2.0);

         return Enumerable.Range(0, shapes)
            .Select(shapeNum => {
               var vertices = 3 + shapeNum;
               var angleShape = FixAngle(angle + shapeNum * anglePart);
               //angleAccumulative = Math.sin(FigureHelper.toRadian(angle/4))*angleAccumulative; // accelerate / ускоряшка..

               var sq = angleShape * sqDiff / 360;
               // (un)comment next line to view result changes...
               sq = Math.Sin((angleShape / 4).ToRadian()) * sq; // accelerate / ускоряшка..
               sq = accelerateRevert
                     ? sqMin + sq
                     : sqMax - sq;

               var radius = sq / 1.8;

               var clr = ForegroundColor;
               if (PolarLights)
                  clr = new HSV(clr).AddHue(+angleShape).ToColor(); // try: -angleShape

               return new Tuple<double, Tuple<Color, IEnumerable<PointDouble>>>(sq, new Tuple<Color, IEnumerable<PointDouble>>(
                     clr,
                     FigureHelper.GetRegularPolygonCoords(vertices,
                                                            radius,
                                                            center,
                                                            45 // try to view: angleAccumulative
                                                            )));
            })
            .OrderBy(x => x.Item1)
            .Select(x => x.Item2);
      }

      private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords_MosaicGroupAsValueOthers2() {
         var sq = Math.Min( // size inner square
               Width - Padding.LeftAndRight,
               Height - Padding.TopAndBottom);
         var radius = sq / 2.7;

         var shapes = 3; // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур: треугольники, квадраты и шестигранники

         var angle = RotateAngle;
         var anglePart = 360.0 / shapes;

         var center = new PointDouble(Width / 2.0, Height / 2.0);
         var zero = new PointDouble(0, 0);
         return Enumerable.Range(0, shapes)
            .Select(shapeNum => {
               var angleShape = angle * shapeNum;

               // adding offset
               var offset = FigureHelper.GetPointOnCircle(sq / 5, angleShape + shapeNum * anglePart, zero);
               var centerStar = new PointDouble(center.X + offset.X, center.Y + offset.Y);

               var clr = ForegroundColor;
               if (PolarLights)
                  clr = new HSV(clr).AddHue(shapeNum * anglePart).ToColor();

               int vertices;
               switch (shapeNum) { // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур:
               case 0: vertices = 6; break; // шестигранники
               case 1: vertices = 4; break; // квадраты
               case 2: vertices = 3; break; // и треугольники
               default: throw new Exception();
               }
               return new Tuple<double, Tuple<Color, IEnumerable<PointDouble>>>(1.0, new Tuple<Color, IEnumerable<PointDouble>>(
                     clr,
                     FigureHelper.GetRegularPolygonCoords(vertices, radius, centerStar, -angle)));
            })
            .OrderBy(x => x.Item1)
            .Select(x => x.Item2);
      }

   }

}
