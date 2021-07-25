package fmg.swing.app.serializers;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

import fmg.common.Logger;
import fmg.common.geom.Point;
import fmg.common.geom.SizeDouble;
import fmg.core.app.AProjSettings;
import fmg.core.app.ISerializer;
import fmg.core.types.model.MosaicInitData;
import fmg.swing.app.menu.EShowElement;
import fmg.swing.app.model.AppData;

/** Application data (de)serializer. For save / restore {@link AppData} */
public class AppDataSerializer implements ISerializer {

    private static final long VERSION = 1;

    private void write(AppData data, ObjectOutput out) throws IOException {
        out.writeLong(VERSION);

        MosaicInitData mid = new MosaicInitData();
        mid.setCountMines(data.getCountMines());
        mid.setMosaicType(data.getMosaicType());
        mid.setSizeField(data.getSizeField());
        new MosaicInitDataSerializer().write(mid, out);

        out.writeBoolean(data.getActiveUserId() != null);
        if (data.getActiveUserId() != null)
            out.writeUTF(data.getActiveUserId().toString());
        out.writeBoolean(data.isDoNotAskStartup());

        out.writeBoolean(data.isSystemTheme());
        for (EShowElement key : EShowElement.values())
            out.writeBoolean(data.getShowElement(key));
        out.writeBoolean(data.isZoomAlwaysMax());
        out.writeBoolean(data.isUseUnknown());
        out.writeBoolean(data.isUsePause());
        out.writeInt(data.getLocation().x);
        out.writeInt(data.getLocation().y);
        out.writeDouble(data.getSizeMosaic().width);
        out.writeDouble(data.getSizeMosaic().height);
    }

    private void read(AppData data, ObjectInput in) throws IOException {
        long ver = in.readLong();
        if (VERSION != ver)
            throw new IllegalArgumentException(AppDataSerializer.class.getSimpleName() + ": Unsupported version " + ver);

        MosaicInitData mid = new MosaicInitDataSerializer().read(in);
        data.setCountMines(mid.getCountMines());
        data.setMosaicType(mid.getMosaicType());
        data.setSizeField(mid.getSizeField());

        if (in.readBoolean())
            data.setActiveUserId(UUID.fromString(in.readUTF()));
        data.setDoNotAskStartup(in.readBoolean());

        data.setSystemTheme(in.readBoolean());
        for (EShowElement key : EShowElement.values())
            data.setShowElement(key, in.readBoolean());
        data.setZoomAlwaysMax(in.readBoolean());
        data.setUseUnknown(in.readBoolean());
        data.setUsePause(in.readBoolean());
        int x = in.readInt();
        int y = in.readInt();
        data.setLocation(new Point(x, y));
        double w = in.readDouble();
        double h = in.readDouble();
        data.setSizeMosaic(new SizeDouble(w, h));

//        Logger.info("Read: location={0}, sizeMosaic=[{1}, {2}]", location, sizeMosaicWidth, sizeMosaicHeight);
    }

    /** Load ini data from file
     * @return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults */
    public boolean load(AppData data) {
        Objects.requireNonNull(data);

        File file = getIniFile();
        if (!file.exists()) {
            data.setDefaults();
            return false;
        }

        try (InputStream is = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(is))
        {
            read(data, in);
            return true;
        } catch (Exception ex) {
            Logger.error("Can`t load " + AppData.class.getSimpleName(), ex);
            data.setDefaults();
            return false;
        }
    }

    public void save(AppData data) {
        try (OutputStream os = new FileOutputStream(getIniFile());
             ObjectOutputStream out = new ObjectOutputStream(os))
        {
            write(data, out);
        } catch (Exception ex) {
            Logger.error("Can`t save " + AppData.class.getSimpleName(), ex);
        }
    }

    private static File getIniFile() {
        return new File(AProjSettings.getSettingsFileName());
    }

}
