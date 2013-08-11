package ua.ksn.fmg.controller.event;

import java.util.EventListener;

/** ���������, ������� ������ ����������� ��������� ����������� �� ������� */
public interface MosaicListener extends EventListener {
	/** ����������� � ���, ��� �� ������� ��� ��������� ���� */
	void OnClick(MosaicEvent e); // BOOL leftClick, BOOL down

	/** ����������� � ���������� �������� �������/��� */
	void OnChangeCounters(MosaicEvent e);

	/** ����������� � ��������� ������� ���� (����� ����, ������ ����, ����� ����) */
	void OnChangeGameStatus(MosaicEvent e); // EGameStatus

	/** ����������� � ��������� ������� ������� � ������ */
	void OnChangeArea(MosaicEvent e);

	/** ����������� � ��������� ���� ������� */
	void OnChangeMosaicType(MosaicEvent e);
}
