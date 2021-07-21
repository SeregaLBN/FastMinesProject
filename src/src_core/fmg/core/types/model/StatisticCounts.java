package fmg.core.types.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class StatisticCounts implements Externalizable {

    private static final long VERSION = 1;

    /** количество сыгранных игр */
    public long gameNumber;
    /** количество выиграных игр */
    public long gameWin;
    /** суммарное число открытых ячеек - вывожу средний процент открытия поля */
    public long openField;
    /** суммарное время игр (milliseconds) - вывожу сколько всреднем игрок провёл времени за данной игрой */
    public long playTime;
    /** суммарное число кликов - вывожу среднее число кликов в данной игре */
    public long clickCount;

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        long version = in.readLong();
        if (version != VERSION)
            throw new RuntimeException("Unsupported " + StatisticCounts.class.getSimpleName() + " version " + version);

        gameNumber = in.readLong();
        gameWin    = in.readLong();
        openField  = in.readLong();
        playTime   = in.readLong();
        clickCount = in.readLong();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(VERSION);
        out.writeLong(gameNumber);
        out.writeLong(gameWin);
        out.writeLong(openField);
        out.writeLong(playTime);
        out.writeLong(clickCount);
    }

    @Override
    protected StatisticCounts clone() throws CloneNotSupportedException {
        StatisticCounts clone = new StatisticCounts();
        clone.gameNumber = this.gameNumber;
        clone.gameWin    = this.gameWin;
        clone.openField  = this.openField;
        clone.playTime   = this.playTime;
        clone.clickCount = this.clickCount;
        return clone;
    }

}