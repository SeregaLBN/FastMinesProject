package ksn.fm.Mosaic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * ����� �������� ��� ����������
 */
public class CAssistantInfo implements Externalizable {
	public boolean m_bUse;         // on/off m_Assistant
	private final class TimeOut{
		int m_iFirst;     // ������� (� ��������) ������� ������������ ���������� (����� ������� ����������� ���������� ��� ����������� ������������)
		int m_iNext;      // ������� (� �������������) ��������� ������������ ����������
	};
	public TimeOut m_Timeout;
	boolean m_bAutoStart;   // autostart new game ?
	boolean m_bStopWork;    // ������������� ����� ��� ������������ ���������� ���� ?
	boolean m_bPlayIsInvisible; // �������� ���� ���� ��������

	public CAssistantInfo() {
		m_bUse             = true;
		m_bAutoStart       = true;
		m_bStopWork        = false;
		m_bPlayIsInvisible = true;
		m_Timeout.m_iFirst = 30;
		m_Timeout.m_iNext  = 100;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		m_bUse             = in.readBoolean();
		m_bAutoStart       = in.readBoolean();
		m_bStopWork        = in.readBoolean();
		m_bPlayIsInvisible = in.readBoolean();
		m_Timeout.m_iFirst = in.readInt();
		m_Timeout.m_iNext  = in.readInt();
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(m_bUse);
		out.writeBoolean(m_bAutoStart);
		out.writeBoolean(m_bStopWork);
		out.writeBoolean(m_bPlayIsInvisible);
		out.writeInt(m_Timeout.m_iFirst);
		out.writeInt(m_Timeout.m_iNext);
	}
}
