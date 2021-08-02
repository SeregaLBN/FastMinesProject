package fmg.swing.app.serializers;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import fmg.common.Logger;
import fmg.common.crypt.Simple3DES;
import fmg.core.app.AProjSettings;
import fmg.core.app.ISerializer;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.core.types.model.Champions;
import fmg.core.types.model.Champions.Record;

/** хранилище чемпионов */
public class ChampionsSerializer implements ISerializer {

    private static final long VERSION = 1;

    private static class RecordSerializer implements ISerializer {

        void write(Champions.Record rec, ObjectOutput to) throws IOException {
            to.writeUTF(rec.userId.toString());
            to.writeUTF(rec.userName);
            to.writeLong(rec.playTime);
        }

        Champions.Record read(ObjectInput from) throws IOException {
            Champions.Record res = new Record();
            res.userId = UUID.fromString(from.readUTF());
            res.userName = from.readUTF();
            res.playTime = from.readLong();
            return res;
        }
    }

    private void write(Champions champions, ObjectOutput to) throws IOException {
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

    public Champions read(ObjectInput from) throws IOException {
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

    public void save(Champions champions) {
        byte[] data;
        // 1. serialize
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            write(champions, oos);
            oos.flush();

            data = baos.toByteArray();

        } catch (Exception ex) {
            Logger.error("Can`t serialize data " + Champions.class.getSimpleName(), ex);
            return;
        }

        // 2. crypt data
        byte[] cryptedData;
        // 1. serializable object
        try  {
            cryptedData = new Simple3DES(getSerializeKey()).encrypt(data);

        } catch (Exception ex) {
            Logger.error("Can`t crypt data " + Champions.class.getSimpleName(), ex);
            return;
        }

        // 3. write to file
        try (OutputStream fos = new FileOutputStream(getChampFile());
             ObjectOutputStream oot = new ObjectOutputStream(fos))
        {
            oot.writeLong(VERSION); // save version and decrypt key
            int len = cryptedData.length;
            oot.writeInt(len);
            oot.write(cryptedData);
        } catch (Exception ex) {
            Logger.error("Can`t save " + Champions.class.getSimpleName(), ex);
        }

    }

    public Champions load() {
        File file = getChampFile();
        if (!file.exists())
            return new Champions();

        // 1. read from file
        byte[] cryptedData;
        try (InputStream fis = new FileInputStream(file);
             ObjectInputStream oin = new ObjectInputStream(fis))
        {
            long version = oin.readLong();
            if (version != VERSION)
                throw new RuntimeException("Invalid file data. Unsupported " + Champions.class.getSimpleName() + " version " + version);

            cryptedData = new byte[oin.readInt()];
            int read = 0;
            do {
                int curr = oin.read(cryptedData, read, cryptedData.length-read);
                if (curr < 0)
                    break;
                read += curr;
            } while(read < cryptedData.length);
            if (read != cryptedData.length)
                throw new IOException("Invalid data length. Required " + cryptedData.length + " bytes; read " + read + " bytes.");

        } catch (Exception ex) {
            Logger.error("Can`t load " + Champions.class.getSimpleName(), ex);
            return new Champions();
        }

        // 2. decrypt data
        byte[] data;
        try {
            data = new Simple3DES(getSerializeKey()).decrypt(cryptedData);
        } catch (Exception ex) {
            Logger.error("Can`t decrypt " + Champions.class.getSimpleName(), ex);
            return new Champions();
        }

        // 3. deserialize
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais))
        {
            return read(ois);
        } catch (Exception ex) {
            Logger.error("Can`t deserialize data " + Champions.class.getSimpleName(), ex);
            return new Champions();
        }
    }

    private static String getSerializeKey() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] digest = MessageDigest.getInstance("MD5")
                                     .digest(Long.toString(VERSION)
                                                 .getBytes(StandardCharsets.UTF_8));
        return String.format("%032X", new BigInteger(1, digest));
    }

    private static File getChampFile() {
        return new File(AProjSettings.getChampionsFileName());
    }

}
