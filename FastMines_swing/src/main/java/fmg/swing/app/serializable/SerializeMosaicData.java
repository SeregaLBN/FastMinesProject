package fmg.swing.app.serializable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;

/** Mosaic data. For save/restore to/from settings/storage */
public class SerializeMosaicData extends MosaicInitData implements Externalizable {

    private static final long VERSION = 2;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(VERSION);
        out.writeDouble(getSize().width);
        out.writeDouble(getSize().height);
        out.writeInt(getMosaicType().getIndex());
        out.writeInt(getSizeField().m);
        out.writeInt(getSizeField().n);
        out.writeInt(getMinesCount());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long ver = in.readLong();
        switch ((int)ver) {
        case 1:
            in.readLong(); // old area value
            setMosaicType(EMosaic.fromIndex(in.readInt()));
            setSizeField(new Matrisize(in.readInt(), in.readInt()));
            setMinesCount(in.readInt());
            break;
        case (int)VERSION:
            setSize(new SizeDouble(in.readDouble(), in.readDouble()));
            setMosaicType(EMosaic.fromIndex(in.readInt()));
            setSizeField(new Matrisize(in.readInt(), in.readInt()));
            setMinesCount(in.readInt());
            break;
        default:
            throw new IllegalArgumentException("Unknown version #" + ver);
        }
    }

}