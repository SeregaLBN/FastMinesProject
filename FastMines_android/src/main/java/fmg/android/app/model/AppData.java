package fmg.android.app.model;

import fmg.core.app.model.MosaicBackupData;
import fmg.core.app.model.MosaicInitData;
import fmg.android.app.presentation.MenuSettings;
import fmg.core.types.EOpen;

/** Data model of the application to save/restore */
public final class AppData {

    private MosaicInitData mosaicInitData;
    private MosaicActivityBackupData mosaicActivityBackupData;
    private boolean splitPaneOpen;

    public AppData() {
        setDefaults();
    }

    public void setDefaults() {
        mosaicInitData = new MosaicInitData();
        mosaicActivityBackupData = null;
        splitPaneOpen = MenuSettings.DEFAULT_SPLIT_PANE_OPEN;
    }

    public MosaicInitData getMosaicInitData() {
        if (mosaicActivityBackupData != null) {
            MosaicInitData res = new MosaicInitData();
            MosaicBackupData mosaicBackupData = mosaicActivityBackupData.mosaicBackupData;
            res.setMosaicType(mosaicBackupData.mosaicType);
            res.setSizeField(mosaicBackupData.sizeField);
            res.setCountMines((int)mosaicBackupData.cellStates
                                                   .stream()
                                                   .filter(c -> c.getOpen() == EOpen._Mine)
                                                   .count());
            return res;
        }
        return mosaicInitData;
    }

    public void setMosaicInitData(MosaicInitData mosaicInitData) {
        this.mosaicInitData = mosaicInitData;
        if (mosaicInitData != null)
            this.mosaicActivityBackupData = null;
    }

    public MosaicActivityBackupData getMosaicActivityBackupData() {
        return mosaicActivityBackupData;
    }

    public void setMosaicActivityBackupData(MosaicActivityBackupData mosaicActivityBackupData) {
        this.mosaicActivityBackupData = mosaicActivityBackupData;
        if (mosaicActivityBackupData != null)
            this.mosaicInitData = null;
    }

    public boolean isSplitPaneOpen() {
        return splitPaneOpen;
    }

    public void setSplitPaneOpen(boolean splitPaneOpen) {
        this.splitPaneOpen = splitPaneOpen;
    }

}
