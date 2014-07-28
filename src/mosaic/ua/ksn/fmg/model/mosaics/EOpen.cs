namespace ua.ksn.fmg.model.mosaics {
   public enum EOpen {
      _Nil,
      _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21,
      _Mine
   }

   public static class EOpenEx {
      public static uint Ordinal(this EOpen self) { return (uint)self; }

      public static string toCaption(this EOpen self) {
         switch (self) {
         case EOpen._Nil:
            return string.Empty;
         default:
            return Ordinal(self).ToString(); // System.Enum.GetName(typeof(EOpen), self).TrimStart('_');
         case EOpen._Mine:
            return "M";
            //return "\u2699"; // Шестерня: Gear
         }
      }
   }
}