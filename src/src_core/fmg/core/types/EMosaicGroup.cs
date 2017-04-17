using System;
using System.Collections.Generic;

namespace fmg.core.types {

   public enum EMosaicGroup {
      eTriangles,
      eQuadrangles,
      ePentagons,
      eHexagons,
      eOthers
   }

   public static class EMosaicGroupEx {

      private static readonly EMosaicGroup[] EMosaicGroupValues = (EMosaicGroup[])Enum.GetValues(typeof(EMosaicGroup));
      public static EMosaicGroup[] GetValues() { return EMosaicGroupValues; }

      public static int VertexCount(this EMosaicGroup self, int defaultValue = 7) {
         switch (self) {
         case EMosaicGroup.eTriangles  : return 3;
         case EMosaicGroup.eQuadrangles: return 4;
         case EMosaicGroup.ePentagons  : return 5;
         case EMosaicGroup.eHexagons   : return 6;
         case EMosaicGroup.eOthers     : return defaultValue; // 3 + ThreadLocalRandom.Current.Next() & 3;
         }
         throw new ArgumentException("Invalid paramenter value " + self);
      }

      public static EMosaicGroup FromIndex(int index) {
         return (EMosaicGroup) index;
         //foreach (EMosaicGroup item in EMosaicGroupValues)
         //    if ((int)item == index)
         //        return item;
         //throw new ArgumentException("Invalid paramenter value " + index);
      }

      public static int GetIndex(this EMosaicGroup self) { return (int)self; }

      public static int Ordinal(this EMosaicGroup self) {
         var values = GetValues();
         for (var i = 0; i < values.Length; i++)
            if (values[i] == self)
               return i;
         throw new ArgumentException("Index not found");
      }

      public static IEnumerable<EMosaic> GetBind(this EMosaicGroup self) {
         switch (self) {
         case EMosaicGroup.eTriangles:
            yield return EMosaic.eMosaicTriangle1;
            yield return EMosaic.eMosaicTriangle2;
            yield return EMosaic.eMosaicTriangle3;
            yield return EMosaic.eMosaicTriangle4;
            break;
         case EMosaicGroup.eQuadrangles:
            yield return EMosaic.eMosaicSquare1;
            yield return EMosaic.eMosaicSquare2;
            yield return EMosaic.eMosaicParquet1;
            yield return EMosaic.eMosaicParquet2;
            yield return EMosaic.eMosaicTrapezoid1;
            yield return EMosaic.eMosaicTrapezoid2;
            yield return EMosaic.eMosaicTrapezoid3;
            yield return EMosaic.eMosaicRhombus1;
            yield return EMosaic.eMosaicQuadrangle1;
            yield return EMosaic.eMosaicPenrousePeriodic1;
            break;
         case EMosaicGroup.ePentagons:
            yield return EMosaic.eMosaicPentagonT24;
            yield return EMosaic.eMosaicPentagonT5;
            yield return EMosaic.eMosaicPentagonT10;
            break;
         case EMosaicGroup.eHexagons:
            yield return EMosaic.eMosaicHexagon1;
            break;
         case EMosaicGroup.eOthers:
            yield return EMosaic.eMosaicTrSq1;
            yield return EMosaic.eMosaicTrSq2;
            yield return EMosaic.eMosaicSqTrHex;
            break;
         }
      }

      /// <summary> Описание для пользователя </summary>
      public static string GetDescription(this EMosaicGroup self) {
         return self.ToString().Substring(1);
      }

      public static char UnicodeChar(this EMosaicGroup self, bool dark) {
         switch (self) {
         case EMosaicGroup.eTriangles  : return dark ? '\u25B2' : '\u25B3'; // http://unicode-table.com/en/search/?q=triangle
         case EMosaicGroup.eQuadrangles: return dark ? '\u25AE' : '\u25AF'; // http://unicode-table.com/en/search/?q=rectangle
         case EMosaicGroup.ePentagons  : return dark ? '\u2B1F' : '\u2B20'; // http://unicode-table.com/en/search/?q=pentagon
         case EMosaicGroup.eHexagons   : return dark ? '\u2B22' : '\u2B21'; // http://unicode-table.com/en/search/?q=hexagon
         case EMosaicGroup.eOthers     : return dark ? '\u2605' : '\u2606'; // http://unicode-table.com/en/blocks/miscellaneous-symbols/
                                       //return dark ? '\u25E9' : '\u2B15'; // http://unicode-table.com/en/search/?q=Square+with+Left+Diagonal+Half+Black
         }
         throw new ArgumentException("Invalid paramenter value " + self);
      }

   }

}
