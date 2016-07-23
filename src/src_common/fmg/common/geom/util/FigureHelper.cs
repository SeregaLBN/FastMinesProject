using System;
using System.Collections.Generic;
using System.Linq;

namespace fmg.common.geom.util {

   public static class FigureHelper {

      public static double ToRadian(this double degreeAngle) {
         return degreeAngle * Math.PI / 180; // to radians
      }

      /// <summary> Получить координаты точки на периметре круга </summary>
      /// <param name="radius">радиус круга</param>
      /// <param name="radAngle">угол в радианах</param>
      /// <param name="center">центр круга</param>
      /// <returns>координаты точки на круге</returns>
      public static PointDouble GetPointOnCircleRadian(double radius, double radAngle, PointDouble center) {
         return new PointDouble(radius * Math.Sin(radAngle) + center.X, -radius * Math.Cos(radAngle) + center.Y);
         // ! беру радиус с минусом по Y'ку, т.к. эта координата в математике зеркальна экранной
      }

      /// <summary> Получить координаты точки на периметре круга </summary>
      /// <param name="radius">радиус круга</param>
      /// <param name="degreeAngle">угол: -360° .. 0° .. +360°</param>
      /// <param name="center">центр круга</param>
      /// <returns>координаты точки на круге</returns>
      public static PointDouble GetPointOnCircle(double radius, double degreeAngle, PointDouble center) {
         return GetPointOnCircleRadian(radius, degreeAngle.ToRadian(), center);
      }

      /// <summary> https://en.wikipedia.org/wiki/Regular_polygon
      /// Получить координаты правильного многоугольника
      /// </summary>
      /// <param name="n">edges / vertices</param>
      /// <param name="radius"></param>
      /// <param name="center">центр фигуры</param>
      /// <param name="offsetAngle">-360° .. 0° .. +360°</param>
      /// <returns>координаты правильного многоугольника</returns>
      public static IEnumerable<PointDouble> GetRegularPolygonCoords(int n, double radius, PointDouble center, double offsetAngle = 0) {
         var angle = 2*Math.PI/n; // 360° / n
         offsetAngle = offsetAngle.ToRadian();
         return Enumerable.Range(0, n).
               Select(i => i*angle + offsetAngle).
               Select(a => GetPointOnCircleRadian(radius, a, center));
      }

      /// <summary>
      /// Анимация для преобразования простого N-многоугольника в M-многоугольник (где N &lt; M).
      /// </summary>
      /// <param name="n">кол-во вершин с которых начинается преобразование фигуры</param>
      /// <param name="m">кол-во вершин к которой преобразовывается фигуру</param>
      /// <param name="radius"></param>
      /// <param name="center"></param>
      /// <param name="incrementSpeedAngle">угловая скорость приращения: 0°..360°.
      /// При 0°..180° - N стремится к M.
      /// При 180°..360° - M стремится к N. </param>
      /// <param name="offsetAngle"></param>
      /// <returns></returns>
      public static IEnumerable<PointDouble> GetFlowingToTheRightPolygonCoords(int n, int m, double radius, PointDouble center, double incrementSpeedAngle, double offsetAngle = 0) {
         incrementSpeedAngle = incrementSpeedAngle.ToRadian();
         offsetAngle = offsetAngle.ToRadian();
         var angle = 2 * Math.PI / m; // 360° / m
         var angleM = angle * Math.Sin(incrementSpeedAngle / 2); // 0(0°)..angle(180°)..0(360°)
         System.Diagnostics.Debug.Assert(angleM >= 0, nameof(incrementSpeedAngle) + " parameter must have a value of 0°..360°");
         var angleN = (360.0.ToRadian() - angleM * (m - n)) / n;
         return Enumerable.Range(0, m).
               Select(i => (i < n)
                  ? i * angleN + offsetAngle                    // 0..n
                  : n * angleN + (i - n) * angleM + offsetAngle // n..m
               ).
               Select(a => FigureHelper.GetPointOnCircleRadian(radius, a, center));
      }

      /// <summary> https://en.wikipedia.org/wiki/Star_polygon
      /// Суть:
      ///  * два круга - внешний и внутр
      ///  * на каждом, по периметру - равноудалённые точки. Кол-во точек == кол-ву лучей у звезды
      ///  * внутр круг повёрнут относительно внешенего на 360° / (кол-во лучей) / 2
      ///  * и соединяем (т.е. последовательны в массиве) поочерёдно точки с внешнего и внутр круга
      /// так и получаем звезду
      /// </summary>
      /// <param name="rays">the number of corner vertices</param>
      /// <param name="radiusOut">external radius</param>
      /// <param name="radiusIn">internal radius</param>
      /// <param name="center">центр фигуры</param>
      /// <param name="offsetAngle">-360° .. 0° .. +360°</param>
      /// <returns>координаты правильной звезды</returns>
      public static IEnumerable<PointDouble> GetRegularStarCoords(int rays, double radiusOut, double radiusIn, PointDouble center, double offsetAngle = 0) {
         var pointsExt = GetRegularPolygonCoords(rays, radiusOut, center, offsetAngle);
         var pointsInt = GetRegularPolygonCoords(rays, radiusIn, center, offsetAngle + 180.0/rays);
         return pointsExt.Zip(pointsInt, (p1, p2) => new[] {p1, p2}).SelectMany(x => x);
      }

      /// <summary> rotate around the center coordinates </summary>
      /// <param name="coords">coordinates for transformation</param>
      /// <param name="angle">angle of rotation: -360° .. 0° .. +360°</param>
      /// <param name="center">центр фигуры</param>
      /// <param name="additionalDeltaOffset">дополнительное смещение координат</param>
      /// <returns>new rotated points</returns>
      public static IEnumerable<PointDouble> Rotate(this IEnumerable<PointDouble> coords, double angle, PointDouble center, PointDouble additionalDeltaOffset = default(PointDouble)) {
         angle = angle.ToRadian();
         var cos = Math.Cos(angle);
         var sin = Math.Sin(angle);
         return coords.Select(p => { //new PointDouble(i.X*cos - i.Y*sin, i.X*sin + i.Y*cos));
            p = new PointDouble(p.X - center.X, p.Y - center.Y);
            var x = (p.X * cos) - (p.Y * sin);
            var y = (p.X * sin) + (p.Y * cos);
            p.X = x + center.X + additionalDeltaOffset.X;
            p.Y = y + center.Y + additionalDeltaOffset.Y;
            return p;
         });
      }

      /// <summary> rotate around the center coordinates. !!Modify existed collection!! </summary>
      /// <param name="coords">coordinates for transformation</param>
      /// <param name="angle">angle of rotation: -360° .. 0° .. +360°</param>
      /// <param name="center">центр фигуры</param>
      /// <param name="additionalDeltaOffset">дополнительное смещение координат</param>
      public static void Rotate(this IList<PointDouble> coords, double angle, PointDouble center, PointDouble additionalDeltaOffset= default(PointDouble)) {
         angle = angle.ToRadian();
         var cos = Math.Cos(angle);
         var sin = Math.Sin(angle);
         for (var i=0; i<coords.Count; ++i) {
            var p = coords[i];
            p.X -= center.X;
            p.Y -= center.Y;
            var x = (p.X * cos) - (p.Y * sin);
            var y = (p.X * sin) + (p.Y * cos);
            p.X = x + center.X + additionalDeltaOffset.X;
            p.Y = y + center.Y + additionalDeltaOffset.Y;
            coords[i] = p;
         }
      }

   }
}
