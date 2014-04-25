using ua.ksn.geom;
using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.model.mosaics;

// Делегаты, для уведомлений от мозаики
namespace ua.ksn.fmg.controller.Event {

   /// <summary> уведомление о том, что на мозаике был произведён клик </summary>
   public delegate void OnClickEvent(Mosaic source, bool leftClick, bool down);

   /// <summary> изменено кол-во открытых ячеек / флагов / кликов / ... на мозаике </summary>
   public delegate void OnChangedCountersEvent(Mosaic source);

   /// <summary> уведомление об изменении статуса игры (новая игра, начало игры, конец игры) </summary>
   public delegate void OnChangedGameStatusEvent(Mosaic source, EGameStatus oldValue);

   /// <summary> уведомление об изменении размера площади у ячейки </summary>
   public delegate void OnChangedAreaEvent(Mosaic source, int oldArea);

   /// <summary> уведомление об изменении типа мозаики </summary>
   public delegate void OnChangedMosaicTypeEvent(Mosaic source, EMosaic oldMosaic);

   /// <summary> уведомление об изменении размера мозаики </summary>
   public delegate void OnChangedMosaicSizeEvent(Mosaic source, Size oldSize);
}