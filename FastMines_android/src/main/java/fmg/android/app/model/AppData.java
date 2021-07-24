package fmg.android.app.model;

import fmg.core.types.model.MosaicInitData;
import fmg.android.app.presentation.MenuSettings;

/** Data model of the application to save/restore */
public final class AppData {

    private MosaicInitData mosaicInitData;
    private boolean splitPaneOpen;

    public AppData() {
        setDefaults();
    }

    public void setDefaults() {
        mosaicInitData = new MosaicInitData();
        splitPaneOpen = MenuSettings.DEFAULT_SPLIT_PANE_OPEN;
    }

    public MosaicInitData getMosaicInitData() {
        return mosaicInitData;
    }

    public void setMosaicInitData(MosaicInitData mosaicInitData) {
        this.mosaicInitData = mosaicInitData;
    }

    public boolean isSplitPaneOpen() {
        return splitPaneOpen;
    }

    public void setSplitPaneOpen(boolean splitPaneOpen) {
        this.splitPaneOpen = splitPaneOpen;
    }

}
