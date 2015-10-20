/// <summary>
/// Делегаты, для уведомлений от мозаики
/// </summary>
namespace fmg.core.types.Event {

   // <summary> ClickEventHandler - уведомление о том, что на мозаике был произведён клик </summary>
   public delegate void ClickEventHandler(object sender, MosaicEvent.ClickEventArgs e);

   /// <summary> ChangedCountersEventHandler - изменено кол-во открытых ячеек / флагов / кликов / ... на мозаике </summary>
   public delegate void ChangedCountersEventHandler(object sender, MosaicEvent.ChangedCountersEventArgs e);

   /// <summary> ChangedGameStatusEventHandler - уведомление об изменении статуса игры (новая игра, начало игры, конец игры) </summary>
   public delegate void ChangedGameStatusEventHandler(object sender, MosaicEvent.ChangedGameStatusEventArgs e);

   /// <summary> ChangedAreaEventHandler - уведомление об изменении размера площади у ячейки </summary>
   public delegate void ChangedAreaEventHandler(object sender, MosaicEvent.ChangedAreaEventArgs e);

   /// <summary> ChangedMosaicTypeEventHandler - уведомление об изменении типа мозаики </summary>
   public delegate void ChangedMosaicTypeEventHandler(object sender, MosaicEvent.ChangedMosaicTypeEventArgs e);

   /// <summary> ChangedMosaicSizeEventHandler - уведомление об изменении размера мозаики </summary>
   public delegate void ChangedMosaicSizeEventHandler(object sender, MosaicEvent.ChangedMosaicSizeEventArgs e);
}