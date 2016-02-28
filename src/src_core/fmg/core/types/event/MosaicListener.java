package fmg.core.types.event;

import java.util.EventListener;

/** Интерфейс, который должен реализовать слушатель уведомлений от мозаики */
public interface MosaicListener extends EventListener {
   /** уведомление о том, что на мозаике был произведён клик */
   void OnClick(MosaicEvent.ClickEvent e);
}