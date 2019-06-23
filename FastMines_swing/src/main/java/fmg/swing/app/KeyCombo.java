package fmg.swing.app;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.draw.EShowElement;
import fmg.core.types.draw.EZoomInterface;

/** App key combinations */
public final class KeyCombo {
    private KeyCombo() { }

    public static final KeyStroke getKeyStroke_Minimized      () { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false); }
    public static final KeyStroke getKeyStroke_CenterScreenPos() { return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, InputEvent.CTRL_DOWN_MASK, false); }
    public static final KeyStroke getKeyStroke_CenterScreenPos2() { return KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK, false); }
    public static final KeyStroke getKeyStroke_NewGame        () { return KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, true); }
    public static final KeyStroke getKeyStroke_SkillLevel(ESkillLevel key) { return KeyStroke.getKeyStroke(KeyEvent.VK_1+key.ordinal(), 0, true); }
    public static final KeyStroke getKeyStroke_PlayerManage   () { return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0, false); }
    public static final KeyStroke getKeyStroke_Exit           () { return KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK, false); }
    public static final KeyStroke getKeyStroke_Zoom(EZoomInterface key) {
        switch (key) {
        case eMax: return KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0, true);
        case eMin: return KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE  , 0, true);
        case eInc: return KeyStroke.getKeyStroke(KeyEvent.VK_ADD     , 0, true);
        case eDec: return KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0, true);
        default: return null; // throw new RuntimeException();
        }
    }
    public static final KeyStroke getKeyStroke_ZoomMaxAlternative() { return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK, true); }
    public static final KeyStroke getKeyStroke_ZoomMinAlternative() { return KeyStroke.getKeyStroke(KeyEvent.VK_MINUS , InputEvent.CTRL_DOWN_MASK, true); }
    public static final KeyStroke getKeyStroke_ZoomIncAlternative() { return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0, true); }
    public static final KeyStroke getKeyStroke_ZoomDecAlternative() { return KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0, true); }
    public static final KeyStroke getKeyStroke_ThemeDefault   () { return KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false); }
    public static final KeyStroke getKeyStroke_ThemeSystem    () { return KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK, false); }
    public static final KeyStroke getKeyStroke_UseUnknown     () { return null; }
    public static final KeyStroke getKeyStroke_UsePause       () { return null; }
    public static final KeyStroke getKeyStroke_ShowElements   (EShowElement key) {
//            switch (key) {
//            case eCaption  : return KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false);
//            case eMenu     : return KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0, false);
//            case eToolbar  : return KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false);
//            case eStatusbar: return KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false);
//            default: return null; // throw new RuntimeException();
//            }
        return KeyStroke.getKeyStroke(KeyEvent.VK_F9 + key.ordinal(), 0, false);
    }
    public static final KeyStroke getKeyStroke_Pause1         () { return KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0, false); }
    public static final KeyStroke getKeyStroke_Pause2         () { return KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false); }
    public static final KeyStroke getKeyStroke_About          () { return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false); }
    public static final KeyStroke getKeyStroke_Champions      () { return KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false); }
    public static final KeyStroke getKeyStroke_Statistics     () { return KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false); }

    public static final KeyStroke getKeyStroke_Mosaic(EMosaic key) { return null; }//KeyStroke.getKeyStroke(KeyEvent.VK_, 0, false); }

    public static final KeyStroke getKeyStroke_MosaicFieldXInc() { return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, true); }
    public static final KeyStroke getKeyStroke_MosaicFieldXDec() { return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT , InputEvent.ALT_DOWN_MASK, true); }
    public static final KeyStroke getKeyStroke_MosaicFieldYInc() { return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN , InputEvent.ALT_DOWN_MASK, true); }
    public static final KeyStroke getKeyStroke_MosaicFieldYDec() { return KeyStroke.getKeyStroke(KeyEvent.VK_UP   , InputEvent.ALT_DOWN_MASK, true); }

    public static final KeyStroke getKeyStroke_SelectMosaic(EMosaicGroup key) { return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3+key.ordinal(), 0, false); }

    // ------ Mnemonics
    private static final int VK_NULL = 0;//'\0';

    public static final int getMnemonic_MenuGame       () { return KeyEvent.VK_G; }
    public static final int getMnemonic_MenuMosaic     () { return KeyEvent.VK_M; }
    public static final int getMnemonic_MenuMosaicGroup(EMosaicGroup key) { return KeyEvent.VK_NUMPAD3 + key.ordinal(); }
    public static final int getMnemonic_MenuOptions    () { return KeyEvent.VK_O; }
    public static final int getMnemonic_MenuZoom       () { return KeyEvent.VK_Z; }
    public static final int getMnemonic_MenuHelp       () { return KeyEvent.VK_H; }

    public static final int getMnemonic_NewGame        () { return KeyEvent.VK_N; }
    public static final int getMnemonic_SkillLevel(ESkillLevel key) {
        switch (key) {
        case eBeginner: return KeyEvent.VK_B;
        case eAmateur : return KeyEvent.VK_A;
        case eProfi   : return KeyEvent.VK_P;
        case eCrazy   : return KeyEvent.VK_R;
        case eCustom  : return KeyEvent.VK_C;
        default       : return VK_NULL;
        }
    }
    public static final int getMnemonic_PlayerManage   () { return KeyEvent.VK_P; }
    public static final int getMnemonic_Exit           () { return KeyEvent.VK_E; }
    public static final int getMnemonic_Zoom(EZoomInterface key) { return VK_NULL; }
    public static final int getMnemonic_Theme          () { return KeyEvent.VK_T; }
    public static final int getMnemonic_ThemeDefault   () { return KeyEvent.VK_D; }
    public static final int getMnemonic_ThemeSystem    () { return KeyEvent.VK_Y; }
    public static final int getMnemonic_UseUnknown     () { return KeyEvent.VK_U; }
    public static final int getMnemonic_UsePause       () { return KeyEvent.VK_P; }
    public static final int getMnemonic_ShowElements   (EShowElement key) { return KeyEvent.VK_S; }
    public static final int getMnemonic_About          () { return VK_NULL; }
    public static final int getMnemonic_Champions      () { return VK_NULL; }
    public static final int getMnemonic_Statistics     () { return VK_NULL; }

    public static final int getMnemonic_Mosaic(EMosaic key) { return VK_NULL; }

}
