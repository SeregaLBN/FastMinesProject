package fmg.swing.app.serializable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fmg.common.geom.Matrisize;
import fmg.core.app.ISerializator;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;

/** Mosaic data (de)serializator. For save/restore {@link MosaicInitData} */
public class MosaicDataSerialize implements ISerializator {

    private static final long VERSION = 1;

    public void write(MosaicInitData data, ObjectOutput out) throws IOException {
        out.writeLong(VERSION);
        out.writeInt(data.getMosaicType().getIndex());
        out.writeInt(data.getSizeField().m);
        out.writeInt(data.getSizeField().n);
        out.writeInt(data.getCountMines());
    }

    public MosaicInitData read(ObjectInput in) throws IOException {
        long ver = in.readLong();
        if (ver != VERSION)
            throw new IllegalArgumentException("Unsupported version #" + ver);

        MosaicInitData data = new MosaicInitData();

        data.setMosaicType(EMosaic.fromIndex(in.readInt()));
        int m = in.readInt();
        int n = in.readInt();
        data.setSizeField(new Matrisize(m, n));
        data.setCountMines(in.readInt());

        return data;
    }

}