using System.IO;

namespace ua.ksn.fmg.controller.serializable {

public class StatisticCounts : IExternalizable {
	public long
		gameNumber, // ���������� ��������� ���
		gameWin,    // ���������� ��������� ���
		openField,  // ��������� ����� �������� ����� - ������ ������� ������� �������� ����
		playTime,   // ��������� ����� ��� - ������ ������� �������� ����� ����� ������� �� ������ �����
		clickCount; // ��������� ����� ������ - ������ ������� ����� ������ � ������ ����

	public void readExternal(BinaryReader input) {
		gameNumber = input.ReadInt64();
		gameWin    = input.ReadInt64();
		openField  = input.ReadInt64();
		playTime   = input.ReadInt64();
      clickCount = input.ReadInt64();
	}
	public void writeExternal(BinaryWriter output) {
		output.Write(gameNumber);
		output.Write(gameWin);
		output.Write(openField);
		output.Write(playTime);
		output.Write(clickCount);
	}

	public StatisticCounts clone() {
      return (StatisticCounts)this.MemberwiseClone();
      //StatisticCounts clone = new StatisticCounts();
      //clone.gameNumber = this.gameNumber;
      //clone.gameWin    = this.gameWin;
      //clone.openField  = this.openField;
      //clone.playTime   = this.playTime;
      //clone.clickCount = this.clickCount;
      //return clone;
	}
}
}