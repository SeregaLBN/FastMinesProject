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

   /** Получить координаты точки на переиметре круга. Центр круга - начало координат
    * @param radius радиус круга
    * @param radAngle угол в радианах
    * @return координаты точки на круге */
   public static PointDouble getPointOnCircleRadian(double radius, double radAngle) {
      return new PointDouble(radius * Math.sin(radAngle), -radius * Math.cos(radAngle));
      // ! беру радиус с минусом по Y'ку, т.к. эта координата в математике зеркальна экранной
   }
   /** Получить координаты точки на периметре круга. Центр круга - начало координат
    * @param radius радиус круга
    * @param degreeAngle угол: -360° .. 0° .. +360°
    * @return координаты точки на круге */
   public static PointDouble getPointOnCircle(double radius, double degreeAngle) {
      return getPointOnCircleRadian(radius, toRadian(degreeAngle));
   }

   /** https://en.wikipedia.org/wiki/Regular_polygon
    * Получить координаты правильного многоугольника с центром фигуры в координатах [0,0]
    *
    * @param n edges / vertices
    * @param radius
    * @param offsetAngle -360° .. 0° .. +360°
    * @return координаты правильного многоугольника */
   public static Stream<PointDouble> getRegularPolygonCoords(int n, double radius) { return getRegularPolygonCoords(n, radius, 0); }

   public static Stream<PointDouble> getRegularPolygonCoords(int n, double radius, double offsetAngle) {
      double angle = (2 * Math.PI)/n; // 360° / n
      double offsetAngle2 = toRadian(offsetAngle);
      return IntStream.range(0, n)
            .mapToObj(i -> (i * angle) + offsetAngle2)
            .map(a -> getPointOnCircleRadian(radius, a));
   }

   /** https://en.wikipedia.org/wiki/Star_polygon
    * Суть:
    *  * два круга - внешний и внутр
    *  * на каждом, по периметру - равноудалённые точки. Кол-во точек == кол-ву лучей у звезды
    *  * внутр круг повёрнут относительно внешенего на 360° / (кол-во лучей) / 2
    *  * и соединяем (т.е. последовательны в массиве) поочерёдно точки с внешнего и внутр круга
    * так и получаем звезду
    * Центр фигуры - координаты [0,0]
    *
    * @param rays the number of corner vertices
    * @param radiusOut external radius
    * @param radiusIn internal radius
    * @param offsetAngle -360° .. 0° .. +360°
    * @return координаты правильной звезды */
   public static Stream<PointDouble> getRegularStarCoords(int rays, double radiusOut, double radiusIn) { return getRegularStarCoords(rays, radiusOut, radiusIn, 0); }
   public static Stream<PointDouble> getRegularStarCoords(int rays, double radiusOut, double radiusIn, double offsetAngle) {
      Stream<PointDouble> pointsExternal = getRegularPolygonCoords(rays, radiusOut, offsetAngle);
      Stream<PointDouble> pointsInternal = getRegularPolygonCoords(rays, radiusIn, offsetAngle + (180.0/rays));
      return zip(pointsExternal, pointsInternal, (p1, p2) -> Stream.of(p1, p2)).flatMap(x -> x);
   }

   /** rotate around the center coordinates
    * @param coords coordinates for transformation
    * @param angle angle of rotation: -360° .. 0° .. +360°
    */
   public static Stream<PointDouble> rotate(Stream<PointDouble> coords, double angle) {
      angle = toRadian(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      return coords.map(p -> new PointDouble((p.x * cos) - (p.y * sin), (p.x * sin) + (p.y * cos)));
   }

   public static void rotate(Collection<PointDouble> coords, double angle, PointDouble center) {
      angle = toRadian(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      coords.forEach(p -> {
         p.x -= center.x;
         p.y -= center.x;
         double x = (p.x * cos) - (p.y * sin);
         double y = (p.x * sin) + (p.y * cos);
         p.x = x + center.x;
         p.y = y + center.y;
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
