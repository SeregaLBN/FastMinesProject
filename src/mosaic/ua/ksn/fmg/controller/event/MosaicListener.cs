using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.model.mosaics;

/// <summary>Делегаты, для уведомлений от мозаики</summary>
namespace ua.ksn.fmg.controller.Event {

   /// <summary>уведомление о том, что на мозаике был произведён клик</summary>
   public delegate void OnClickEvent(Mosaic source, bool leftClick, bool down);

   /// <summary>изменено кол-во открытых ячеек / флагов / кликов / ... на мозаике</summary>
   public delegate void OnChangeCountersEvent(Mosaic source);

   /// <summary>уведомление о изменении статуса игры (новая игра, начало игры, конец игры)</summary>
   public delegate void OnChangeGameStatusEvent(Mosaic source, EGameStatus oldValue);

   /// <summary>уведомление о изменении размера площади у ячейки</summary>
   public delegate void OnChangeAreaEvent(Mosaic source, int oldArea);

   /// <summary>уведомление о изменении типа мозаики</summary>
   public delegate void OnChangeMosaicTypeEvent(Mosaic source, EMosaic oldMosaic);
}