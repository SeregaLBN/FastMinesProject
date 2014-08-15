package ua.ksn.fmg.controller.serializable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class StatisticCounts implements Externalizable {
	public long
		gameNumber, // количество сыгранных игр
		gameWin,    // количество выиграных игр
		openField,  // суммарное число открытых ячеек - вывожу средний процент открытия поля
		playTime,   // суммарное время игр - вывожу сколько всреднем игрок провёл времени за данной игрой
		clickCount; // суммарное число кликов - вывожу среднее число кликов в данной игре

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		gameNumber = in.readLong();
		gameWin    = in.readLong();
		openField  = in.readLong();
		playTime   = in.readLong();
		clickCount = in.readLong();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
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