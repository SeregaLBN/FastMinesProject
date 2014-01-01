package ua.ksn.fmg.controller.event;

import java.util.EventListener;

/** ���������, ������� ������ ����������� ��������� ����������� �� ������� */
public interface MosaicListener extends EventListener {
	/** ����������� � ���, ��� �� ������� ��� ��������� ���� */
	void OnClick(MosaicEvent.ClickEvent e);

	/** �����������: �������� ���-�� �������� ����� / ������ / ������ / ... �� ������� */
	void OnChangeCounters(MosaicEvent.ChangeCountersEvent e);

	/** ����������� � ��������� ������� ���� (����� ����, ������ ����, ����� ����) */
	void OnChangeGameStatus(MosaicEvent.ChangeGameStatusEvent e);

	/** ����������� � ��������� ������� ������� � ������ */
	void OnChangeArea(MosaicEvent.ChangeAreaEvent e);

	/** ����������� � ��������� ���� ������� */
	void OnChangeMosaicType(MosaicEvent.ChangeMosaicTypeEvent e);
}
