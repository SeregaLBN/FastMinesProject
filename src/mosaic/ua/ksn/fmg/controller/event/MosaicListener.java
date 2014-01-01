package ua.ksn.fmg.controller.event;

import java.util.EventListener;

/** Интерфейс, который должен реализовать слушатель уведомлений от мозаики */
public interface MosaicListener extends EventListener {
	/** уведомление о том, что на мозаике был произведён клик */
	void OnClick(MosaicEvent.ClickEvent e);

	/** уведомление: изменено кол-во открытых ячеек / флагов / кликов / ... на мозаике */
	void OnChangeCounters(MosaicEvent.ChangeCountersEvent e);

	/** уведомление о изменении статуса игры (новая игра, начало игры, конец игры) */
	void OnChangeGameStatus(MosaicEvent.ChangeGameStatusEvent e);

	/** уведомление о изменении размера площади у ячейки */
	void OnChangeArea(MosaicEvent.ChangeAreaEvent e);

	/** уведомление о изменении типа мозаики */
	void OnChangeMosaicType(MosaicEvent.ChangeMosaicTypeEvent e);
}
