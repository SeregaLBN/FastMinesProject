package ua.ksn.fmg.controller.event;

import java.util.EventListener;

/** ���������, ������� ������ ����������� ��������� ����������� �� ������� */
public interface MosaicListener extends EventListener {
	/** ����������� � ���, ��� �� ������� ��� ��������� ���� */
	void OnClick(MosaicEvent.ClickEvent e);

	/** �����������: �������� ���-�� �������� ����� / ������ / ������ / ... �� ������� */
	void OnChangedCounters(MosaicEvent.ChangedCountersEvent e);

	/** ����������� �� ��������� ������� ���� (����� ����, ������ ����, ����� ����) */
	void OnChangedGameStatus(MosaicEvent.ChangedGameStatusEvent e);

	/** ����������� �� ��������� ������� ������� � ������ */
	void OnChangedArea(MosaicEvent.ChangedAreaEvent e);

	/** ����������� �� ��������� ���� ������� */
	void OnChangedMosaicType(MosaicEvent.ChangedMosaicTypeEvent e);

	/** ����������� �� ��������� ������� ������� */
	void OnChangedMosaicSize(MosaicEvent.ChangedMosaicSizeEvent e);
}