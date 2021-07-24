package fmg.swing.app.serializers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fmg.common.geom.Matrisize;
import fmg.core.app.ISerializer;
import fmg.core.types.EMosaic;
import fmg.core.types.model.MosaicInitData;

/** Mosaic data (de)serializer. For save/restore {@link MosaicInitData} */
public class MosaicInitDataSerializer implements ISerializer {

    private static final long VERSION = 1;

    void write(MosaicInitData data, ObjectOutput out) throws IOException {
        out.writeLong(VERSION);
        out.writeInt(data.getMosaicType().getIndex());
        out.writeInt(data.getSizeField().m);
        out.writeInt(data.getSizeField().n);
        out.writeInt(data.getCountMines());
    }

    MosaicInitData read(ObjectInput in) throws IOException {
        long ver = in.readLong();
        if (ver != VERSION)
            throw new IllegalArgumentException(MosaicInitDataSerializer.class.getSimpleName() + ": Unsupported version #" + ver);

        MosaicInitData data = new MosaicInitData();

        data.setMosaicType(EMosaic.fromIndex(in.readInt()));
        int m = in.readInt();
        int n = in.readInt();
        data.setSizeField(new Matrisize(m, n));
        data.setCountMines(in.readInt());

        return data;
    }

}