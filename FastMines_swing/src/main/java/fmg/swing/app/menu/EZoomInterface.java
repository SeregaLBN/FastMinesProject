package fmg.swing.app.menu;

public enum EZoomInterface {

    eAlwaysMax,
    eMax, eMin,
    eInc, eDec;

    public String getDescription() {
        switch (this) {
        case eAlwaysMax: return "Always maximal size";
        case eMax      : return "Maximal mosaic size";
        case eMin      : return "Minimal mosaic size";
        case eInc      : return "Increase mosaic size";
        case eDec      : return "Decrease mosaic size";
        }
        throw new RuntimeException("Invalid "+this.toString());
    }

}
