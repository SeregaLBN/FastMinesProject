package fmg.core.mosaic;

import java.security.InvalidParameterException;

import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.*;
import fmg.core.mosaic.shape.*;
import fmg.core.types.EMosaic;

public final class MosaicHelper {

    private MosaicHelper() {}

    public static final double AREA_PRECISION = 0.001;

    /** Создать экземпляр атрибута для конкретного типа мозаики */
    public static final BaseShape createShapeInstance(EMosaic mosaicType) {
        switch (mosaicType) {
        case eMosaicTriangle1        : return new ShapeTriangle1        ();
        case eMosaicTriangle2        : return new ShapeTriangle2        ();
        case eMosaicTriangle3        : return new ShapeTriangle3        ();
        case eMosaicTriangle4        : return new ShapeTriangle4        ();
        case eMosaicSquare1          : return new ShapeSquare1          ();
        case eMosaicSquare2          : return new ShapeSquare2          ();
        case eMosaicParquet1         : return new ShapeParquet1         ();
        case eMosaicParquet2         : return new ShapeParquet2         ();
        case eMosaicTrapezoid1       : return new ShapeTrapezoid1       ();
        case eMosaicTrapezoid2       : return new ShapeTrapezoid2       ();
        case eMosaicTrapezoid3       : return new ShapeTrapezoid3       ();
        case eMosaicRhombus1         : return new ShapeRhombus1         ();
        case eMosaicQuadrangle1      : return new ShapeQuadrangle1      ();
        case eMosaicPenrousePeriodic1: return new ShapePenrousePeriodic1();
        case eMosaicPentagonT24      : return new ShapePentagonT24      ();
        case eMosaicPentagonT5       : return new ShapePentagonT5       ();
        case eMosaicPentagonT10      : return new ShapePentagonT10      ();
        case eMosaicHexagon1         : return new ShapeHexagon1         ();
        case eMosaicTrSq1            : return new ShapeTrSq1            ();
        case eMosaicTrSq2            : return new ShapeTrSq2            ();
        case eMosaicSqTrHex          : return new ShapeSqTrHex          ();
        default:
            throw new RuntimeException("Unknown type " + mosaicType);
        }
    }

    /** Создать экземпляр ячейки для конкретного типа мозаики */
    public static final BaseCell createCellInstance(BaseShape shape, EMosaic mosaicType, Coord coord) {
        BaseCell cell = createCell(shape, mosaicType, coord);
        cell.init();
        return cell;
    }

    private static final BaseCell createCell(BaseShape shape, EMosaic mosaicType, Coord coord) {
        switch (mosaicType) {
        case eMosaicTriangle1        : return new Triangle1        ((ShapeTriangle1        )shape, coord);
        case eMosaicTriangle2        : return new Triangle2        ((ShapeTriangle2        )shape, coord);
        case eMosaicTriangle3        : return new Triangle3        ((ShapeTriangle3        )shape, coord);
        case eMosaicTriangle4        : return new Triangle4        ((ShapeTriangle4        )shape, coord);
        case eMosaicSquare1          : return new Square1          ((ShapeSquare1          )shape, coord);
        case eMosaicSquare2          : return new Square2          ((ShapeSquare2          )shape, coord);
        case eMosaicParquet1         : return new Parquet1         ((ShapeParquet1         )shape, coord);
        case eMosaicParquet2         : return new Parquet2         ((ShapeParquet2         )shape, coord);
        case eMosaicTrapezoid1       : return new Trapezoid1       ((ShapeTrapezoid1       )shape, coord);
        case eMosaicTrapezoid2       : return new Trapezoid2       ((ShapeTrapezoid2       )shape, coord);
        case eMosaicTrapezoid3       : return new Trapezoid3       ((ShapeTrapezoid3       )shape, coord);
        case eMosaicRhombus1         : return new Rhombus1         ((ShapeRhombus1         )shape, coord);
        case eMosaicQuadrangle1      : return new Quadrangle1      ((ShapeQuadrangle1      )shape, coord);
        case eMosaicPenrousePeriodic1: return new PenrousePeriodic1((ShapePenrousePeriodic1)shape, coord);
        case eMosaicPentagonT24      : return new PentagonT24      ((ShapePentagonT24      )shape, coord);
        case eMosaicPentagonT5       : return new PentagonT5       ((ShapePentagonT5       )shape, coord);
        case eMosaicPentagonT10      : return new PentagonT10      ((ShapePentagonT10      )shape, coord);
        case eMosaicHexagon1         : return new Hexagon1         ((ShapeHexagon1         )shape, coord);
        case eMosaicTrSq1            : return new TrSq1            ((ShapeTrSq1            )shape, coord);
        case eMosaicTrSq2            : return new TrSq2            ((ShapeTrSq2            )shape, coord);
        case eMosaicSqTrHex          : return new SqTrHex          ((ShapeSqTrHex          )shape, coord);
        default:
            throw new RuntimeException("Unknown type " + mosaicType);
        }
    }

    /**
     * Поиск больше-меньше
     * @param baseDelta - начало дельты приращения
     * @param func - ф-ция сравнения
     * @return что найдено
     */
    static int finderI(int baseDelta, Comparable<Integer> func) {
        double res = baseDelta;
        double d = baseDelta;
        boolean deltaUp = true;
        do {
            int cmp = func.compareTo((int)res);
            // Example:
            // func comparable(x) -> return x==1000 ? 0: x<1000 ? -1 : +1;
            // init data:  res=100 d=100
            //  iter 0: cmp=+1; d=d*2=200; res=res+d=300
            //  iter 1: cmp=+1; d=d*2=400; res=res+d=700
            //  iter 2: cmp=+1; d=d*2=800; res=res+d=1500
            //  iter 3: cmp=-1; d=d/2=400; res=res-d=1100
            //  iter 4: cmp=-1; d=d/2=200; res=res-d=900
            //  iter 5: cmp=+1; d=d/2=100; res=res+d=1000 - finded!!!
            if (cmp == 0)
                break;
            if ((d < 1) && (cmp == -1))
                break;

            boolean resultUp = (cmp < 0);
            deltaUp = deltaUp && resultUp;
            if (deltaUp)
                d *= 2;
            else
                d /= 2;
            if (resultUp)
                res += d;
            else
                res -= d;
        } while (true);
        return (int)res;
    }

    /**
     * Поиск больше-меньше
     * @param baseDelta - начало дельты приращения
     * @param func - ф-ция сравнения
     * @return что найдено
     */
    static double finderD(double baseDelta, Comparable<Double> func) {
        double res = baseDelta;
        double d = baseDelta;
        boolean deltaUp = true;
        do {
            double cmp = func.compareTo(res);
            if (DoubleExt.almostEquals(cmp, 0))
                break;
            if ((d < AREA_PRECISION) && DoubleExt.almostEquals(cmp, -1))
                break;

            boolean resultUp = (cmp < 0);
            deltaUp = deltaUp && resultUp;
            if (deltaUp)
                d *= 2;
            else
                d /= 2;
            if (resultUp)
                res += d;
            else
                res -= d;
        } while (true);
        return res;
    }

    /** узнаю мах размер площади ячеек мозаики, при котором вся мозаика вмещается в заданную область
     * @param mosaicSizeField - интересуемый размер (в ячейках) поля мозаики
     * @param sizeClientIn - размер области (в пикселях) в которую должна вписаться мозаика
     * @param sizeClientOut - размер области (в пикселях) в которую реально впишется мозаика
     * @return площадь ячейки
     */
    private static double findAreaBySize(BaseShape shape, final Matrisize mosaicSizeField, final SizeDouble sizeClientIn, SizeDouble sizeClientOut) {
        // сделал приватным, т.к. неявно меняет свойства параметра 'shape'

        if (Double.isNaN(sizeClientIn.height) || Double.isNaN(sizeClientIn.width))
            throw new IllegalArgumentException("sizeClient must be defined");
        if (sizeClientIn.height <= 0 || sizeClientIn.width <= 0)
            throw new InvalidParameterException("sizeClientIn must be positive");

        final SizeDouble sizeIter = new SizeDouble();
        int[] iterations = { 0 };
        double res = finderD(2000, (Comparable<Double>)area -> {
            assert(++iterations[0] < 100);
            shape.setArea(area);
            SizeDouble tmp = shape.getSize(mosaicSizeField);
            sizeIter.width = tmp.width;
            sizeIter.height = tmp.height;
            if (DoubleExt.almostEquals(sizeIter.width, sizeClientIn.width) &&
                (sizeIter.width  <= sizeClientIn.width) &&
                (sizeIter.height <= sizeClientIn.height))
                return 0;
            if ((sizeIter.width  <= sizeClientIn.width) &&
                (sizeIter.height <= sizeClientIn.height) &&
                DoubleExt.almostEquals(sizeIter.height, sizeClientIn.height))
                return 0;
            if ((sizeIter.width < sizeClientIn.width) &&
                (sizeIter.height < sizeClientIn.height))
                return -1;
            return +1;
        });
        assert(sizeIter.width  <= sizeClientIn.width);
        assert(sizeIter.height <= sizeClientIn.height);
        sizeClientOut.width = sizeIter.width;
        sizeClientOut.height = sizeIter.height;
        return res;
    }

    /** узнаю max размер поля мозаики, при котором вся мозаика вмещается в в заданную область
     * @param mosaicType тип мозаики
     * @param area интересуемая площадь ячеек мозаики
     * @param sizeClient размер области (в пикселях) в которую должна вписаться мозаика
     * @return размер поля мозаики
     */
    public static Matrisize findSizeByArea(EMosaic mosaicType, double area, SizeDouble sizeClient) {
        if (Double.isNaN(sizeClient.height) || Double.isNaN(sizeClient.width))
            throw new IllegalArgumentException("sizeClient must be defined");

        if (sizeClient.height <= 0 || sizeClient.width <= 0)
            throw new IllegalArgumentException("sizeClient must be positive");

        BaseShape shape = createShapeInstance(mosaicType);
        shape.setArea(area);
        final Matrisize result = new Matrisize();
        finderI(2000, (Comparable<Integer>)newWidth -> {
            result.m = newWidth;
            SizeDouble sizeWnd = shape.getSize(result);
            if (DoubleExt.almostEquals(sizeWnd.width, sizeClient.width))
                return 0;
            if (sizeWnd.width <= sizeClient.width)
                return -1;
            return +1;
        });
        finderI(2000, (Comparable<Integer>)newHeight -> {
            result.n = newHeight;
            SizeDouble sizeWnd = shape.getSize(result);
            if (DoubleExt.almostEquals(sizeWnd.height, sizeClient.height))
                return 0;
            if (sizeWnd.height < sizeClient.height)
                return -1;
            return +1;
        });
        return result;
    }

    /** узнаю мах размер площади ячеек мозаики, при котором вся мозаика вмещается в заданную область
     * @param mosaicType тип мозаики
     * @param mosaicSizeField интересуемый размер (в ячейках) поля мозаики
     * @param sizeClientIn размер области (в пикселях) в которую должна вписаться мозаика
     * @param sizeClientOut размер области (в пикселях) в которую реально впишется мозаика
     * @return площадь ячейки
     */
    public static double findAreaBySize(EMosaic mosaicType, Matrisize mosaicSizeField, SizeDouble sizeClientIn, SizeDouble sizeClientOut) {
        return findAreaBySize(createShapeInstance(mosaicType), mosaicSizeField, sizeClientIn, sizeClientOut);
    }

    /** The size in pixels where to place the matrix */
    public static SizeDouble getSize(EMosaic mosaicType, double area, Matrisize mosaicSizeField) {
        BaseShape shape = createShapeInstance(mosaicType);
        shape.setArea(area);
        return shape.getSize(mosaicSizeField);
    }

}
