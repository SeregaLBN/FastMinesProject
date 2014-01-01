package ua.ksn.fmg.model.mosaics;

public enum EClose {
	_Unknown, _Clear, _Flag;

	public static String toCaption(EClose eClose) {
		switch (eClose) {
		case _Unknown: return "?";
		case _Clear  : return "";
		case _Flag   : return "F";
		}
		return null;
	}
	public final String toCaption() {
		return toCaption(this);
	}
}
