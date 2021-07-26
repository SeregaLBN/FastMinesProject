package fmg.core.types;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;

public enum ESkillLevel {

    eBeginner,
    eAmateur,
    eProfi,
    eCrazy,
    eCustom;

    /** коэффициент уровня сложности в зависимости от типа мозаики - чем больше, тем сложнее */
    private static final Map<EMosaic, Double> mosaicCoefficient; // skill level coefficient
    static {
        mosaicCoefficient = new EnumMap<>(EMosaic.class);
        for (EMosaic mosaicType : EMosaic.values()) {
            BaseCell.BaseAttribute attr = MosaicHelper.createAttributeInstance(mosaicType);

            // variant 1 - сложность в зависимости от кол-ва пересечений ячеек в одной точке
//            mosaicCoefficient.info(mosaicType, attr.getVertexIntersection());

            // variant 2 - сложность в зависимости от кол-ва соседних ячеек
//            int cntDir = attr.GetDirectionCount();
//            int neighbors = 0;
//            for (int i=0; i<cntDir; i++)
//               neighbors += attr.getNeighborNumber(i);
//            mosaicCoefficient.info(mosaicType, ((double)neighbors)/cntDir);

            // variant 3 - сложность в зависимости от кол-ва соседних ячеек и кол-ва точек пересечения
            double neighbors = IntStream.range(0, attr.getDirectionCount())
                  .map(i -> attr.getNeighborNumber(i))
                  .average().getAsDouble();
            mosaicCoefficient.put(mosaicType, attr.getVertexIntersection()/neighbors);

//            Logger.info(attr.getClass().getSimpleName() + ": " + mosaicCoefficient.get(mosaicType));
        }

        // x*y * coefficient / mosaicCoefficient  = 15
        // 15*mosaicCoefficient/(x*y)  = coefficient
//        Logger.info(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 15  / (10*10));
//        Logger.info(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 54  / (20*15));
//        Logger.info(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 126 / (30*20));
//        Logger.info(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 281 / (45*25));

//        System.exit(0);
    }
/*
         Sq1  Tring2       PentT5  Trapez1   Pent24   Tring1
Neighbor     8     8          8       8        7        12
VertexInter   4     3.75      3.6     3.6      3.4       6
eBeginner    15
eAmateur     54
eProfi      126
eCrazy      281
*/
    /** коэффициент уровня сложности */
    private double getCoefficient() {
        // variant 1
//        switch (this) {
//        case eBeginner: return 0.6;
//        case eAmateur : return 0.72;
//        case eProfi   : return 0.84;
//        case eCrazy   : return 0.9991111111111111;
//        }

        // variant 2
//        switch (this) {
//        case eBeginner: return 1.2;
//        case eAmateur : return 1.44;
//        case eProfi   : return 1.68;
//        case eCrazy   : return 1.9982222222222221;
//        default: break;
//        }

        // variant 3
        switch (this) {
        case eBeginner: return 0.075;
        case eAmateur : return 0.09;
        case eProfi   : return 0.105;
        case eCrazy   : return 0.12488888888888888;
        default: break;
        }
        throw new IllegalArgumentException("Invalid method call. Для уровня сложности '"+this+"' нет коэффициента сложности.");
    }

    /** размеры полей */
    public Matrisize getDefaultSize() {
        switch (this) {
        case eBeginner: return new Matrisize(10, 10); // 15
        case eAmateur : return new Matrisize(20, 15); // 54
        case eProfi   : return new Matrisize(30, 20); // 126
        case eCrazy   : return new Matrisize(45, 25); // 281
        default: break;
        }
        throw new IllegalArgumentException("Invalid method call. Для уровня сложности '"+this+"' нет размера поля по-умолчанию.");
    }

    /** Узнать кол-во мин на размере поля по-умолчанию */
    public int getNumberMines(EMosaic eMosaic) {
        return getNumberMines(eMosaic, this.getDefaultSize());
    }

    /** Узнать кол-во мин на заданном размере поля */
    public int getNumberMines(EMosaic eMosaic, Matrisize customSizeMosaic) {
        if (customSizeMosaic == null)
            throw new IllegalArgumentException("customSizeMosaic must be not null");
        if (this == eCustom)
            throw new IllegalArgumentException("Для уровня сложности '"+this+"' кол-во мин задаётся явно, а не расчитывается...");

        return (int) (customSizeMosaic.m * customSizeMosaic.n * getCoefficient() / mosaicCoefficient.get(eMosaic));
    }

    public String getDescription() {
        if (this == eProfi)
            return "Professional";
        return this.toString().substring(1);
    }

    public static ESkillLevel fromOrdinal(int ordinal) {
        if ((ordinal < 0) || (ordinal >= ESkillLevel.values().length))
            throw new IndexOutOfBoundsException("Invalid ordinal");
        return ESkillLevel.values()[ordinal];
    }

    public char unicodeChar() {
        switch (this) {
        // http://unicode-table.com/sets/stars-symbols/                  // http://unicode-table.com/en/sets/emoji/
        // http://unicode-table.com/search/?q=star                       // http://unicode-table.com/en/1F63D/
        case eBeginner: return '\u2736'; // '✶'; //                               "😺"; // \u1F638
        case eAmateur : return '\u2737'; // '✷'; //                               "😸"; // \u1F63A
        case eProfi   : return '\u2738'; // '✸'; //                               "😻"; // \u1F63B
        case eCrazy   : return '\u2739'; // '✹'; //                               "😼"; // \u1F63C
        case eCustom  : return '\u273B'; // '✻'; //                               "😽"; // \u1F63D
        }
        throw new IllegalArgumentException("Invalid paramenter value " + this);
    }

    public Matrisize sizeTileField(EMosaic mosaicType) {
        Matrisize size = mosaicType.sizeIcoField(true);
        switch (this) {
        case eBeginner: size.m += 0; size.n += 0; break;
        case eAmateur : size.m += 1; size.n += 0; break;
        case eProfi   : size.m += 1; size.n += 1; break;
        case eCrazy   : size.m += 2; size.n += 1; break;
        case eCustom:
        default:
            throw new IllegalArgumentException("Invalid paramenter value " + mosaicType);
        }
        return size;
    }

    public static ESkillLevel calcSkillLevel(EMosaic mosaicType, Matrisize sizeField, int minesCount) {
        if (sizeField.equals(eBeginner.getDefaultSize()) && (minesCount == eBeginner.getNumberMines(mosaicType))) return eBeginner;
        if (sizeField.equals(eAmateur .getDefaultSize()) && (minesCount == eAmateur .getNumberMines(mosaicType))) return eAmateur;
        if (sizeField.equals(eProfi   .getDefaultSize()) && (minesCount == eProfi   .getNumberMines(mosaicType))) return eProfi;
        if (sizeField.equals(eCrazy   .getDefaultSize()) && (minesCount == eCrazy   .getNumberMines(mosaicType))) return eCrazy;
        return eCustom;
    }

}
