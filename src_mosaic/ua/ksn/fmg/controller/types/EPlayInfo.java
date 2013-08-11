package ua.ksn.fmg.controller.types;

/** 1. Kто играл: юзер и/или робот?
 * <br/>
 *  2. Play from file or created game?
 */
public enum EPlayInfo {
	/** ещё неизвестно кто играет. До старта игры */
	ePlayerUnknown,
	/** играл/играет человек */
	ePlayerUser,
	/** играл/играет ассистент */
	ePlayerAssistant,
	/** играли оба - и человек, и ассистент (т.е. может играть робот, а закончить пользователь, или же юзер воспользовался подсказкой ассистента) */
	ePlayBoth,
	/** Не учитывать игру в статистике и в чемпионских результатах - данная игра была или загружена из файла или была создана */
	ePlayIgnor;

	public static EPlayInfo setPlayInfo(EPlayInfo oldVal, EPlayInfo newVal) {
		switch (newVal) {
		case ePlayerUnknown: return ePlayerUnknown;
		case ePlayerUser:
			switch (oldVal) {
			case ePlayerUnknown: return ePlayerUser;
			case ePlayerUser: return ePlayerUser;
			case ePlayerAssistant: return ePlayBoth;
			case ePlayBoth: return ePlayBoth;
			case ePlayIgnor: return ePlayIgnor;
			}
		case ePlayerAssistant:
			switch (oldVal) {
			case ePlayerUnknown: return ePlayerAssistant;
			case ePlayerUser: return ePlayBoth;
			case ePlayerAssistant: return ePlayerAssistant;
			case ePlayBoth: return ePlayBoth;
			case ePlayIgnor: return ePlayIgnor;
			}
		case ePlayBoth:
			switch (oldVal) {
			case ePlayerUnknown:
			case ePlayerUser:
			case ePlayerAssistant:
			case ePlayBoth: return ePlayBoth;
			case ePlayIgnor: return ePlayIgnor;
			}
		case ePlayIgnor: return ePlayIgnor;
		}
		throw new RuntimeException(); // never
	}
}
