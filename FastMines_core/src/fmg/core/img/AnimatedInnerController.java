package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;

/**
 * MVC controller. Base animation controller.
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 * @param <TImageModel> MVC model
 */
public final class AnimatedInnerController<TImage,
                                           TImageView  extends IImageView<TImage, TImageModel>,
                                           TImageModel extends IAnimatedModel>
            implements IAnimatedController<TImage, TImageView, TImageModel>
{

    private final TImageModel model;
    private Map<Class<? extends IModelTransformer>, IModelTransformer> transformers = new HashMap<>();
    private boolean animationWasUsed = false;
    private final PropertyChangeListener onModelPropertyChangedListener = this::onModelPropertyChanged;

    public AnimatedInnerController(TImageModel model) {
        this.model = model;
        model.addListener(onModelPropertyChangedListener);
    }


    // #region: begin unusable code
    @Override
    public TImageModel getModel()                               { throw new UnsupportedOperationException(); }
    @Override
    public TImage getImage()                                    { throw new UnsupportedOperationException(); }
    @Override
    public SizeDouble getSize()                                 { throw new UnsupportedOperationException(); }
    @Override
    public void addListener(PropertyChangeListener listener)    { throw new UnsupportedOperationException(); }
    @Override
    public void removeListener(PropertyChangeListener listener) { throw new UnsupportedOperationException(); }
    @Override
    public void useRotateTransforming(boolean enable)           { throw new UnsupportedOperationException(); }
    @Override
    public void usePolarLightFgTransforming(boolean enable)     { throw new UnsupportedOperationException(); }
    // #region: end unusable code


    @Override
    public void addModelTransformer(IModelTransformer transformer) {
        if (!transformers.keySet().contains(transformer.getClass()))
            transformers.put(transformer.getClass(), transformer);
    }
    @Override
    public void removeModelTransformer(Class<? extends IModelTransformer> transformerClass) {
        if (transformers.keySet().contains(transformerClass))
            transformers.remove(transformerClass);
    }

    private void onModelPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case IAnimatedModel.PROPERTY_ANIMATED:
            animationWasUsed = true;
            if ((Boolean)ev.getNewValue()) {
                UiInvoker.ANIMATOR.get().subscribe(this, timeFromStartSubscribe -> {
                    long mod = timeFromStartSubscribe % model.getAnimatePeriod();
                    long frame = mod * model.getTotalFrames() / model.getAnimatePeriod();
                    //Logger.info("ANIMATOR : " + getClass().getSimpleName() + ": "+ timeFromStartSubscribe);
                    model.setCurrentFrame((int)frame);
                });
            } else {
                UiInvoker.ANIMATOR.get().pause(this);
            }
            break;
        case IAnimatedModel.PROPERTY_CURRENT_FRAME:
            if (transformers.size() == 0)
                Logger.error("No any transformer! " + getClass().getSimpleName()); // зачем работать анимации если нет трансформеров модели
            transformers.forEach((k,v) -> v.execute(model));
            break;
        }
    }

    @Override
    public void close() {
        model.removeListener(onModelPropertyChangedListener);
        if (animationWasUsed) // do not call UiInvoker.ANIMATOR if it is not already used
            UiInvoker.ANIMATOR.get().unsubscribe(this);
        transformers.clear();
    }

}
