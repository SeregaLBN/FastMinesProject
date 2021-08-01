package fmg.swing.app.serializers;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import fmg.common.Logger;
import fmg.common.crypt.Simple3DES;
import fmg.core.app.AProjSettings;
import fmg.core.app.ISerializer;
import fmg.core.types.model.Players;
import fmg.core.types.model.Players.Record;
import fmg.core.types.model.Statistics;
import fmg.core.types.model.User;

/** хранилище пользователей и их игровой статистики */
public class PlayersSerializer implements ISerializer {

    private static final long VERSION = 1;

    private static class RecordSerializer implements ISerializer {

        void write(Players.Record rec, ObjectOutput to) throws IOException {
            new UserSerializer().write(rec.user, to);
            StatisticsSerializer ss = new StatisticsSerializer();
            for (Statistics[] record: rec.statistics)
                for (Statistics subRecord: record)
                    ss.write(subRecord, to);
        }

        Players.Record read(ObjectInput from) throws IOException {
            User user = new UserSerializer().read(from);
            Players.Record res = new Players.Record(user);
            StatisticsSerializer ss = new StatisticsSerializer();
            for (Statistics[] record: res.statistics)
                for (Statistics subRecord: record)
                    ss.read(subRecord, from);
            return res;
        }

    }

    private void write(Players players, ObjectOutput to) throws IOException {
        to.writeLong(VERSION);
        to.writeInt(players.size());
        RecordSerializer rs = new RecordSerializer();
        for (Players.Record rec : players.getRecords())
            rs.write(rec, to);
    }

    private Players read(ObjectInput from) throws IOException {
        long version = from.readLong();
        if (version != VERSION)
            throw new RuntimeException("Unsupported " + Players.class.getSimpleName() + " version " + version);

        int size = from.readInt();
        RecordSerializer rs = new RecordSerializer();
        Players res = new Players();
        List<Record> all = res.getRecords();
        for (int i = 0; i < size; i++)
            all.add(rs.read(from));

        return res;
    }

    public void save(Players players) {
        byte[] data;
        // 1. serialize
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            write(players, oos);
            oos.flush();

            data = baos.toByteArray();

        } catch (Exception ex) {
            Logger.error("Can`t serialize data " + Players.class.getSimpleName(), ex);
            return;
        }

        // 2. crypt data
        byte[] cryptedData;
        // 1. serializable object
        try  {
            cryptedData = new Simple3DES(getSerializeKey()).encrypt(data);

        } catch (Exception ex) {
            Logger.error("Can`t crypt data " + Players.class.getSimpleName(), ex);
            return;
        }

        // 3. write to file
        try (OutputStream fos = new FileOutputStream(getStcFile());
             ObjectOutputStream oot = new ObjectOutputStream(fos))
        {
            oot.writeLong(VERSION); // save version and decrypt key
            int len = cryptedData.length;
            oot.writeInt(len);
            oot.write(cryptedData);
        } catch (Exception ex) {
            Logger.error("Can`t save " + Players.class.getSimpleName(), ex);
        }

    }

    public Players load() {
        File file = getStcFile();
        if (!file.exists())
            return new Players();

        // 1. read from file
        byte[] cryptedData;
        try (InputStream fis = new FileInputStream(file);
             ObjectInputStream oin = new ObjectInputStream(fis))
        {
            long version = oin.readLong();
            if (version != VERSION)
                throw new RuntimeException("Invalid file data. Unsupported " + Players.class.getSimpleName() + " version " + version);

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
            Logger.error("Can`t load " + Players.class.getSimpleName(), ex);
            return new Players();
        }

        // 2. decrypt data
        byte[] data;
        try {
            data = new Simple3DES(getSerializeKey()).decrypt(cryptedData);
        } catch (Exception ex) {
            Logger.error("Can`t decrypt " + Players.class.getSimpleName(), ex);
            return new Players();
        }

        // 3. deserialize
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais))
        {
            return read(ois);
        } catch (Exception ex) {
            Logger.error("Can`t deserialize data " + Players.class.getSimpleName(), ex);
            return new Players();
        }
    }

    private static String getSerializeKey() throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("MD5")
                                     .digest(Long.toString(VERSION)
                                                 .getBytes(StandardCharsets.UTF_8));
        return String.format("%032X", new BigInteger(1, digest));
    }

    private static File getStcFile() {
        return new File(AProjSettings.getStatisticsFileName());
    }

}
