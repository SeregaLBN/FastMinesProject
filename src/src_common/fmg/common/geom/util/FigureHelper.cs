using System;
using System.Collections.Generic;
using System.Linq;

namespace fmg.common.geom.util {

   public static class FigureHelper {

      public static double ToRadian(this double degreesAngle) {
         return degreesAngle * Math.PI / 180;
      }
      public static double ToDegrees(this double radianAngle) {
         return radianAngle * 180 / Math.PI;
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
      /// <param name="degreesAngle">угол: -360° .. 0° .. +360°</param>
      /// <param name="center">центр круга</param>
      /// <returns>координаты точки на круге</returns>
      public static PointDouble GetPointOnCircle(double radius, double degreesAngle, PointDouble center) {
         return GetPointOnCircleRadian(radius, degreesAngle.ToRadian(), center);
      }

      /// <summary> https://en.wikipedia.org/wiki/Regular_polygon
      /// Получить координаты правильного многоугольника
      /// </summary>
      /// <param name="n">edges / vertices</param>
      /// <param name="radius"></param>
      /// <param name="center">центр фигуры</param>
      /// <param name="offsetAngle">additional rotation angle in degrees: -360° .. 0° .. +360°</param>
      /// <returns>координаты правильного многоугольника</returns>
      public static IEnumerable<PointDouble> GetRegularPolygonCoords(int n, double radius, PointDouble center, double offsetAngle = 0) {
         var angle = 2*Math.PI/n; // 360° / n
         offsetAngle = offsetAngle.ToRadian();
         return Enumerable.Range(0, n).
               Select(i => i*angle + offsetAngle).
               Select(a => GetPointOnCircleRadian(radius, a, center));
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
      /// <param name="offsetAngle">additional rotation angle in degrees: -360° .. 0° .. +360°</param>
      /// <returns>координаты правильной звезды</returns>
      public static IEnumerable<PointDouble> GetRegularStarCoords(int rays, double radiusOut, double radiusIn, PointDouble center, double offsetAngle = 0) {
         var pointsExt = GetRegularPolygonCoords(rays, radiusOut, center, offsetAngle);
         var pointsInt = GetRegularPolygonCoords(rays, radiusIn, center, offsetAngle + 180.0/rays);
         return pointsExt.Zip(pointsInt, (p1, p2) => new[] {p1, p2}).SelectMany(x => x);
      }

      /// <summary>
      /// Очередной шаг анимации преобразования простого N-многоугольника в M-многоугольник (где N &lt; M).
      /// Вся анимация - при изменении параметра incrementSpeedAngle от 0° до 360°.
      /// Анимация заключается в том, что последние M-N вершин плавно расходятся из одной точки (при incrementSpeedAngle 0°..180°), и наоборот - плавно сходятся в одну точку (при incrementSpeedAngle 180°..360°) при постоянном радиусе.
      /// Оба многоугольника расчитываются через радиусом круга в который они вписываются (радиус задан как параметр).
      ///
      /// Т.к. M-многоугольник описывается вписанными в круг М-треугольниками, то манипулируя внутренними (у центра круга) углами треугольников, можно создать плавную анимацию.
      /// Плавность обеспечиваю изменением угла (у последних M-N треугольников) от 0° до 360°/M (ускорение делаю через функцию синуса параметра incrementSpeedAngle/2).
      /// </summary>
      /// <param name="n">кол-во вершин с которых начинается преобразование фигуры</param>
      /// <param name="m">кол-во вершин к которой преобразовывается фигуру</param>
      /// <param name="radius"></param>
      /// <param name="center"></param>
      /// <param name="incrementSpeedAngle">угловая скорость приращения: 0°..360°.
      /// При 0°..180° - N стремится к M.
      /// При 180°..360° - M стремится к N. </param>
      /// <param name="offsetAngle">additional rotation angle in degrees: -360° .. 0° .. +360°</param>
      /// <returns></returns>
      public static IEnumerable<PointDouble> GetFlowingToTheRightPolygonCoordsByRadius(int n, int m, double radius, PointDouble center, double incrementSpeedAngle, double offsetAngle = 0) {
         System.Diagnostics.Debug.Assert(incrementSpeedAngle >= 0);
         System.Diagnostics.Debug.Assert(incrementSpeedAngle < 360);
         if (n > m) {
            var tmp = m;
            m = n;
            n = tmp;
            incrementSpeedAngle += 180;
            if (incrementSpeedAngle >= 360)
               incrementSpeedAngle -= 360;
         }
         System.Diagnostics.Debug.Assert(n > 2);
         incrementSpeedAngle = incrementSpeedAngle.ToRadian();
         offsetAngle = offsetAngle.ToRadian();
         var angle = 2 * Math.PI / m; // 360° / m
         var angleLastNM = angle * Math.Sin(incrementSpeedAngle / 2); // angleLastNM|incrementSpeedAngle == 0°|0° .. angle|180° .. 0°|360°
         System.Diagnostics.Debug.Assert(angleLastNM >= 0, nameof(incrementSpeedAngle) + " parameter must have a value of 0°..360°");
         var angleFirstN = (2 * Math.PI - angleLastNM * (m - n)) / n;
         System.Diagnostics.Debug.Assert((2 * Math.PI).HasMinDiff(n * angleFirstN + (m - n) * angleLastNM));
         return Enumerable.Range(0, m).
               Select(i => (i < n)
                  ? i * angleFirstN + offsetAngle                         // 0..n
                  : n * angleFirstN + (i - n) * angleLastNM + offsetAngle // n..m
               ).
               Select(a => GetPointOnCircleRadian(radius, a, center));
      }

      /// <summary>
      /// Очередной шаг анимации преобразования простого N-многоугольника в M-многоугольник (где N &lt; M).
      /// Вся анимация - при изменении параметра incrementSpeedAngle от 0° до 360°.
      /// Анимация заключается в том, что последние M-N вершин плавно расходятся из одной точки (при incrementSpeedAngle 0°..180°), и наоборот - плавно сходятся в одну точку (при incrementSpeedAngle 180°..360°) при постоянном размере одной из сторон.
      /// Оба многоугольника определяются размером стороны (задаётся как параметр), а также номером стороны, размер которой будет постоянным при анимации.
      ///
      /// Стартовый N- и конечный M-многоугольник расчитываются через радиус круга (в который они вписаны). Но т.к. многоугольники определяются размером стороны, то радусы у них различны.
      /// Поэтому плавность анимации обеспечиваю:
      ///  * изменением угла (у последних M-N треугольников) от 0° до 360°/M (ускорение делаю через функцию синуса параметра incrementSpeedAngle/2).
      ///  * изменением радиуса от rN до rM
      /// </summary>
      /// <param name="n">кол-во вершин с которых начинается преобразование фигуры</param>
      /// <param name="m">кол-во вершин к которой преобразовывается фигуру</param>
      /// <param name="sizeSide">размер стороны многоугольника</param>
      /// <param name="sideNum">номер грани многоугольника, длина которой должен быть постоянным</param>
      /// <param name="center"></param>
      /// <param name="incrementSpeedAngle">угловая скорость приращения: 0°..360°.
      /// При 0°..180° - N стремится к M.
      /// При 180°..360° - M стремится к N. </param>
      /// <param name="offsetAngle">additional rotation angle in degrees: -360° .. 0° .. +360°</param>
      /// <returns></returns>
      public static IEnumerable<PointDouble> GetFlowingToTheRightPolygonCoordsBySide(int n, int m, double sizeSide, int sideNum, PointDouble center, double incrementSpeedAngle, double offsetAngle = 0) {
         //incrementSpeedAngle = incrementSpeedAngle % 360;
         //if (incrementSpeedAngle < 0)
         //   incrementSpeedAngle += 360;
         System.Diagnostics.Debug.Assert(incrementSpeedAngle >= 0);
         System.Diagnostics.Debug.Assert(incrementSpeedAngle < 360);
         if (n > m) {
            var tmp = m;
            m = n;
            n = tmp;
            incrementSpeedAngle += 180;
            if (incrementSpeedAngle >= 360)
               incrementSpeedAngle -= 360;
         }
         System.Diagnostics.Debug.Assert(n > 2);
         System.Diagnostics.Debug.Assert(sideNum <= n);
         incrementSpeedAngle = incrementSpeedAngle.ToRadian();
         offsetAngle = offsetAngle.ToRadian();
         var angleNpart = 2 * Math.PI / n; // 360° / n
         var angleMpart = 2 * Math.PI / m; // 360° / m
         var angle = angleNpart + (angleMpart - angleNpart) * Math.Sin(incrementSpeedAngle / 2); // angle|incrementSpeedAngle == angleNpart|0° .. angleMpart|180° .. angleNpart|360°
         var radius = sizeSide * Math.Sin((Math.PI - angle) / 2) / Math.Sin(angle); // from formula 'Law of sines':   sizeSide/sin(angle) == radius/sin((180°-angle)/2)
         var angleLastNM = angle * Math.Sin(incrementSpeedAngle / 2); // angleLastNM|incrementSpeedAngle == 0°|0° .. angle|180° .. 0°|360°
         System.Diagnostics.Debug.Assert(angleLastNM >= 0, nameof(incrementSpeedAngle) + " parameter must have a value of 0°..360°");
         var angleFirstN = (2 * Math.PI - angleLastNM * (m - n) - angle) / (n - 1);
         System.Diagnostics.Debug.Assert((2 * Math.PI).HasMinDiff((n - 1) * angleFirstN + angle + (m - n) * angleLastNM));
         return Enumerable.Range(0, m).
               Select(i => {
                  if (i < n) {
                     // 0..n
                     if (i < sideNum)
                        return i * angleFirstN + offsetAngle;
                     if (i == sideNum)
                        return (i - 1) * angleFirstN + angle + offsetAngle;
                     return (i - 1) * angleFirstN + angle + offsetAngle;
                  }
                  // n..m
                  return (n - 1) * angleFirstN + angle + (i - n) * angleLastNM + offsetAngle;
               }).
               Select(a => GetPointOnCircleRadian(radius, a, center));
      }

      /// <summary> rotate around the center coordinates </summary>
      /// <param name="coords">coordinates for transformation</param>
      /// <param name="angle">angle of rotation: -360° .. 0° .. +360°</param>
      /// <param name="center">центр фигуры</param>
      /// <returns>new rotated points</returns>
      public static IEnumerable<PointDouble> Rotate(this IEnumerable<PointDouble> coords, double angle, PointDouble center) {
         angle = angle.ToRadian();
         var cos = Math.Cos(angle);
         var sin = Math.Sin(angle);
         return coords.Select(p => {
            p.X -= center.X;
            p.Y -= center.Y;
            var x = (p.X * cos) - (p.Y * sin);
            var y = (p.X * sin) + (p.Y * cos);
            p.X = x + center.X;
            p.Y = y + center.Y;
            return p;
         });
      }

      /// <summary> rotate around the center coordinates. !!Modify existed collection!! </summary>
      /// <param name="coords">coordinates for transformation</param>
      /// <param name="angle">angle of rotation: -360° .. 0° .. +360°</param>
      /// <param name="center">центр фигуры</param>
      public static IList<PointDouble> RotateList(this IList<PointDouble> coords, double angle, PointDouble center) {
         angle = angle.ToRadian();
         var cos = Math.Cos(angle);
         var sin = Math.Sin(angle);
         for (var i = 0; i < coords.Count; ++i) {
            var p = coords[i];
            p.X -= center.X;
            p.Y -= center.Y;
            var x = (p.X * cos) - (p.Y * sin);
            var y = (p.X * sin) + (p.Y * cos);
            p.X = x + center.X;
            p.Y = y + center.Y;
            coords[i] = p;
         }
         return coords;
      }

      /// <summary> adds an offset to each point </summary>
      public static IEnumerable<PointDouble> Move(this IEnumerable<PointDouble> coords, PointDouble offset) {
         return coords.Select(p => {
            p.X += offset.X;
            p.Y += offset.Y;
            return p;
         });
      }

      /// <summary> adds an offset to each point. !!Modify existed collection!! </summary>
      public static IList<PointDouble> MoveList(this IList<PointDouble> coords, PointDouble offset) {
         for (var i = 0; i < coords.Count; ++i) {
            var p = coords[i];
            p.X += offset.X;
            p.Y += offset.Y;
            coords[i] = p;
         }
         return coords;
      }

      /// <summary>
      /// Повернуть / выровнять заданную фигуру по указанной грани относительно X координаты.
      /// (Узнаю насколько повёрнута грань и разворачиваю всю фигуру в обратную сторону)
      /// </summary>
      /// <param name="coords">Фигура заданная координатами</param>
      /// <param name="sideNum">Номер грани фигуры (начиная с 1)</param>
      /// <param name="center">центр вращения</param>
      /// <param name="alignmentAngle">угол на который необходимо выровнять (в градусах)</param>
      /// <returns>Координаты выровняной фигуры</returns>
      public static IEnumerable<PointDouble> RotateBySide(this IEnumerable<PointDouble> coords, int sideNum, PointDouble center, double alignmentAngle = 0) {
         var list = coords.ToList();
         var p1 = list[sideNum - 1];
         var p2 = list[sideNum];

         var dx = p2.X - p1.X; // cathetus x
         var dy = p2.Y - p1.Y; // cathetus y
         var h = Math.Sqrt(dx * dx + dy * dy); // hypotenuse

         var cos = dx / h;
         var angle = Math.Acos(cos).ToDegrees();
         if (angle < 0)
            angle += 360;
         if (dy < 0)
            angle = 360 - angle;

         //LoggerSimple.Put($"vector=[{dx:0.00},{dy:0.00}]; cos={cos:0.000000}; angle={angle:0.00}");

         return list.Rotate(-angle + alignmentAngle, center);
      }

   }

}
