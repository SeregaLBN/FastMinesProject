using fmg.common;
using fmg.core.img;
using fmg.data.view.draw;

namespace fmg.core.mosaic {

   /// <summary> MVC: mosaic image controller. Base implementation </summary>
   public abstract class AMosaicImageController<TImage, TMosaicView>
                            : AMosaicController<TImage, Nothing, TMosaicView, MosaicAnimatedModel<Nothing>>
      where TImage : class
      where TMosaicView : AMosaicView<TImage, Nothing, MosaicAnimatedModel<Nothing>>
   {

      public AMosaicImageController(TMosaicView view)
         : base(view)
      {
         AddModelTransformer(new MosaicRotateTransformer<TImage>());

         var model = Model;
         var pen = model.PenBorder;
         pen.ColorLight = pen.ColorShadow;
         model.BkFill.Mode = 1 + ThreadLocalRandom.Current.Next(model.CellAttr.GetMaxBackgroundFillModeValue());
      }

   }

}
