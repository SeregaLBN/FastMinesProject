using System;

namespace fmg.core.types {

   public enum EClose {
      _Unknown,
      _Clear,
      _Flag
   }

   public static class ECloseEx {
      private static readonly EClose[] ECloseValues = (EClose[])Enum.GetValues(typeof(EClose));
      public static EClose[] GetValues() { return ECloseValues; }

      public static uint Ordinal(this EClose self)
      {
         return (uint) self;
      }

      public static string ToCaption(this EClose self) {
         switch (self) {
            case EClose._Unknown:
               return "?";
            case EClose._Clear:
               //return "X";
               return string.Empty;
            case EClose._Flag:
               //return "\u26F3"; // Flag in hole: Флаг в воронке
               return "\u2690"; // Белый флаг: White flag
               //return "\u2691"; // Черный флаг: Black flag
               //return "F";
         }
         return null;
      }
   }
}