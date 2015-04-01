package fmg.data.view.draw;

public enum EShowElement {
	eCaption,
	eMenu,
	eToolbar,
	eStatusbar;

	public String getDescription() {
		switch (this) {
		case eCaption  : return "Show caption";
		case eMenu     : return "Show menu";
		case eToolbar  : return "Show toolbar";
		case eStatusbar: return "Show status bar";
		}
		throw new RuntimeException("Invalid "+this.toString());
	}
}