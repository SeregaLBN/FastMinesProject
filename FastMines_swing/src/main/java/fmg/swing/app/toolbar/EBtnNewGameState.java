package fmg.swing.app.toolbar;

import fmg.core.img.SmileModel;
import fmg.core.img.SmileModel.EFaceType;

public enum EBtnNewGameState {
    eNormal,
    ePressed,
    eSelected,
    eDisabled,
    eDisabledSelected,
    eRollover,
    eRolloverSelected,

    // addons
    eNormalMosaic,
    eNormalWin,
    eNormalLoss;

    public SmileModel.EFaceType mapToSmileType() {
        switch (this) {
        case eNormal          : return EFaceType.Face_WhiteSmiling;
        case ePressed         : return EFaceType.Face_SavouringDeliciousFood;
        case eSelected        : return null;
        case eDisabled        : return null;
        case eDisabledSelected: return null;
        case eRollover        : return EFaceType.Face_WhiteSmiling;
        case eRolloverSelected: return null;
        case eNormalMosaic    : return EFaceType.Face_Grinning;
        case eNormalWin       : return EFaceType.Face_SmilingWithSunglasses;
        case eNormalLoss      : return EFaceType.Face_Disappointed;
        }
        throw new RuntimeException("Map me...");
    }

}
