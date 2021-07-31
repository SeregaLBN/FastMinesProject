package fmg.swing.app.serializers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import fmg.core.app.ISerializer;
import fmg.core.types.model.Statistic;

public class StatisticSerializer implements ISerializer {

    private static final long VERSION = 1;

    public void write(Statistic data, ObjectOutput to) throws IOException {
        to.writeLong(VERSION);
        to.writeLong(data.gameNumber);
        to.writeLong(data.gameWin);
        to.writeLong(data.openField);
        to.writeLong(data.playTime);
        to.writeLong(data.clickCount);
    }

    public void read(Statistic to, ObjectInput from) throws IOException {
        long version = from.readLong();
        if (version != VERSION)
            throw new RuntimeException("Unsupported " + Statistic.class.getSimpleName() + " version " + version);

        to.gameNumber = from.readLong();
        to.gameWin    = from.readLong();
        to.openField  = from.readLong();
        to.playTime   = from.readLong();
        to.clickCount = from.readLong();
    }

}