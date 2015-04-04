package fmg.core.types.event;

import java.util.EventListener;

/** Интерфейс, который должен реализовать слушатель уведомлений от мозаики */
public interface MosaicListener extends EventListener {
	/** уведомление о том, что на мозаике был произведён клик */
	void OnClick(MosaicEvent.ClickEvent e);

	/** уведомление: изменено кол-во открытых ячеек / флагов / кликов / ... на мозаике */
	void OnChangedCounters(MosaicEvent.ChangedCountersEvent e);

	/** уведомление об изменении статуса игры (новая игра, начало игры, конец игры) */
	void OnChangedGameStatus(MosaicEvent.ChangedGameStatusEvent e);

	/** уведомление об изменении размера площади у ячейки */
	void OnChangedArea(MosaicEvent.ChangedAreaEvent e);

	/** уведомление об изменении типа мозаики */
	void OnChangedMosaicType(MosaicEvent.ChangedMosaicTypeEvent e);

	/** уведомление об изменении размера мозаики */
	void OnChangedMosaicSize(MosaicEvent.ChangedMosaicSizeEvent e);
}