using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.mosaic.wbmp;

namespace fmg.uwp.img.wbmp {

   /// <summary>
   /// Representable {@link fmg.core.types.EMosaic} as image.
   /// UWP impl over WriteableBitmap.
   /// </summary>
   public class MosaicImg : AMosaicViewWBmp<Nothing, MosaicAnimatedModel<Nothing>> {

      protected bool _useBackgroundColor = true;

      protected MosaicImg()
         : base(new MosaicAnimatedModel<Nothing>())
      { }

      public override void Draw(IEnumerable<BaseCell> modifiedCells) {
         Draw(modifiedCells, null, _useBackgroundColor);
      }

      protected override void DrawBody() {
         //base.DrawBody(); // !hide base implementation

         MosaicAnimatedModel<Nothing> model = Model;

         _useBackgroundColor = true;
         switch (model.RotateMode) {
         case MosaicAnimatedModel<Nothing>.ERotateMode.fullMatrix:
            Draw(model.Matrix);
            break;
         case MosaicAnimatedModel<Nothing>.ERotateMode.someCells:
            // draw static part
            Draw(model.GetNotRotatedCells());

            // draw rotated part
            _useBackgroundColor = false;
            model.GetRotatedCells(rotatedCells => Draw(rotatedCells));
            break;
         }
      }

      protected override void Disposing() {
         Model.Dispose();
         base.Disposing();
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Smile image controller implementation for <see cref="MosaicImg"/> </summary>
      public class Controller : AMosaicImageController<WriteableBitmap, MosaicImg> {

         public Controller()
            : base(new MosaicImg())
         {
            _notifier.DeferredNotifications = !Windows.ApplicationModel.DesignMode.DesignModeEnabled;
         }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

   }

}
