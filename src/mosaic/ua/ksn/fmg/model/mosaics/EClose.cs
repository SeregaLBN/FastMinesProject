namespace ua.ksn.fmg.model.mosaics {

   public enum EClose {
      _Unknown, _Clear, _Flag
   }

   public static class ECloseEx {
      public static uint Ordinal(this EClose self) { return (uint)self; }

      public static string ToCaption(this EClose self) {
         switch (self) {
         case EClose._Unknown: return "?";
#if DEBUG
         case EClose._Clear  : return "X";
#else
         case EClose._Clear  : string.Empty;
#endif
         case EClose._Flag   : return "F";
         }
         return null;
      }
   }
}