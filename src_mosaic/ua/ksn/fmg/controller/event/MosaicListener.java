package ua.ksn.fmg.controller.event;

import java.util.EventListener;

/** »нтерфейс, который должен реализовать слушатель уведомлений от мозаики */
public interface MosaicListener extends EventListener {
	/** уведомление о том, что на мозаике был произведЄн клик */
	void OnClick(MosaicEvent e); // BOOL leftClick, BOOL down

	/** уведомление о изменени€х счЄтчика таймера/мин */
	void OnChangeCounters(MosaicEvent e);

	/** уведомление о изменении статуса игры (нова€ игра, начало игры, конец игры) */
	void OnChangeGameStatus(MosaicEvent e); // EGameStatus

	/** уведомление о изменении размера площади у €чейки */
	void OnChangeArea(MosaicEvent e);

	/** уведомление о изменении типа мозаики */
	void OnChangeMosaicType(MosaicEvent e);
}
