using System;

namespace Fmg.Core.Types.Draw {

    public enum EZoomInterface {
        eAlwaysMax,
        eMax, eMin,
        eInc, eDec
    }

    public static class EZoomInterfaceExt {
        public static string getDescription(this EZoomInterface self) {
            switch (self) {
            case EZoomInterface.eAlwaysMax: return "Always maximal size";
            case EZoomInterface.eMax: return "Maximal mosaic size";
            case EZoomInterface.eMin: return "Minimal mosaic size";
            case EZoomInterface.eInc: return "Increase mosaic size";
            case EZoomInterface.eDec: return "Decrease mosaic size";
            }
            throw new Exception("Invalid " + self.ToString());
        }
    }

}
