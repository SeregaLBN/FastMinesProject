package fmg.core.mosaic;

import java.util.concurrent.ThreadLocalRandom;

import fmg.core.img.*;
import fmg.core.types.draw.PenBorder;

/** MVC: mosaic image controller. Base implementation */
public abstract class MosaicImageController<TImage,        TMosaicView extends MosaicView<TImage, Void, MosaicAnimatedModel<Void>>>
                   extends MosaicController<TImage, Void,  TMosaicView,                                 MosaicAnimatedModel<Void>>
             implements IAnimatedController<TImage,        TMosaicView,                                 MosaicAnimatedModel<Void>>
{
    private final AnimatedInnerController<TImage, TMosaicView, MosaicAnimatedModel<Void>> _innerController;

    public MosaicImageController(TMosaicView view) {
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
