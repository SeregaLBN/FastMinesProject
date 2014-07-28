package ua.ksn.fmg.model.mosaics;

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
//    		return "\u2699"; // Шестерня: Gear
    	}
    }
    public final String toCaption() { return toCaption(this); }
}
