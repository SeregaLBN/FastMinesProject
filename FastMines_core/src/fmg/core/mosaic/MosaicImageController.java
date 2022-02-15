package fmg.core.mosaic;

import java.util.concurrent.ThreadLocalRandom;

import fmg.core.img.*;
import fmg.core.types.draw.PenBorder;

/** MVC: mosaic animated image controller. Base implementation
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TMosaicView> mosaic view
 */
public abstract class MosaicImageController<TImage,        TMosaicView extends MosaicView<TImage, Void, MosaicAnimatedModel<Void>>>
                   extends MosaicController<TImage, Void,  TMosaicView,                                 MosaicAnimatedModel<Void>>
             implements IAnimatedController<TImage,        TMosaicView,                                 MosaicAnimatedModel<Void>>
{
    private final AnimatedInnerController<TImage, TMosaicView, MosaicAnimatedModel<Void>> innerController;

    protected MosaicImageController(TMosaicView view) {
        super(view);
        MosaicAnimatedModel<Void> model = getModel();
        innerController = new AnimatedInnerController<>(model);
        useRotateTransforming(true);

        PenBorder pen = model.getPenBorder();
        pen.setColorLight(pen.getColorShadow());
        model.getCellFill().setMode(1 + ThreadLocalRandom.current().nextInt(model.getShape().getMaxCellFillModeValue()));
    }

    @Override
    public void addModelTransformer(IModelTransformer transformer) {
        innerController.addModelTransformer(transformer);
    }
    @Override
    public void removeModelTransformer(Class<? extends IModelTransformer> transformerClass) {
        innerController.removeModelTransformer(transformerClass);
    }

    @Override
    public void useRotateTransforming(boolean enable) {
        if (enable)
            addModelTransformer(new MosaicRotateTransformer());
        else
            removeModelTransformer(MosaicRotateTransformer.class);
    }

    @Override
    public void usePolarLightFgTransforming(boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        innerController.close();
        super.close();
    }

}
