package fmg.android.app.model;

import fmg.common.geom.SizeDouble;
import fmg.core.app.model.MosaicBackupData;

/** Data model of the MosaicActivity to save/restore */
public final class MosaicActivityBackupData {

    public MosaicBackupData mosaicBackupData;
    public long playTime;
    public SizeDouble mosaicOffset;

}
