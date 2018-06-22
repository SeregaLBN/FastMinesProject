using fmg.common;
using fmg.core.img;
using fmg.data.view.draw;

namespace fmg.core.mosaic {

   /// <summary> MVC: mosaic image controller. Base implementation </summary>
   public abstract class AMosaicImageController<TImage, TMosaicView>
                            : AMosaicController<TImage, Void, TMosaicView, MosaicAnimatedModel<Void>>
      where TImage : class
      where TMosaicView : AMosaicView<TImage, Void, MosaicAnimatedModel<Void>>
   {

      public AMosaicImageController(TMosaicView view)
         : super(view)
      {
         AddModelTransformer(new MosaicRotateTransformer());

         var model = Model;
         var pen = model.PenBorder;
         pen.ColorLight = pen.ColorShadow;
         model.BackgroundFill.setMode(1 + ThreadLocalRandom.Current.next(model.CellAttr.MaxBackgroundFillModeValue));
      }

   }

}
