package fmg.core.app.serializers;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import fmg.common.Logger;
import fmg.core.app.model.Champions;
import fmg.core.app.model.Champions.Record;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Champions base (de)serializer */
public abstract class ChampionsSerializer implements ISerializer {

    protected static final long VERSION = 1;

    private static class RecordSerializer implements ISerializer {

        void write(Champions.Record rec, ObjectOutput to) throws IOException {
            to.writeUTF(rec.userId.toString());
            to.writeUTF(rec.userName);
            to.writeLong(rec.playTime);
            to.writeInt(rec.clicks);
            to.writeLong(rec.date.getTime());
        }

        Champions.Record read(ObjectInput from) throws IOException {
            Champions.Record res = new Record();
            res.userId = UUID.fromString(from.readUTF());
            res.userName = from.readUTF();
            res.playTime = from.readLong();
            res.clicks   = from.readInt();
            res.date = new Date(from.readLong());
            return res;
        }
    }

    private void write(Champions champions, ObjectOutput to) throws IOException {
        to.writeLong(VERSION);
        List<Record>[][] all = champions.getRecords();
        RecordSerializer rs = new RecordSerializer();
        for (EMosaic mosaic : EMosaic.values())
            for (ESkillLevel eSkill : ESkillLevel.values())
                if (eSkill != ESkillLevel.eCustom) {
                    List<Champions.Record> list = all[mosaic.ordinal()][eSkill.ordinal()];
                    to.writeInt(list.size());
                    for (Record record : list)
                        rs.write(record, to);
                }
    }

    private Champions read(ObjectInput from) throws IOException {
        long version = from.readLong();
        if (version != VERSION)
            throw new RuntimeException("Unsupported " + Champions.class.getSimpleName() + " version " + version);

        Champions res = new Champions();
        List<Record>[][] all = res.getRecords();
        RecordSerializer rs = new RecordSerializer();
        for (EMosaic mosaic : EMosaic.values())
            for (ESkillLevel eSkill : ESkillLevel.values())
                if (eSkill != ESkillLevel.eCustom) {
                    List<Champions.Record> list = all[mosaic.ordinal()][eSkill.ordinal()];
                    int size = from.readInt();
                    for (int i=0; i<size; i++) {
                        Record record = rs.read(from);
                        list.add(record);
                    }
                }
        return res;
    }

    /** serialize to bytes */
    private byte[] asBytes(Champions champions) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            write(champions, oos);
            oos.flush();

            return baos.toByteArray();
        }
    }

    /** deserilize from bytes */
    protected Champions fromBytes(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais))
        {
            return read(ois);
        }
    }

    /** write to file */
    private void write(byte[] data, File file) throws IOException {
        try (OutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oot = new ObjectOutputStream(fos))
        {
            oot.writeLong(VERSION); // save version and decrypt key
            int len = data.length;
            oot.writeInt(len);
            oot.write(data);
        }
    }

    /** read from file */
    private byte[] read(File file) throws IOException {
        try (InputStream fis = new FileInputStream(file);
             ObjectInputStream oin = new ObjectInputStream(fis))
        {
            long version = oin.readLong();
            if (version != VERSION)
                throw new IOException("Invalid file data. Unsupported " + Champions.class.getSimpleName() + " version " + version);

            byte[] data = new byte[oin.readInt()];
            int read = 0;
            do {
                int curr = oin.read(data, read, data.length-read);
                if (curr < 0)
                    break;
                read += curr;
            } while(read < data.length);

            if (read != data.length)
                throw new IOException("Invalid data length. Required " + data.length + " bytes; read " + read + " bytes.");

            return data;
        }
    }

    protected byte[] writeTransform(byte[] data) throws IOException {
        // defaut none
        return data;
    }

    protected byte[] readTransform(byte[] data) throws IOException {
        // defaut none
        return data;
    }

    public void save(Champions champions) {
        try {
            // 1. serialize
            byte[] data = asBytes(champions);

            // 2. transform data
            data = writeTransform(data);

            // 3. write to file
            write(data, getChampionsFile());

        } catch (Exception ex) {
            Logger.error("Can`t save " + Champions.class.getSimpleName(), ex);
        }
    }

    public Champions load() {
        File file = getChampionsFile();
        if (!file.exists())
            return new Champions();

        try {
            // 1. read from file
            byte[] data = read(file);

            // 2. transform data
            data = readTransform(data);

            // 3. deserialize
            return fromBytes(data);

        } catch (Exception ex) {
            Logger.error("Can`t load " + Champions.class.getSimpleName(), ex);
            return new Champions();
        }
    }

    protected abstract File getChampionsFile();

}
