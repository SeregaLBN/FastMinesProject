package fmg.swing.app.serializable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;

/** Mosaic data. For save/restore to/from settings/storage */
public class SerializeMosaicData extends MosaicInitData implements Externalizable {
   private static final long version = 1;

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeLong(version);

      out.writeDouble(getArea());
      out.writeInt(getMosaicType().getIndex());
      out.writeInt(getSizeField().m);
      out.writeInt(getSizeField().n);
      out.writeInt(getMinesCount());
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      if (version != in.readLong())
         throw new RuntimeException("Unknown version!");

      setArea(in.readDouble());
      setMosaicType(EMosaic.fromIndex(in.readInt()));
      setSizeField(new Matrisize(in.readInt(), in.readInt()));
      setMinesCount(in.readInt());
   }

}