package fmg.core.types;

public enum EClose {
   _Unknown, _Clear, _Flag;

   public static String toCaption(EClose eClose) {
      switch (eClose) {
      case _Unknown: return "?";
      case _Clear  : return "";
      case _Flag   :
//            return "\u26F3"; // Flag in hole: Флаг в воронке
//            return "\u2690"; // Белый флаг: White flag
//            return "\u2691"; // Черный флаг: Black flag
            return "F";
      }
      return null;
   }
   public final String toCaption() {
      return toCaption(this);
   }

   public EClose nextState(boolean useUnknown) {
      switch (this) {
      case _Clear  : return _Flag;
      case _Flag   : return useUnknown ? _Unknown : _Clear;
      case _Unknown: return _Clear;
      }
      throw new RuntimeException("Unknown current value EClose."+this);
   }
}
