package fmg.swing.app.toolbar;

import fmg.core.img.SmileModel;
import fmg.core.img.SmileModel.EFaceType;

public enum EBtnPauseState {
    eNormal,
    ePressed,
    eSelected,
    eDisabled,
    eDisabledSelected,
    eRollover,
    eRolloverSelected,

    /** типа ход ассистента - задел на будущее */
    eAssistant;

    public SmileModel.EFaceType mapToSmileType() {
        switch (this) {
        case eNormal          : return EFaceType.Face_EyesOpen;
        case ePressed         : return EFaceType.Face_WinkingEyeLeft;
        case eSelected        : return EFaceType.Face_EyesClosed;
        case eDisabled        : return EFaceType.Eyes_OpenDisabled;
        case eDisabledSelected: return EFaceType.Eyes_ClosedDisabled;
        case eRollover        : return EFaceType.Face_EyesOpen;
        case eRolloverSelected: return EFaceType.Face_WinkingEyeRight;
        case eAssistant       : return EFaceType.Face_Assistant;
        }
        throw new RuntimeException("Map me...");
    }

}
