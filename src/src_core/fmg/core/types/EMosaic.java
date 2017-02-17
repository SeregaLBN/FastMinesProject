package fmg.core.types;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.Matrisize;

/* Перечень мозаик */

/** тип мозаики */
public enum EMosaic {
   // ============== Triangles ==============
   /** Triangle 60°-60°-60° */
   eMosaicTriangle1            (300),
   /** Triangle 60°-60°-60° (offset) */
   eMosaicTriangle2            (301),
   /** Triangle 45°-90°-45° */
   eMosaicTriangle3            (302),
   /** Triangle 30°-30°-120° */
   eMosaicTriangle4            (303),

   // ============== Quadrangles ==============
   /** Square 1 */
   eMosaicSquare1              (400),
   /** Square 2 (offset) */
   eMosaicSquare2              (401),
   /** Parquet №1 - 'Herring-bone' - Паркет в елку */
   eMosaicParquet1             (402),
   /** Parquet №2 */
   eMosaicParquet2             (403),
   /** Trapezoid 1 - 3 трапеции, составляющие равносторонний треугольник*/
   eMosaicTrapezoid1           (404),
   /** Trapezoid 2 - 3 трапеции, составляющие равносторонний треугольник*/
   eMosaicTrapezoid2           (405),
   /** Trapezoid 3 - 8 трапеций, складывающихся в шестигранник*/
   eMosaicTrapezoid3           (406),
   /** Rhombus */
   eMosaicRhombus1             (407),
   /** Quadrilateral 120°-90°-60°-90° */
   eMosaicQuadrangle1          (408),
   /**
    * Penrose tilings (rombus 72°-108° & 36°- 144°) - one of the periodic variations.
    * <li><a href="http://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D0%B7%D0%B0%D0%B8%D0%BA%D0%B0_%D0%9F%D0%B5%D0%BD%D1%80%D0%BE%D1%83%D0%B7%D0%B0">ru wiki</>
    * <li><a href="http://en.wikipedia.org/wiki/Penrose_tiling">en wiki</>
    */
   eMosaicPenrousePeriodic1    (409),

   // ============== Pentagons ==============
   // http://en.wikipedia.org/wiki/Pentagonal_tiling

   /** eMosaicPentagonT24 */
   eMosaicPentagonT24          (500),
   /* eMosaicPentagonT5 */
   eMosaicPentagonT5           (501),
   /** eMosaicPentagonT10 */
   eMosaicPentagonT10          (502),

   // ============== Hexagons ==============
   /** Hexagon */
   eMosaicHexagon1             (600),

   // ============== Others ==============
   /** Square-Triangle 1 */
   eMosaicTrSq1                (700),
   /** Square-Triangle 2 */
   eMosaicTrSq2                (701),
   /** Square-Triangle-Hexagon */
   eMosaicSqTrHex              (702);

   private final int index;
   private EMosaic(int index) {
      this.index = index;
   }

   public static EMosaic fromOrdinal(int ordinal) {
      if ((ordinal < 0) || (ordinal >= EMosaic.values().length))
         throw new IndexOutOfBoundsException("Invalid ordinal");
      return EMosaic.values()[ordinal];
   }

   public static EMosaic fromIndex(int index) {
      for (EMosaic item: EMosaic.values())
         if (item.index == index)
            return item;
      throw new IllegalArgumentException("Invalid paramenter value " + index);
   }
   public int getIndex() { return index; }

   /** Описание для пользователя */
   public String getDescription(boolean small) {
      if (small)
         return this.toString().substring(7);

      switch (this) {
      case eMosaicTriangle1        : return "Triangle 60°-60°-60°";
      case eMosaicTriangle2        : return "Triangle 60°-60°-60° (offset)";
      case eMosaicTriangle3        : return "Triangle 45°-90°-45°";
      case eMosaicTriangle4        : return "Triangle 30°-30°-120°";
      case eMosaicSquare1          : return "Square 1";
      case eMosaicSquare2          : return "Square 2 (offset)";
      case eMosaicParquet1         : return "Rectangle 1 (Parquet 'Herring-bone')";
      case eMosaicParquet2         : return "Rectangle 2";
      case eMosaicTrapezoid1       : return "Trapezoid 1";
      case eMosaicTrapezoid2       : return "Trapezoid 2";
      case eMosaicTrapezoid3       : return "Trapezoid 3";
      case eMosaicRhombus1         : return "Rhombus";
      case eMosaicQuadrangle1      : return "Quadrilateral 120°-90°-60°-90°";
      case eMosaicPenrousePeriodic1: return "Penrose periodic tilings";// (rombus 72°-108° & 36°- 144°)";
      case eMosaicPentagonT24      : return "Pentagon (type 2 and 4)";
      case eMosaicPentagonT5       : return "Pentagon (type 5)";
      case eMosaicPentagonT10      : return "Pentagon (type 10)";
      case eMosaicHexagon1         : return "Hexagon";
      case eMosaicTrSq1            : return "Square-Triangle 1";
      case eMosaicTrSq2            : return "Square-Triangle 2";
      case eMosaicSqTrHex          : return "Square-Triangle-Hexagon";
      }
      return getClass().getName()+".getDescription: Mosaic '" + this.toString() + "' not implemented";
   }
   /** Перечень описаний мозаик */
   public static List<String> getDescriptionValues() {
      List<String> res = new ArrayList<String>(EMosaic.values().length);
      for (EMosaic type: EMosaic.values())
         res.add(type.getDescription(false));
      return res;
   }
   public static EMosaic fromDescription(String description) {
      for (EMosaic type: EMosaic.values())
         if (type.getDescription(false).equals(description))
            return type;
      return null;
   }

   /** 'Быстрый' код - уникальный номер мозаики для быстрого ввода.
    * <ul> Идея:
    * <li> треугольники начинаются с цифры 3
    * <li> четырёхугольники - с цифры 4
    * <li> пятиугольники - с цифры 5
    * <li> шестиугольники - с цифры 6
    * <li> прочие - с цифры 7
    */
   public int getFastCode() {
      switch (this) {
      case eMosaicTriangle1        : return 30;
      case eMosaicTriangle2        : return 31;
      case eMosaicTriangle3        : return 32;
      case eMosaicTriangle4        : return 33;
      case eMosaicSquare1          : return 40;
      case eMosaicSquare2          : return 41;
      case eMosaicParquet1         : return 42;
      case eMosaicParquet2         : return 43;
      case eMosaicTrapezoid1       : return 44;
      case eMosaicTrapezoid2       : return 45;
      case eMosaicTrapezoid3       : return 46;
      case eMosaicRhombus1         : return 47;
      case eMosaicQuadrangle1      : return 48;
      case eMosaicPenrousePeriodic1: return 49;
      case eMosaicPentagonT24      : return 50;
      case eMosaicPentagonT5       : return 51;
      case eMosaicPentagonT10      : return 52;
      case eMosaicHexagon1         : return 60;
      case eMosaicTrSq1            : return 70;
      case eMosaicTrSq2            : return 71;
      case eMosaicSqTrHex          : return 72;
      }
      System.err.println(getClass().getName()+".getFastCode: Mosaic '" + this.toString() + "' not implemented");
      return 0;
   }
   /** Перечень 'быстрых' кодов */
   public static List<Integer> getFastCodeValues() {
      List<Integer> res = new ArrayList<Integer>(EMosaic.values().length);
      for (EMosaic val: EMosaic.values())
         res.add(val.getFastCode());
      return res;
   }
   public static EMosaic fromFastCode(int fastCode) {
      for (EMosaic type: EMosaic.values())
         if (fastCode == type.getFastCode())
            return type;
      return null;
   }

   public String getMosaicClassName() {
      return this.toString().substring(7);
   }

   /** Для рисование иконки: минимальный размер поля, по которому будет визуально ясно, что это за мозаика... */
   public Matrisize sizeIcoField(boolean smallSize) {
      Matrisize res = new Matrisize();
      switch (this) {
      case eMosaicTriangle1        : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicTriangle2        : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicTriangle3        : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicTriangle4        : res.m = 4; res.n = smallSize ? 4 : 5; break;
      case eMosaicSquare1          : res.m = smallSize ? 2 : 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicSquare2          : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicParquet1         : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicParquet2         : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicTrapezoid1       : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicTrapezoid2       : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicTrapezoid3       : res.m = 4; res.n = smallSize ? 2 : 3; break;
      case eMosaicRhombus1         : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicQuadrangle1      : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicPenrousePeriodic1: res.m = 3; res.n = smallSize ? 3 : 4; break;
      case eMosaicPentagonT24      : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicPentagonT5       : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicPentagonT10      : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case eMosaicHexagon1         : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case eMosaicTrSq1            : res.m = 4; res.n = smallSize ? 4 : 5; break;
      case eMosaicTrSq2            : res.m = 4; res.n = smallSize ? 4 : 5; break;
      case eMosaicSqTrHex          : res.m = 4; res.n = smallSize ? 4 : 5; break;
      default:
         System.err.println("EMosaic.SizeIcoField: Mosaic '" + this + "' not implemented");
      }
      return res;
   }
}