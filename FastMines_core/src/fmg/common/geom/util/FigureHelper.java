package fmg.common.geom.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fmg.common.geom.DoubleExt;
import fmg.common.geom.PointDouble;

/**
 * Трансформеры для фигур - манипуляции с координатами...
 * ! Избегать использования цветов - это удел конкретной реализации.
 */
public final class FigureHelper {

    public static double toRadian(double degreesAngle) {
        return (degreesAngle * Math.PI) / 180; // to radians
    }
    public static double toDegrees(double radianAngle) {
        return radianAngle * 180 / Math.PI;
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
     * @param degreesAngle угол: -360° .. 0° .. +360°
     * @param center центр круга
     * @return координаты точки на круге */
    public static PointDouble getPointOnCircle(double radius, double degreesAngle, PointDouble center) {
        return getPointOnCircleRadian(radius, toRadian(degreesAngle), center);
    }

    /** https://en.wikipedia.org/wiki/Regular_polygon
     * Получить координаты правильного многоугольника
     *
     * @param n edges / vertices
     * @param radius
     * @param center центр фигуры
     * @param offsetAngle additional rotation angle in degrees: -360° .. 0° .. +360°
     * @return координаты правильного многоугольника */
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
     * @param offsetAngle additional rotation angle in degrees: -360° .. 0° .. +360°
     * @return координаты правильной звезды */
    public static Stream<PointDouble> getRegularStarCoords(int rays, double radiusOut, double radiusIn, PointDouble center, double offsetAngle) {
        Stream<PointDouble> pointsExternal = getRegularPolygonCoords(rays, radiusOut, center, offsetAngle);
        Stream<PointDouble> pointsInternal = getRegularPolygonCoords(rays, radiusIn, center, offsetAngle + (180.0/rays));
        return zip(pointsExternal, pointsInternal, (p1, p2) -> Stream.of(p1, p2)).flatMap(x -> x);
    }

    /** Очередной шаг анимации преобразования простого N-многоугольника в M-многоугольник (где N &lt; M).
     * <br>Вся анимация - при изменении параметра incrementSpeedAngle от 0° до 360°.
     * <br>Анимация заключается в том, что последние M-N вершин плавно расходятся из одной точки (при incrementSpeedAngle 0°..180°), и наоборот - плавно сходятся в одну точку (при incrementSpeedAngle 180°..360°) при постоянном радиусе.
     * <br>Оба многоугольника расчитываются через радиусом круга в который они вписываются (радиус задан как параметр).
     * <br>
     * <br>Т.к. M-многоугольник описывается вписанными в круг М-треугольниками, то манипулируя внутренними (у центра круга) углами треугольников, можно создать плавную анимацию.
     * <br>Плавность обеспечиваю изменением угла (у последних M-N треугольников) от 0° до 360°/M (ускорение делаю через функцию синуса параметра incrementSpeedAngle/2).
     * @param n кол-во вершин с которых начинается преобразование фигуры
     * @param m кол-во вершин к которой преобразовывается фигуру
     * @param radius
     * @param center
     * @param incrementSpeedAngle угловая скорость приращения: 0°..360°.
     * <br> При 0°..180° - N стремится к M.
     * <br> При 180°..360° - M стремится к N.
     * @param offsetAngle additional rotation angle in degrees: -360° .. 0° .. +360°
     * @return
     */
    public static Stream<PointDouble> getFlowingToTheRightPolygonCoordsByRadius(int n, int m, double radius, PointDouble center, double incrementSpeedAngle, double offsetAngle) {
        assert(incrementSpeedAngle >= 0);
        assert(incrementSpeedAngle < 360);
        if (n > m) {
            int tmp = m;
            m = n;
            n = tmp;
            incrementSpeedAngle += 180;
            if (incrementSpeedAngle >= 360)
                incrementSpeedAngle -= 360;
        }
        assert(n > 2);
        incrementSpeedAngle = toRadian(incrementSpeedAngle);
        double offsetAngleRad = toRadian(offsetAngle);
        double angle = 2 * Math.PI / m; // 360° / m
        double angleLastNM = angle * Math.sin(incrementSpeedAngle / 2); // angleLastNM|incrementSpeedAngle == 0°|0° .. angle|180° .. 0°|360°
        assert(angleLastNM >= 0); // incrementSpeedAngle parameter must have a value of 0°..360°
        double angleFirstN = (2 * Math.PI - angleLastNM * (m - n)) / n;
        assert(DoubleExt.hasMinDiff(2 * Math.PI, n * angleFirstN + (m - n) * angleLastNM));
        int nn = n;
        return IntStream.range(0, m).
                mapToObj(i -> (i < nn)
                    ? i * angleFirstN + offsetAngleRad                           // 0..n
                    : nn * angleFirstN + (i - nn) * angleLastNM + offsetAngleRad // n..m
                ).
                map(a -> getPointOnCircleRadian(radius, a, center));
    }

    /** Очередной шаг анимации преобразования простого N-многоугольника в M-многоугольник (где N &lt; M).
     * <br>Вся анимация - при изменении параметра incrementSpeedAngle от 0° до 360°.
     * <br>Анимация заключается в том, что последние M-N вершин плавно расходятся из одной точки (при incrementSpeedAngle 0°..180°), и наоборот - плавно сходятся в одну точку (при incrementSpeedAngle 180°..360°) при постоянном размере одной из сторон.
     * <br>Оба многоугольника определяются размером стороны (задаётся как параметр), а также номером стороны, размер которой будет постоянным при анимации.
     * <br>
     * <br>Стартовый N- и конечный M-многоугольник расчитываются через радиус круга (в который они вписаны). Но т.к. многоугольники определяются размером стороны, то радусы у них различны.
     * <br>Поэтому плавность анимации обеспечиваю:
     * <br> * изменением угла (у последних M-N треугольников) от 0° до 360°/M (ускорение делаю через функцию синуса параметра incrementSpeedAngle/2).
     * <br> * изменением радиуса от rN до rM
     * @param n кол-во вершин с которых начинается преобразование фигуры
     * @param m кол-во вершин к которой преобразовывается фигуру
     * @param sizeSide размер стороны многоугольника
     * @param sideNum номер грани многоугольника, длина которой должен быть постоянным (начиная с 1)
     * @param center
     * @param incrementSpeedAngle угловая скорость приращения: 0°..360°.
     * <br> При 0°..180° - N стремится к M.
     * <br> При 180°..360° - M стремится к N.
     * @param offsetAngle additional rotation angle in degrees: -360° .. 0° .. +360°
     * @return
     */
    public static Stream<PointDouble> getFlowingToTheRightPolygonCoordsBySide(int n, int m, double sizeSide, int sideNum, PointDouble center, double incrementSpeedAngle, double offsetAngle) {
       //incrementSpeedAngle = incrementSpeedAngle % 360;
       //if (incrementSpeedAngle < 0)
       //    incrementSpeedAngle += 360;
       assert(incrementSpeedAngle >= 0);
       assert(incrementSpeedAngle < 360);
       if (n > m) {
            int tmp = m;
            m = n;
            n = tmp;
            incrementSpeedAngle += 180;
            if (incrementSpeedAngle >= 360)
                incrementSpeedAngle -= 360;
       }
       assert(n > 2);
       assert(sideNum <= n);
       incrementSpeedAngle = toRadian(incrementSpeedAngle);
       double offsetAngleRad = toRadian(offsetAngle);
       double angleNpart = 2 * Math.PI / n; // 360° / n
       double angleMpart = 2 * Math.PI / m; // 360° / m
       double angle = angleNpart + (angleMpart - angleNpart) * Math.sin(incrementSpeedAngle / 2); // angle|incrementSpeedAngle == angleNpart|0° .. angleMpart|180° .. angleNpart|360°
       double radius = sizeSide * Math.sin((Math.PI - angle) / 2) / Math.sin(angle); // from formula 'Law of sines':   sizeSide/sin(angle) == radius/sin((180°-angle)/2)
       double angleLastNM = angle * Math.sin(incrementSpeedAngle / 2); // angleLastNM|incrementSpeedAngle == 0°|0° .. angle|180° .. 0°|360°
       assert(angleLastNM >= 0); // incrementSpeedAngle parameter must have a value of 0°..360°
       double angleFirstN = (2 * Math.PI - angleLastNM * (m - n) - angle) / (n - 1);
       assert(DoubleExt.hasMinDiff(2 * Math.PI, (n - 1) * angleFirstN + angle + (m - n) * angleLastNM));
       int nn = n;
       return IntStream.range(0, m).
            mapToObj(i -> {
                if (i < nn) {
                    // 0..n
                    if (i < sideNum)
                        return i * angleFirstN + offsetAngleRad;
                    if (i == sideNum)
                        return (i - 1) * angleFirstN + angle + offsetAngleRad;
                    return (i - 1) * angleFirstN + angle + offsetAngleRad;
                }
                // n..m
                return (nn - 1) * angleFirstN + angle + (i - nn) * angleLastNM + offsetAngleRad;
            }).
            map(a -> getPointOnCircleRadian(radius, a, center));
    }

    /** rotate around the center coordinates
     * @param coords coordinates for transformation
     * @param angle angle of rotation: -360° .. 0° .. +360°
     * @param center центр фигуры
     */
    public static Stream<PointDouble> rotate(Stream<PointDouble> coords, double angle, PointDouble center) {
       angle = toRadian(angle);
       double cos = Math.cos(angle);
       double sin = Math.sin(angle);
       return coords.map(p -> {
                p = new PointDouble(p.x - center.x, p.y - center.y);
                double x = (p.x * cos) - (p.y * sin);
                double y = (p.x * sin) + (p.y * cos);
                p.x = x + center.x;
                p.y = y + center.y;
                return p;
            });
    }

    /** rotate around the center coordinates. !!Modify existed collection!!
     * @param coords coordinates for transformation
     * @param angle angle of rotation: -360° .. 0° .. +360°
     * @param center центр фигуры
     */
    public static <TCollection extends Collection<PointDouble>> TCollection rotateCollection(TCollection coords, double angle, PointDouble center) {
        angle = toRadian(angle);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        coords.forEach(p -> {
            p.x -= center.x;
            p.y -= center.y;
            double x = (p.x * cos) - (p.y * sin);
            double y = (p.x * sin) + (p.y * cos);
            p.x = x + center.x;
            p.y = y + center.y;
        });
        return coords;
    }

    /** adds an offset to each point */
    public static Stream<PointDouble> move(Stream<PointDouble> coords, PointDouble offset) {
        return coords.map(p -> new PointDouble(p.x + offset.x, p.y + offset.y));
    }

    /** adds an offset to each point. !!Modify existed collection!! */
    public static <TCollection extends Collection<PointDouble>> TCollection moveCollection(TCollection coords, PointDouble offset) {
        coords.forEach(p -> {
            p.x += offset.x;
            p.y += offset.y;
        });
        return coords;
    }

    /** Повернуть / выровнять заданную фигуру по указанной грани относительно X координаты. <br>
     * (Узнаю насколько повёрнута грань и разворачиваю всю фигуру в обратную сторону)
     * @param coords Фигура заданная координатами
     * @param sideNum Номер грани фигуры (начиная с 1)
     * @param center центр вращения
     * @param alignmentAngle угол на который необходимо выровнять (в градусах)
     * @return Координаты выровняной фигуры
     */
    public static Stream<PointDouble> rotateBySide(Stream<PointDouble> coords, int sideNum, PointDouble center, double alignmentAngle) {
        List<PointDouble> list = coords.collect(Collectors.toList());
        PointDouble p1 = list.get(sideNum - 1);
        PointDouble p2 = list.get(sideNum);

        double dx = p2.x - p1.x; // cathetus x
        double dy = p2.y - p1.y; // cathetus y
        double h = Math.sqrt(dx * dx + dy * dy); // hypotenuse

        double cos = dx / h;
        double angle = toDegrees(Math.acos(cos));
        if (angle < 0)
            angle += 360;
        if (dy < 0)
            angle = 360 - angle;

        return rotate(list.stream(), -angle + alignmentAngle, center);
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
