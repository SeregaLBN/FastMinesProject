package fmg.core.mosaic;

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;

import fmg.common.Logger;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

public final class MosaicHelper {

    private MosaicHelper() {}

    public static final double AREA_PRECISION = 0.001;

    private static final String getPackageName() {
        Package pkg = MosaicHelper.class.getPackage();
        if (pkg != null)
            return pkg.getName();
        String[] arr = MosaicHelper.class.getName().split("\\.");
        String name = "";
        for (int i=0; i<arr.length-1; i++) {
            if (!name.isEmpty())
                name += '.';
            name += arr[i];
        }
        return name;
    }

    /** Создать экземпляр атрибута для конкретного типа мозаики */
    public static final BaseCell.BaseAttribute createAttributeInstance(EMosaic mosaicType) {
//        switch (mosaicType) {
//        case eMosaicTriangle1        : return new Triangle1        .AttrTriangle1        (area);
//        case eMosaicTriangle2        : return new Triangle2        .AttrTriangle2        (area);
//        case eMosaicTriangle3        : return new Triangle3        .AttrTriangle3        (area);
//        case eMosaicTriangle4        : return new Triangle4        .AttrTriangle4        (area);
//        case eMosaicSquare1          : return new Square1          .AttrSquare1          (area);
//        case eMosaicSquare2          : return new Square2          .AttrSquare2          (area);
//        case eMosaicParquet1         : return new Parquet1         .AttrParquet1         (area);
//        case eMosaicParquet2         : return new Parquet2         .AttrParquet2         (area);
//        case eMosaicTrapezoid1       : return new Trapezoid1       .AttrTrapezoid1       (area);
//        case eMosaicTrapezoid2       : return new Trapezoid2       .AttrTrapezoid2       (area);
//        case eMosaicTrapezoid3       : return new Trapezoid3       .AttrTrapezoid3       (area);
//        case eMosaicRhombus1         : return new Rhombus1         .AttrRhombus1         (area);
//        case eMosaicQuadrangle1      : return new Quadrangle1      .AttrQuadrangle1      (area);
//        case eMosaicPenrousePeriodic1: return new PenrousePeriodic1.AttrPenrousePeriodic1(area);
//        case eMosaicPentagonT24      : return new PentagonT24      .AttrPentagonT24      (area);
//        case eMosaicPentagonT5       : return new PentagonT5       .AttrPentagonT5       (area);
//        case eMosaicPentagonT10      : return new PentagonT10      .AttrPentagonT10      (area);
//        case eMosaicHexagon1         : return new Hexagon1         .AttrHexagon1         (area);
//        case eMosaicTrSq1            : return new TrSq1            .AttrTrSq1            (area);
//        case eMosaicTrSq2            : return new TrSq2            .AttrTrSq2            (area);
//        case eMosaicSqTrHex          : return new SqTrHex          .AttrSqTrHex          (area);
//        default:
//            throw new RuntimeException("Unknown type "+mosaicType);
//        }

        try {
            String className = getPackageName() + ".cells." + mosaicType.getMosaicClassName() + "$Attr"+mosaicType.getMosaicClassName();
            @SuppressWarnings("unchecked")
            Class<? extends BaseCell.BaseAttribute> cellAttrClass = (Class<? extends BaseCell.BaseAttribute>)Class.forName(className);
            Constructor<? extends BaseCell.BaseAttribute> constructor = cellAttrClass.getConstructor(); //(Constructor<? extends BaseAttribute>) cellClass.getConstructors()[0]; //
            BaseCell.BaseAttribute attr = constructor.newInstance();
            return attr;
        } catch (Exception ex) {
            Logger.error("createAttributeInstance", ex);
            throw new RuntimeException("Unknown type " + mosaicType, ex);
        }
    }

    /** Создать экземпляр ячейки для конкретного типа мозаики */
    public static final BaseCell createCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord) {
//        switch (mosaicType) {
//        case eMosaicTriangle1        : return new Triangle1        ((Triangle1        .AttrTriangle1        ) attr, coord);
//        case eMosaicTriangle2        : return new Triangle2        ((Triangle2        .AttrTriangle2        ) attr, coord);
//        case eMosaicTriangle3        : return new Triangle3        ((Triangle3        .AttrTriangle3        ) attr, coord);
//        case eMosaicTriangle4        : return new Triangle4        ((Triangle4        .AttrTriangle4        ) attr, coord);
//        case eMosaicSquare1          : return new Square1          ((Square1          .AttrSquare1          ) attr, coord);
//        case eMosaicSquare2          : return new Square2          ((Square2          .AttrSquare2          ) attr, coord);
//        case eMosaicParquet1         : return new Parquet1         ((Parquet1         .AttrParquet1         ) attr, coord);
//        case eMosaicParquet2         : return new Parquet2         ((Parquet2         .AttrParquet2         ) attr, coord);
//        case eMosaicTrapezoid1       : return new Trapezoid1       ((Trapezoid1       .AttrTrapezoid1       ) attr, coord);
//        case eMosaicTrapezoid2       : return new Trapezoid2       ((Trapezoid2       .AttrTrapezoid2       ) attr, coord);
//        case eMosaicTrapezoid3       : return new Trapezoid3       ((Trapezoid3       .AttrTrapezoid3       ) attr, coord);
//        case eMosaicRhombus1         : return new Rhombus1         ((Rhombus1         .AttrRhombus1         ) attr, coord);
//        case eMosaicQuadrangle1      : return new Quadrangle1      ((Quadrangle1      .AttrQuadrangle1      ) attr, coord);
//        case eMosaicPenrousePeriodic1: return new PenrousePeriodic1((PenrousePeriodic1.AttrPenrousePeriodic1) attr, coord);
//        case eMosaicPentagonT24      : return new PentagonT24      ((PentagonT24      .AttrPentagonT24      ) attr, coord);
//        case eMosaicPentagonT5       : return new PentagonT5       ((PentagonT5       .AttrPentagonT5       ) attr, coord);
//        case eMosaicPentagonT10      : return new PentagonT10      ((PentagonT10      .AttrPentagonT10      ) attr, coord);
//        case eMosaicHexagon1         : return new Hexagon1         ((Hexagon1         .AttrHexagon1         ) attr, coord);
//        case eMosaicTrSq1            : return new TrSq1            ((TrSq1            .AttrTrSq1            ) attr, coord);
//        case eMosaicTrSq2            : return new TrSq2            ((TrSq2            .AttrTrSq2            ) attr, coord);
//        case eMosaicSqTrHex          : return new SqTrHex          ((SqTrHex          .AttrSqTrHex          ) attr, coord);
//        default:
//            throw new RuntimeException("Unknown type "+mosaicType);
//        }

        try {
            String className = getPackageName() + ".cells." + mosaicType.getMosaicClassName();
            @SuppressWarnings("unchecked")
            Class<? extends BaseCell> cellClass = (Class<? extends BaseCell>)Class.forName(className);

            Constructor<? extends BaseCell> constructor = cellClass.getConstructor(attr.getClass(), coord.getClass()); // cellClass.getConstructors()[0];
            BaseCell cell = constructor.newInstance(attr, coord);
            cell.init();
            return cell;
        } catch (Exception ex) {
            Logger.error("createCellInstance", ex);
            throw new RuntimeException("Unknown type " + mosaicType, ex);
        }
    }

    /**
     * Поиск больше-меньше
     * @param baseDelta - начало дельты приращения
     * @param func - ф-ция сравнения
     * @return что найдено
     */
    static int FinderI(int baseDelta, Comparable<Integer> func) {
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
    static double FinderD(double baseDelta, Comparable<Double> func) {
        double res = baseDelta;
        double d = baseDelta;
        boolean deltaUp = true;
        do {
            double cmp = func.compareTo(res);
            if (DoubleExt.hasMinDiff(cmp, 0))
                break;
            if ((d < AREA_PRECISION) && DoubleExt.hasMinDiff(cmp, -1))
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
    private static double findAreaBySize(BaseCell.BaseAttribute cellAttr, final Matrisize mosaicSizeField, final SizeDouble sizeClientIn, SizeDouble sizeClientOut) {
        // сделал приватным, т.к. неявно меняет свойства параметра 'cellAttr'

        if (Double.isNaN(sizeClientIn.height) || Double.isNaN(sizeClientIn.width))
            throw new IllegalArgumentException("sizeClient must be defined");
        if (sizeClientIn.height <= 0 || sizeClientIn.width <= 0)
            throw new InvalidParameterException("sizeClientIn must be positive");

        final SizeDouble sizeIter = new SizeDouble();
        int[] iterations = { 0 };
        double res = FinderD(2000, (Comparable<Double>)area -> {
            assert(++iterations[0] < 100);
            cellAttr.setArea(area);
            SizeDouble tmp = cellAttr.getSize(mosaicSizeField);
            sizeIter.width = tmp.width;
            sizeIter.height = tmp.height;
            if (DoubleExt.hasMinDiff(sizeIter.width, sizeClientIn.width) &&
                (sizeIter.width  <= sizeClientIn.width) &&
                (sizeIter.height <= sizeClientIn.height))
                return 0;
            if ((sizeIter.width  <= sizeClientIn.width) &&
                (sizeIter.height <= sizeClientIn.height) &&
                DoubleExt.hasMinDiff(sizeIter.height, sizeClientIn.height))
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

        BaseCell.BaseAttribute cellAttr = createAttributeInstance(mosaicType);
        cellAttr.setArea(area);
        final Matrisize result = new Matrisize();
        FinderI(2000, (Comparable<Integer>)newWidth -> {
            result.m = newWidth;
            SizeDouble sizeWnd = cellAttr.getSize(result);
            if (DoubleExt.hasMinDiff(sizeWnd.width, sizeClient.width))
                return 0;
            if (sizeWnd.width <= sizeClient.width)
                return -1;
            return +1;
        });
        FinderI(2000, (Comparable<Integer>)newHeight -> {
            result.n = newHeight;
            SizeDouble sizeWnd = cellAttr.getSize(result);
            if (DoubleExt.hasMinDiff(sizeWnd.height, sizeClient.height))
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
        return findAreaBySize(createAttributeInstance(mosaicType), mosaicSizeField, sizeClientIn, sizeClientOut);
    }

    /** The size in pixels where to place the matrix */
    public static SizeDouble getSize(EMosaic mosaicType, double area, Matrisize mosaicSizeField) {
        BaseCell.BaseAttribute attr = createAttributeInstance(mosaicType);
        attr.setArea(area);
        return attr.getSize(mosaicSizeField);
    }

}
