using System;
using fmg.common;
using fmg.core.img;

namespace fmg.core.mosaic {

   /// <summary> MVC: mosaic image controller. Base implementation </summary>
   public abstract class AMosaicImageController<TImage,          TMosaicView>
                            : AMosaicController<TImage, Nothing, TMosaicView, MosaicAnimatedModel<Nothing>>,
                            IAnimatedController<TImage,          TMosaicView, MosaicAnimatedModel<Nothing>>
      where TImage : class
      where TMosaicView : AMosaicView<TImage, Nothing, MosaicAnimatedModel<Nothing>>
   {

      private readonly AnimatedInnerController<TImage, TMosaicView, MosaicAnimatedModel<Nothing>> _innerController;

      public AMosaicImageController(TMosaicView view)
         : base(view)
      {
         var model = Model;
         _innerController = new AnimatedInnerController<TImage, TMosaicView, MosaicAnimatedModel<Nothing>>(model);
         AddModelTransformer(new MosaicRotateTransformer<TImage>());

         var pen = model.PenBorder;
         pen.ColorLight = pen.ColorShadow;
         model.BkFill.Mode = 1 + ThreadLocalRandom.Current.Next(model.CellAttr.GetMaxBackgroundFillModeValue());
      }

      public void AddModelTransformer(IModelTransformer transformer) {
         _innerController.AddModelTransformer(transformer);
      }
      public void RemoveModelTransformer(Type /* extends IModelTransformer */ transformerClass) {
         _innerController.RemoveModelTransformer(transformerClass);
      }

   }

}
