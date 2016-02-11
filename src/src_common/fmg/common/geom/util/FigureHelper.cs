using System;
using System.Collections.Generic;
using System.Linq;

namespace fmg.common.geom.util {

   public static class FigureHelper {

      public static double ToRadian(this double degreeAngle) {
         return degreeAngle * Math.PI / 180; // to radians
      }

      /// <summary> Получить координаты точки на переиметре круга. Центр круга - начало координат </summary>
      /// <param name="radius">радиус круга</param>
      /// <param name="radAngle">угол в радианах</param>
      /// <returns>координаты точки на круге</returns>
      public static PointDouble GetPointOnCircleRadian(double radius, double radAngle) {
         return new PointDouble(radius * Math.Sin(radAngle), -radius * Math.Cos(radAngle));
         // ! беру радиус с минусом по Y'ку, т.к. эта координата в математике зеркальна экранной
      }
      /// <summary> Получить координаты точки на периметре круга. Центр круга - начало координат </summary>
      /// <param name="radius">радиус круга</param>
      /// <param name="degreeAngle">угол: -360° .. 0° .. +360°</param>
      /// <returns>координаты точки на круге</returns>
      public static PointDouble GetPointOnCircle(double radius, double degreeAngle) {
         return GetPointOnCircleRadian(radius, degreeAngle.ToRadian());
      }

      /// <summary> https://en.wikipedia.org/wiki/Regular_polygon
      /// Получить координаты правильного многоугольника с центром фигуры в координатах [0,0]
      /// </summary>
      /// <param name="n">edges / vertices</param>
      /// <param name="radius"></param>
      /// <param name="offsetAngle">-360° .. 0° .. +360°</param>
      /// <returns>координаты правильного многоугольника</returns>
      public static IEnumerable<PointDouble> GetRegularPolygonCoords(int n, double radius, double offsetAngle = 0) {
         var angle = 2*Math.PI/n; // 360° / n
         offsetAngle = offsetAngle.ToRadian();
         return Enumerable.Range(0, n).
               Select(i => i*angle + offsetAngle).
               Select(a => GetPointOnCircleRadian(radius, a));
      }

      /// <summary> https://en.wikipedia.org/wiki/Star_polygon
      /// Суть:
      ///  * два круга - внешний и внутр
      ///  * на каждом, по периметру - равноудалённые точки. Кол-во точек == кол-ву лучей у звезды
      ///  * внутр круг повёрнут относительно внешенего на 360° / (кол-во лучей) / 2
      ///  * и соединяем (т.е. последовательны в массиве) поочерёдно точки с внешнего и внутр круга
      /// так и получаем звезду
      /// Центр фигуры - координаты [0,0]
      /// </summary>
      /// <param name="rays">the number of corner vertices</param>
      /// <param name="radiusOut">external radius</param>
      /// <param name="radiusIn">internal radius</param>
      /// <param name="offsetAngle">-360° .. 0° .. +360°</param>
      /// <returns>координаты правильной звезды</returns>
      public static IEnumerable<PointDouble> GetRegularStarCoords(int rays, double radiusOut, double radiusIn, double offsetAngle = 0) {
         var pointsExt = GetRegularPolygonCoords(rays, radiusOut, offsetAngle);
         var pointsInt = GetRegularPolygonCoords(rays, radiusIn, offsetAngle + 180.0/rays);
         return pointsExt.Zip(pointsInt, (p1, p2) => new[] {p1, p2}).SelectMany(x => x);
      }

      /// <summary> rotate around the center coordinates </summary>
      /// <param name="coords">coordinates for transformation</param>
      /// <param name="angle">angle of rotation: -360° .. 0° .. +360°</param>
      /// <returns></returns>
      public static IEnumerable<PointDouble> Rotate(this IEnumerable<PointDouble> coords, double angle) {
         angle = angle.ToRadian();
         var cos = Math.Cos(angle);
         var sin = Math.Sin(angle);
         return coords.Select(i => new PointDouble(i.X*cos - i.Y*sin, i.X*sin + i.Y*cos));
      }

   }
}
