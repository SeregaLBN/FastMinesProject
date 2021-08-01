package fmg.swing.app.serializers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fmg.core.app.ISerializer;
import fmg.core.types.model.Statistics;

public class StatisticsSerializer implements ISerializer {

    private static final long VERSION = 1;

    public void write(Statistics data, ObjectOutput to) throws IOException {
        to.writeLong(VERSION);
        to.writeLong(data.gameNumber);
        to.writeLong(data.gameWin);
        to.writeLong(data.openField);
        to.writeLong(data.playTime);
        to.writeLong(data.clickCount);
    }

    public void read(Statistics to, ObjectInput from) throws IOException {
        long version = from.readLong();
        if (version != VERSION)
            throw new RuntimeException("Unsupported " + Statistics.class.getSimpleName() + " version " + version);

        to.gameNumber = from.readLong();
        to.gameWin    = from.readLong();
        to.openField  = from.readLong();
        to.playTime   = from.readLong();
        to.clickCount = from.readLong();
    }

}