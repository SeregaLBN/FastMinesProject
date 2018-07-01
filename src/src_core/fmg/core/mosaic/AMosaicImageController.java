package fmg.core.mosaic;

import java.util.concurrent.ThreadLocalRandom;

import fmg.core.img.*;
import fmg.data.view.draw.PenBorder;

/** MVC: mosaic image controller. Base implementation */
public abstract class AMosaicImageController<TImage,        TMosaicView extends AMosaicView<TImage, Void, MosaicAnimatedModel<Void>>>
                   extends AMosaicController<TImage, Void,  TMosaicView,                                  MosaicAnimatedModel<Void>>
              implements IAnimatedController<TImage,        TMosaicView,                                  MosaicAnimatedModel<Void>>
{
   private final AnimatedInnerController<TImage, TMosaicView, MosaicAnimatedModel<Void>> _innerController;

   public AMosaicImageController(TMosaicView view) {
      super(view);
      MosaicAnimatedModel<Void> model = getModel();
      _innerController = new AnimatedInnerController<>(model);
      addModelTransformer(new MosaicRotateTransformer());

      PenBorder pen = model.getPenBorder();
      pen.setColorLight(pen.getColorShadow());
      model.getBackgroundFill().setMode(1 + ThreadLocalRandom.current().nextInt(model.getCellAttr().getMaxBackgroundFillModeValue()));
   }

   @Override
   public void addModelTransformer(IModelTransformer transformer) {
      _innerController.addModelTransformer(transformer);
   }
   @Override
   public void removeModelTransformer(Class<? extends IModelTransformer> transformerClass) {
      _innerController.removeModelTransformer(transformerClass);
   }

}
