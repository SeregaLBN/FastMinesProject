package fmg.core.mosaic;

import java.util.concurrent.ThreadLocalRandom;

import fmg.core.img.MosaicAnimatedModel;
import fmg.core.img.MosaicRotateTransformer;
import fmg.data.view.draw.PenBorder;
import fmg.swing.draw.img.MosaicImg;

/** MVC: mosaic image controller. Base implementation */
public abstract class AMosaicImageController<TImage, TMosaicView extends MosaicImg<TImage>>
              extends AMosaicController<TImage, Void, TMosaicView, MosaicAnimatedModel<Void>>
{

   public AMosaicImageController(TMosaicView view) {
      super(view);
      addModelTransformer(new MosaicRotateTransformer());

      MosaicAnimatedModel<Void> model = getModel();
      PenBorder pen = model.getPenBorder();
      pen.setColorLight(pen.getColorShadow());
      model.getBackgroundFill().setMode(1 + ThreadLocalRandom.current().nextInt(model.getCellAttr().getMaxBackgroundFillModeValue()));
   }

}
