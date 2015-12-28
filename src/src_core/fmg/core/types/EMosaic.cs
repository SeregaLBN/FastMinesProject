using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common.geom;

namespace fmg.core.types {

/* Перечень мозаик */

/// <summary> тип мозаики </summary>
public enum EMosaic {
   // ============== Triangles ==============
   /// <summary> Triangle 60°-60°-60° </summary>
   eMosaicTriangle1           = (300),

   /// <summary> Triangle 60°-60°-60° (offset) </summary>
   eMosaicTriangle2           = (301),

   /// <summary> Triangle 45°-90°-45° </summary>
   eMosaicTriangle3           = (302),

   /// <summary> Triangle 30°-30°-120° </summary>
   eMosaicTriangle4           = (303),

   // ============== Quadrangles ==============
   /// <summary> Square 1 </summary>
   eMosaicSquare1             = (400),

   /// <summary> Square 2 (offset) </summary>
   eMosaicSquare2             = (401),

   /// <summary> Parquet №1 - 'Herring-bone' - Паркет в елку </summary>
   eMosaicParquet1            = (402),

   /// <summary> Parquet №2 </summary>
   eMosaicParquet2            = (403),

   /// <summary> Trapezoid 1 - 3 трапеции, составляющие равносторонний треугольник </summary>
   eMosaicTrapezoid1          = (404),

   /// <summary> Trapezoid 2 - 3 трапеции, составляющие равносторонний треугольник </summary>
   eMosaicTrapezoid2          = (405),

   /// <summary> Trapezoid 3 - 8 трапеций, складывающихся в шестигранник </summary>
   eMosaicTrapezoid3          = (406),

   /// <summary> Rhombus </summary>
   eMosaicRhombus1            = (407),

   /// <summary> Quadrilateral 120°-90°-60°-90° </summary>
   eMosaicQuadrangle1         = (408),

   /// <summary>
   ///   Penrose tilings (rombus 72°-108° & 36°- 144°) - one of the periodic variations.
   ///   <li><a href="http://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D0%B7%D0%B0%D0%B8%D0%BA%D0%B0_%D0%9F%D0%B5%D0%BD%D1%80%D0%BE%D1%83%D0%B7%D0%B0">ru wiki</>
   ///   <li><a href="http://en.wikipedia.org/wiki/Penrose_tiling">en wiki</>
   /// </summary>
   eMosaicPenrousePeriodic1   = (409),

   // ============== Pentagons ==============
   // http://en.wikipedia.org/wiki/Pentagonal_tiling

   /// <summary> Pentagon (type 2 and 4) </summary>
   eMosaicPentagonT24         = (500),

   /// <summary> Pentagon (type 5) </summary>
   eMosaicPentagonT5          = (501),

   /// <summary> Pentagon (type 10) </summary>
   eMosaicPentagonT10         = (502),

   // ============== Hexagons ==============
   /// <summary> Hexagon </summary>
   eMosaicHexagon1            = (600),

   // ============== Others ==============
   /// <summary> Square-Triangle 1 </summary>
   eMosaicTrSq1               = (700),

   /// <summary> Square-Triangle 2 </summary>
   eMosaicTrSq2               = (701),

   /// <summary> Square-Triangle-Hexagon </summary>
   eMosaicSqTrHex             = (702)
}

public static class EMosaicEx {
   private static readonly EMosaic[] EMosaicValues = (EMosaic[])Enum.GetValues(typeof(EMosaic));
   public static EMosaic[] GetValues() { return EMosaicValues; }

   public static int Ordinal(this EMosaic self) {
      var values = GetValues();
      for (var i = 0; i < values.Length; i++)
         if (values[i] == self)
            return i;
      throw new ArgumentException("Index not found");
   }

   public static EMosaic FromOrdinal(int ordinal) {
      var values = GetValues();
      if ((ordinal < 0) || (ordinal >= values.Length))
         throw new ArgumentOutOfRangeException("ordinal", "Invalid value " + ordinal);
      return values[ordinal];
   }

   public static EMosaic FromIndex(int index) {
        return (EMosaic)index;
   }

   public static int GetIndex(this EMosaic self) { return (int)self; }

   /// <summary>
   /// Описание для пользователя
   /// </summary>
   /// <param name="self"></param>
   /// <param name="small"></param>
   /// <returns></returns>
   public static String GetDescription(this EMosaic self, bool small) {
      if (small)
         return self.ToString().Substring(7);

      switch (self) {
      case EMosaic.eMosaicTriangle1        : return "Triangle 60°-60°-60°";
      case EMosaic.eMosaicTriangle2        : return "Triangle 60°-60°-60° (offset)";
      case EMosaic.eMosaicTriangle3        : return "Triangle 45°-90°-45°";
      case EMosaic.eMosaicTriangle4        : return "Triangle 30°-30°-120°";
      case EMosaic.eMosaicSquare1          : return "Square 1";
      case EMosaic.eMosaicSquare2          : return "Square 2 (offset)";
      case EMosaic.eMosaicParquet1         : return "Rectangle 1 (Parquet 'Herring-bone')";
      case EMosaic.eMosaicParquet2         : return "Rectangle 2";
      case EMosaic.eMosaicTrapezoid1       : return "Trapezoid 1";
      case EMosaic.eMosaicTrapezoid2       : return "Trapezoid 2";
      case EMosaic.eMosaicTrapezoid3       : return "Trapezoid 3";
      case EMosaic.eMosaicRhombus1         : return "Rhombus";
      case EMosaic.eMosaicQuadrangle1      : return "Quadrilateral 120°-90°-60°-90°";
      case EMosaic.eMosaicPenrousePeriodic1: return "Penrose periodic tilings";// (rombus 72°-108° & 36°- 144°)";
      case EMosaic.eMosaicPentagonT24      : return "Pentagon (type 2 and 4)";
      case EMosaic.eMosaicPentagonT5       : return "Pentagon (type 5)";
      case EMosaic.eMosaicPentagonT10      : return "Pentagon (type 10)";
      case EMosaic.eMosaicHexagon1         : return "Hexagon";
      case EMosaic.eMosaicTrSq1            : return "Square-Triangle 1";
      case EMosaic.eMosaicTrSq2            : return "Square-Triangle 2";
      case EMosaic.eMosaicSqTrHex          : return "Square-Triangle-Hexagon";
      }
      return "EMosaicEx.GetDescription: Mosaic '" + self + "' not implemented";
   }

   /// <summary>
   /// Перечень описаний мозаик
   /// </summary>
   /// <returns></returns>
   public static IEnumerable<string> GetDescriptionValues() {
      return GetValues().Select(x => x.GetDescription(false));
   }

   public static EMosaic? FromDescription(string description) {
      foreach (var type in GetValues().Where(type => type.GetDescription(false).Equals(description)))
         return type;
      return null;
   }

   /// <summary>
   /// 'Быстрый' код - уникальный номер мозаики для быстрого ввода.
   /// <ul> Идея:
   /// <li> треугольники начинаются с цифры 3
   /// <li> четырёхугольники - с цифры 4
   /// <li> пятиугольники - с цифры 5
   /// <li> шестиугольники - с цифры 6
   /// <li> прочие - с цифры 7
   /// </summary>
   public static int GetFastCode(this EMosaic self) {
      switch (self) {
      case EMosaic.eMosaicTriangle1        : return 30;
      case EMosaic.eMosaicTriangle2        : return 31;
      case EMosaic.eMosaicTriangle3        : return 32;
      case EMosaic.eMosaicTriangle4        : return 33;
      case EMosaic.eMosaicSquare1          : return 40;
      case EMosaic.eMosaicSquare2          : return 41;
      case EMosaic.eMosaicParquet1         : return 42;
      case EMosaic.eMosaicParquet2         : return 43;
      case EMosaic.eMosaicTrapezoid1       : return 44;
      case EMosaic.eMosaicTrapezoid2       : return 45;
      case EMosaic.eMosaicTrapezoid3       : return 46;
      case EMosaic.eMosaicRhombus1         : return 47;
      case EMosaic.eMosaicQuadrangle1      : return 48;
      case EMosaic.eMosaicPenrousePeriodic1: return 49;
      case EMosaic.eMosaicPentagonT24      : return 50;
      case EMosaic.eMosaicPentagonT5       : return 51;
      case EMosaic.eMosaicPentagonT10      : return 52;
      case EMosaic.eMosaicHexagon1         : return 60;
      case EMosaic.eMosaicTrSq1            : return 70;
      case EMosaic.eMosaicTrSq2            : return 71;
      case EMosaic.eMosaicSqTrHex          : return 72;
      }
      System.Diagnostics.Debug.Assert(false, "EMosaicEx.GetFastCode: Mosaic '" + self + "' not implemented");
      return 0;
   }

   /// <summary>
   /// Перечень 'быстрых' кодов
   /// </summary>
   /// <returns></returns>
   public static IEnumerable<int> GetFastCodeValues() {
      return GetValues().Select(x => x.GetFastCode());
   }

   public static EMosaic? FromFastCode(int fastCode) {
      foreach (var type in GetValues().Where(type => fastCode == type.GetFastCode()))
         return type;
      return null;
   }

   public static String GetMosaicClassName(this EMosaic self) {
      return self.ToString().Substring(7);
   }

   /// <summary> Для рисование иконки: минимальный размер поля, по которому будет визуально ясно, что это за мозаика... </summary>
   public static Matrisize SizeIcoField(this EMosaic self, bool smallSize) {
      var res = new Matrisize();
      switch (self) {
      case EMosaic.eMosaicTriangle1        : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTriangle2        : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTriangle3        : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTriangle4        : res.m = 4; res.n = smallSize ? 4 : 5; break;
      case EMosaic.eMosaicSquare1          : res.m = smallSize ? 2 : 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicSquare2          : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicParquet1         : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicParquet2         : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTrapezoid1       : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTrapezoid2       : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTrapezoid3       : res.m = 4; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicRhombus1         : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicQuadrangle1      : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicPenrousePeriodic1: res.m = 3; res.n = smallSize ? 3 : 4; break;
      case EMosaic.eMosaicPentagonT24      : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicPentagonT5       : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicPentagonT10      : res.m = 3; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicHexagon1         : res.m = 2; res.n = smallSize ? 2 : 3; break;
      case EMosaic.eMosaicTrSq1            : res.m = 4; res.n = smallSize ? 4 : 5; break;
      case EMosaic.eMosaicTrSq2            : res.m = 4; res.n = smallSize ? 4 : 5; break;
      case EMosaic.eMosaicSqTrHex          : res.m = 4; res.n = smallSize ? 4 : 5; break;
      default:
         System.Diagnostics.Debug.Assert(false, "EMosaicEx.SizeIcoField: Mosaic '" + self + "' not implemented");
         break;
      }
      return res;
   }
}
}