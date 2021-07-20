package fmg.swing.app.serializable;

import java.util.UUID;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Point;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;
import fmg.core.types.draw.EShowElement;

/** Data model of the main window to save/restore */
public class MainWindowData {

    private MosaicInitData mosaicData;

    private UUID activeUserId;
    private boolean doNotAskStartup; // manage dialog

    private boolean[] eShowElements;
    private boolean zoomAlwaysMax;
    private boolean useUnknown;
    private boolean usePause;
    private Point location;
    private double sizeMosaicWidth;
    private double sizeMosaicHeight;
    private boolean systemTheme;

    public MainWindowData() {
        setDefaults();
    }

    public void setDefaults() {
        mosaicData = new MosaicInitData();

        SizeDouble size = MosaicHelper.getSize(mosaicData.getMosaicType(), MosaicInitData.AREA_MINIMUM, mosaicData.getSizeField());
        sizeMosaicWidth  = size.width;
        sizeMosaicHeight = size.height;

        activeUserId = null;
        doNotAskStartup = true;

        if (eShowElements == null)
            eShowElements = new boolean[EShowElement.values().length];
        for (EShowElement se: EShowElement.values())
            eShowElements[se.ordinal()] = true;

        zoomAlwaysMax = false;
        useUnknown = true;
        usePause = true;

        if (location == null)
            location = new Point(0, 0);
        else
            location.x = location.y = 0;

        systemTheme = true;
    }


    public Matrisize getSizeField() { return mosaicData.getSizeField(); }
    public void setSizeField(Matrisize sizeField) { mosaicData.setSizeField(sizeField); }

    public EMosaic getMosaicType() { return mosaicData.getMosaicType(); }
    public void setMosaicType(EMosaic mosaicType) { mosaicData.setMosaicType(mosaicType); }

    public int getCountMines() { return mosaicData.getCountMines(); }
    public void setCountMines(int countMines) { mosaicData.setCountMines(countMines); }

    public SizeDouble getSizeMosaic() { return new SizeDouble(sizeMosaicWidth, sizeMosaicHeight); }
    public void setSizeMosaic(SizeDouble size) {
        this.sizeMosaicWidth  = size.width;
        this.sizeMosaicHeight = size.height;
    }

    public boolean getShowElement(EShowElement key) { return eShowElements[key.ordinal()]; }
    public void setShowElement(EShowElement key, boolean val) { this.eShowElements[key.ordinal()] = val; }

    public boolean isZoomAlwaysMax() { return zoomAlwaysMax; }
    public void setZoomAlwaysMax(boolean zoomAlwaysMax) { this.zoomAlwaysMax = zoomAlwaysMax; }

    public boolean isUseUnknown() { return useUnknown; }
    public void setUseUnknown(boolean useUnknown) { this.useUnknown = useUnknown; }

    public boolean isUsePause() { return usePause; }
    public void setUsePause(boolean usePause) { this.usePause = usePause; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public boolean isSystemTheme() { return systemTheme; }
    public void setSystemTheme(boolean systemTheme) { this.systemTheme = systemTheme; }

    public UUID getActiveUserId() { return activeUserId; }
    public void setActiveUserId(UUID activeUserId) { this.activeUserId = activeUserId; }

    public boolean isDoNotAskStartup() { return doNotAskStartup; }
    public void setDoNotAskStartup(boolean doNotAskStartup) { this.doNotAskStartup = doNotAskStartup; }

}
