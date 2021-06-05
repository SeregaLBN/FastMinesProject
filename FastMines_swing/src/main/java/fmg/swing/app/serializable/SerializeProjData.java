package fmg.swing.app.serializable;

import java.io.*;
import java.util.UUID;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Point;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;
import fmg.core.types.draw.EShowElement;

/** Данные проекта, записываемые/считываемые в/из файл(а) */
public class SerializeProjData implements Externalizable {

    private static final long VERSION = 3;

    private SerializeMosaicData mosaicData;

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

    public SerializeProjData() { setDefaults(); }

    private void setDefaults() {
        mosaicData = new SerializeMosaicData();

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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(VERSION);

        mosaicData.writeExternal(out);

        out.writeBoolean(activeUserId != null);
        if (activeUserId != null)
            out.writeUTF(activeUserId.toString());
        out.writeBoolean(doNotAskStartup);

        out.writeBoolean(systemTheme);
        for (boolean eShowElement : eShowElements)
            out.writeBoolean(eShowElement);
        out.writeBoolean(zoomAlwaysMax);
        out.writeBoolean(useUnknown);
        out.writeBoolean(usePause);
        out.writeInt(location.x);
        out.writeInt(location.y);
        out.writeDouble(sizeMosaicWidth);
        out.writeDouble(sizeMosaicHeight);

//        Logger.info("Write: location={0}, sizeMosaic=[{1}, {2}]", location, sizeMosaicWidth, sizeMosaicHeight);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long ver = in.readLong();
        if (VERSION != ver)
            throw new IllegalArgumentException("Unknown version!");

        mosaicData.readExternal(in);

        if (in.readBoolean())
            activeUserId = UUID.fromString(in.readUTF());
        doNotAskStartup = in.readBoolean();

        systemTheme = in.readBoolean();
        for (int i=0; i<eShowElements.length; i++)
            eShowElements[i] = in.readBoolean();
        zoomAlwaysMax = in.readBoolean();
        useUnknown = in.readBoolean();
        usePause = in.readBoolean();
        location.x = in.readInt();
        location.y = in.readInt();
        sizeMosaicWidth  = in.readDouble();
        sizeMosaicHeight = in.readDouble();

//        Logger.info("Read: location={0}, sizeMosaic=[{1}, {2}]", location, sizeMosaicWidth, sizeMosaicHeight);
    }

    /**
     * Load ini data from file
     * @return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults
     */
    public boolean load() {
        File file = getIniFile();
        if (!file.exists()) {
            setDefaults();
            return false;
        }

        try (InputStream is = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(is))
        {
            this.readExternal(in);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            setDefaults();
            return false;
        }
    }

    public void save() throws IOException {
        try (OutputStream os = new FileOutputStream(getIniFile());
             ObjectOutputStream out = new ObjectOutputStream(os))
        {
            this.writeExternal(out);
        }
    }

    public static File getIniFile() {
        return new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Mines.dat");
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
