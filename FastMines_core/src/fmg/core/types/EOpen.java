package fmg.core.types;

import fmg.common.Color;

public enum EOpen {

    _Nil,
    _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21,
    _Mine;

    public static String toCaption(EOpen eOpen) {
        switch (eOpen) {
        case _Nil: return "";
        default: return String.valueOf(eOpen.ordinal());
        case _Mine:
            return "M";
//          return "\u2699"; // Шестерня: Gear
        }
    }
    public final String toCaption() { return toCaption(this); }

    public Color asColor() {
        switch (this) {
        case _Nil : return Color.Black ();
        case _1   : return Color.Navy  ();
        case _2   : return Color.Green ();
        case _3   : return Color.Red   ();
        case _4   : return Color.Maroon();
        case _5   : return Color.Blue  ();
        case _6   : return Color.Black ();
        case _7   : return Color.Olive ();
        case _8   : return Color.Aqua  ();
        case _9   : return Color.Navy  ();
        case _10  : return Color.Green ();
        case _11  : return Color.Red   ();
        case _12  : return Color.Maroon();
        case _13  : return Color.Navy  ();
        case _14  : return Color.Green ();
        case _15  : return Color.Red   ();
        case _16  : return Color.Maroon();
        case _17  : return Color.Blue  ();
        case _18  : return Color.Black ();
        case _19  : return Color.Olive ();
        case _20  : return Color.Aqua  ();
        case _21  : return Color.Navy  ();
        case _Mine: return Color.Black ();
        default: throw new RuntimeException("Add color for EOpen value " + this);
        }
    }

}
