package fmg.core.app.serializers;

import java.io.*;
import java.util.List;

import fmg.common.Logger;
import fmg.core.app.model.Players;
import fmg.core.app.model.Players.Record;
import fmg.core.app.model.Statistics;
import fmg.core.app.model.User;

/** Players base (de)serializer */
public abstract class PlayersSerializer implements ISerializer {

    protected static final long VERSION = 1;

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
            throw new IOException("Unsupported " + Players.class.getSimpleName() + " version " + version);

        int size = from.readInt();
        RecordSerializer rs = new RecordSerializer();
        Players res = new Players();
        List<Record> all = res.getRecords();
        for (int i = 0; i < size; i++)
            all.add(rs.read(from));

        return res;
    }

    /** serialize to bytes */
    private byte[] asBytes(Players players) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            write(players, oos);
            oos.flush();

            return baos.toByteArray();
        }
    }

    /** deserilize from bytes */
    private Players fromBytes(byte[] data) throws IOException {
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
                throw new IOException("Invalid file data. Unsupported " + Players.class.getSimpleName() + " version " + version);

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

    public void save(Players players) {
        Logger.debug("> PlayersSerializer::Save");
        try {
            // 1. serialize
            byte[] data = asBytes(players);

            // 2. transform data
            data = writeTransform(data);

            // 3. write to file
            write(data, getPlayersFile());

        } catch (Exception ex) {
            Logger.error("Can`t save " + Players.class.getSimpleName(), ex);
        } finally {
            Logger.debug("< PlayersSerializer::Save");
        }
    }

    public Players load() {
        Logger.debug("> PlayersSerializer::Load");
        try {
            File file = getPlayersFile();
            if (!file.exists())
                return new Players();

            // 1. read from file
            byte[] data = read(file);

            // 2. transform data
            data = readTransform(data);

            // 3. deserialize
            return fromBytes(data);

        } catch (Exception ex) {
            Logger.error("Can`t load " + Players.class.getSimpleName(), ex);
            return new Players();
        } finally {
            Logger.debug("< PlayersSerializer::Load");
        }
    }

    protected abstract File getPlayersFile();

}
