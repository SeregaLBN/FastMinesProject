using System;
using System.Collections.Generic;
using System.Linq;

namespace fmg.common.geom.util {

   public static class FigureHelper {

      /// <summary> https://en.wikipedia.org/wiki/Regular_polygon
      /// Вернёт координаты правильного многоугольника
      /// Центр фигуры - координаты [0,0]
      /// </summary>
      /// <param name="n">edges / vertices</param>
      /// <param name="radius"></param>
      /// <param name="offsetAngle">-360° .. 0° .. +360°</param>
      /// <returns></returns>
      public static IEnumerable<PointDouble> GetRegularPolygonCoords(int n, double radius, double offsetAngle = 0) {
         var angle = 2*Math.PI/n; // 360° / n
         offsetAngle = offsetAngle*Math.PI/180; // to radians
         return Enumerable.Range(0, n).
               Select(x => x*angle + offsetAngle).
               Select(x => new PointDouble(radius*Math.Sin(x), radius*Math.Cos(x)));
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
      /// <returns></returns>
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
         angle = angle * Math.PI / 180; // to radians
         var mat = new[] {Math.Cos(angle), -Math.Sin(angle), Math.Sin(angle), Math.Cos(angle) };
         return coords.Select(i => new PointDouble(mat[0]*i.x + mat[1]*i.y, mat[2]*i.x + mat[3]*i.y));
      }

   }
}
