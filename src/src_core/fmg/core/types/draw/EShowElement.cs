using System;

namespace fmg.core.types.draw {

    public enum EShowElement {
        eCaption,
        eMenu,
        eToolbar,
        eStatusbar
    }

    public static class EShowElementExt {
        public static string getDescription(this EShowElement self) {
            switch (self) {
            case EShowElement.eCaption: return "Show caption";
            case EShowElement.eMenu: return "Show menu";
            case EShowElement.eToolbar: return "Show toolbar";
            case EShowElement.eStatusbar: return "Show status bar";
            }
            throw new Exception("Invalid " + self.ToString());
        }
    }

}
