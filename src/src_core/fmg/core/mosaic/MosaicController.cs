using System;
using fmg.core.mosaic.cells;
using fmg.core.mosaic.draw;

namespace fmg.core.mosaic {

   /// <summary> MVC: controller. Default implementation </summary>
   public class MosaicController<TMosaicView, TPaintable, TImage, TPaintContext> : AMosaicController<TMosaicView>
      where TMosaicView : AMosaicView<TPaintable, TImage, TPaintContext>, new()
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintContext<TImage>, new()
   {

      /// <summary> MVC: model </summary>
      public override MosaicBase Mosaic {
         get {
            if (_mosaic == null)
               Mosaic = new MosaicIntenal(); // call setter
            return _mosaic;
         }
      }

      /// <summary> MVC: view </summary>
      public override TMosaicView View {
         get {
            if (_view == null)
               View = new TMosaicView(); // call setter
            return _view;
         }
         protected set {
            if (_view != null)
               _view.Dispose();
            _view = value;
            if (_view != null) {
               var mosaic = Mosaic;
               _view.Mosaic = mosaic;
               (mosaic as MosaicIntenal).View = _view;
            }
         }
      }

      protected class MosaicIntenal : MosaicBase {

         public TMosaicView View { get; set; }

         protected override bool CheckNeedRestoreLastGame() {
            // TODO: override in child classes
            return base.CheckNeedRestoreLastGame();
         }

         public override bool GameNew() {
            var mode = 1 + new Random(Guid.NewGuid().GetHashCode()).Next(MosaicHelper.CreateAttributeInstance(MosaicType).getMaxBackgroundFillModeValue());
            //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
            View.PaintContext.BkFill.Mode = mode;
            var res = base.GameNew();
            if (!res)
               View.Invalidate();
            return res;
         }

         public override void GameBegin(BaseCell firstClickCell) {
            View.PaintContext.BkFill.Mode = 0;
            base.GameBegin(firstClickCell);
         }

      }

   }

}
