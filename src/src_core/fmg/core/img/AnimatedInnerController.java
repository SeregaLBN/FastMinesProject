package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;

/**
 * MVC controller. Base animation controller.
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 * @param <TImageModel> MVC model
 */
public final class AnimatedInnerController<TImage,
                                           TImageView  extends IImageView<TImage, TImageModel>,
                                           TImageModel extends IAnimatedModel>
            implements IAnimatedController<TImage, TImageView, TImageModel>
{

   private final TImageModel _model;
   private Map<Class<? extends IModelTransformer>, IModelTransformer> _transformers = new HashMap<>();
   private final PropertyChangeListener _imageModelListener = ev -> onPropertyModelChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
   private boolean _animationWasUsed = false;

   public AnimatedInnerController(TImageModel model) {
      _model = model;
      model.addListener(_imageModelListener);
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
   // #region: end unusable code


   @Override
   public void addModelTransformer(IModelTransformer transformer) {
      if (!_transformers.keySet().contains(transformer.getClass()))
         _transformers.put(transformer.getClass(), transformer);
   }
   @Override
   public void removeModelTransformer(Class<? extends IModelTransformer> transformerClass) {
      if (_transformers.keySet().contains(transformerClass))
         _transformers.remove(transformerClass);
   }

   private void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      switch (propertyName) {
      case IAnimatedModel.PROPERTY_ANIMATED:
         _animationWasUsed = true;
         if ((Boolean)newValue) {
            TImageModel model = _model;
            Factory.GET_ANIMATOR.get().subscribe(this, timeFromStartSubscribe -> {
               long mod = timeFromStartSubscribe % model.getAnimatePeriod();
               long frame = mod * model.getTotalFrames() / model.getAnimatePeriod();
               //System.out.println("ANIMATOR : " + getClass().getSimpleName() + ": "+ timeFromStartSubscribe);
               model.setCurrentFrame((int)frame);
            });
         } else {
            Factory.GET_ANIMATOR.get().pause(this);
         }
         break;
      case IAnimatedModel.PROPERTY_CURRENT_FRAME:
         if (_transformers.size() == 0)
            System.err.println("No any transformer! " + getClass().getSimpleName()); // зачем работать анимации если нет трансформеров модели
         _transformers.forEach((k,v) -> v.execute(_model));
         break;
      }
   }

   @Override
   public void close() {
      _model.removeListener(_imageModelListener);
      if (_animationWasUsed) // do not call Factory.GET_ANIMATOR if it is not already used
         Factory.GET_ANIMATOR.get().unsubscribe(this);
      _transformers.clear();
   }

}
