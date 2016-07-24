package fmg.common.geom.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fmg.common.geom.PointDouble;

public final class FigureHelper {

   public static double toRadian(double degreeAngle) {
      return (degreeAngle * Math.PI) / 180; // to radians
   }

   /** Получить координаты точки на периметре круга
    * @param radius радиус круга
    * @param radAngle угол в радианах
    * @param center центр круга
    * @return координаты точки на круге */
   public static PointDouble getPointOnCircleRadian(double radius, double radAngle, PointDouble center) {
      return new PointDouble(radius * Math.sin(radAngle) + center.x, -radius * Math.cos(radAngle) + center.y);
      // ! беру радиус с минусом по Y'ку, т.к. эта координата в математике зеркальна экранной
   }

   /** Получить координаты точки на периметре круга
    * @param radius радиус круга
    * @param degreeAngle угол: -360° .. 0° .. +360°
    * @param center центр круга
    * @return координаты точки на круге */
   public static PointDouble getPointOnCircle(double radius, double degreeAngle, PointDouble center) {
      return getPointOnCircleRadian(radius, toRadian(degreeAngle), center);
   }

   /** https://en.wikipedia.org/wiki/Regular_polygon
    * Получить координаты правильного многоугольника
    *
    * @param n edges / vertices
    * @param radius
    * @param center центр фигуры
    * @param offsetAngle -360° .. 0° .. +360°
    * @return координаты правильного многоугольника */
   public static Stream<PointDouble> getRegularPolygonCoords(int n, double radius, PointDouble center) { return getRegularPolygonCoords(n, radius, center, 0); }

   public static Stream<PointDouble> getRegularPolygonCoords(int n, double radius, PointDouble center, double offsetAngle) {
      double angle = (2 * Math.PI)/n; // 360° / n
      double offsetAngle2 = toRadian(offsetAngle);
      return IntStream.range(0, n)
            .mapToObj(i -> (i * angle) + offsetAngle2)
            .map(a -> getPointOnCircleRadian(radius, a, center));
   }

   /** https://en.wikipedia.org/wiki/Star_polygon
    * Суть:
    *  * два круга - внешний и внутр
    *  * на каждом, по периметру - равноудалённые точки. Кол-во точек == кол-ву лучей у звезды
    *  * внутр круг повёрнут относительно внешенего на 360° / (кол-во лучей) / 2
    *  * и соединяем (т.е. последовательны в массиве) поочерёдно точки с внешнего и внутр круга
    * так и получаем звезду
    *
    * @param rays the number of corner vertices
    * @param radiusOut external radius
    * @param radiusIn internal radius
    * @param center центр фигуры
    * @param offsetAngle -360° .. 0° .. +360°
    * @return координаты правильной звезды */
   public static Stream<PointDouble> getRegularStarCoords(int rays, double radiusOut, double radiusIn, PointDouble center) { return getRegularStarCoords(rays, radiusOut, radiusIn, center, 0); }
   public static Stream<PointDouble> getRegularStarCoords(int rays, double radiusOut, double radiusIn, PointDouble center, double offsetAngle) {
      Stream<PointDouble> pointsExternal = getRegularPolygonCoords(rays, radiusOut, center, offsetAngle);
      Stream<PointDouble> pointsInternal = getRegularPolygonCoords(rays, radiusIn, center, offsetAngle + (180.0/rays));
      return zip(pointsExternal, pointsInternal, (p1, p2) -> Stream.of(p1, p2)).flatMap(x -> x);
   }

   /** rotate around the center coordinates
    * @param coords coordinates for transformation
    * @param angle angle of rotation: -360° .. 0° .. +360°
    * @param center центр фигуры
    * @param additionalDeltaOffset дополнительное смещение координат
    */
   public static Stream<PointDouble> rotate(Stream<PointDouble> coords, double angle, PointDouble center, PointDouble additionalDeltaOffset) {
      angle = toRadian(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      return coords.map(p -> {
               p = new PointDouble(p.x - center.x, p.y - center.y);
               double x = (p.x * cos) - (p.y * sin);
               double y = (p.x * sin) + (p.y * cos);
               p.x = x + center.x + additionalDeltaOffset.x;
               p.y = y + center.y + additionalDeltaOffset.y;
               return p;
            });
   }

   /** rotate around the center coordinates. !!Modify existed collection!!
    * @param coords coordinates for transformation
    * @param angle angle of rotation: -360° .. 0° .. +360°
    * @param center центр фигуры
    * @param additionalDeltaOffset дополнительное смещение координат
    */
   public static void rotateCollection(Collection<PointDouble> coords, double angle, PointDouble center, PointDouble additionalDeltaOffset) {
      angle = toRadian(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      coords.forEach(p -> {
         p.x -= center.x;
         p.y -= center.y;
         double x = (p.x * cos) - (p.y * sin);
         double y = (p.x * sin) + (p.y * cos);
         p.x = x + center.x + additionalDeltaOffset.x;
         p.y = y + center.y + additionalDeltaOffset.y;
      });
   }

   /** http://stackoverflow.com/questions/17640754/zipping-streams-using-jdk8-with-lambda-java-util-stream-streams-zip */
   public static<A, B, C> Stream<C> zip(Stream<? extends A> a, Stream<? extends B> b, BiFunction<? super A, ? super B, ? extends C> zipper) {
      Objects.requireNonNull(zipper);
      @SuppressWarnings("unchecked")
      Spliterator<A> aSpliterator = (Spliterator<A>) Objects.requireNonNull(a).spliterator();
      @SuppressWarnings("unchecked")
      Spliterator<B> bSpliterator = (Spliterator<B>) Objects.requireNonNull(b).spliterator();

      // Zipping looses DISTINCT and SORTED characteristics
      int both = aSpliterator.characteristics() & bSpliterator.characteristics() & ~(Spliterator.DISTINCT | Spliterator.SORTED);
      int characteristics = both;

      long zipSize = ((characteristics & Spliterator.SIZED) != 0)
            ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
            : -1;

      Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
      Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
      Iterator<C> cIterator = new Iterator<C>() {
         @Override
         public boolean hasNext() {
            return aIterator.hasNext() && bIterator.hasNext();
         }
         @Override
         public C next() {
            return zipper.apply(aIterator.next(), bIterator.next());
         }
      };

      Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
      return (a.isParallel() || b.isParallel())
            ? StreamSupport.stream(split, true)
            : StreamSupport.stream(split, false);
   }
}
