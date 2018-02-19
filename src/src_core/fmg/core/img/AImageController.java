package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/**
 *
 * MVC: controller.
 * Base implementation of image controller (manipelations with the image).
 *
 * @param <TImage> plaform specific image
 * @param <TImageView> image view
 * @param <TImageModel> image model
 **/
public abstract class AImageController<TImage,
                                       TImageView  extends IImageView<TImage>,
                                       TImageModel extends IImageModel>
                extends NotifyPropertyChanged
                implements AutoCloseable
{

   public static Consumer<Runnable> DEFERR_INVOKER;

   private final TImageView  _imageView;
   private final TImageModel _imageModel;
   private final PropertyChangeListener _imageViewListener, _imageModelListener;

   protected AImageController(TImageView imageView, TImageModel imageModel) {
      _imageView = imageView;
      _imageModel = imageModel;

      _imageView.setSize(_imageModel.getSize());

      _imageViewListener = event -> {
         assert event.getSource() == _imageView; // by reference

         switch (event.getPropertyName()) {
         case IImageView.PROPERTY_SIZE:
            _imageModel.setSize((Size)event.getNewValue());
            break;
         case IImageView.PROPERTY_IMAGE:
            this.onPropertyChanged(event.getOldValue(), event.getNewValue(), IImageView.PROPERTY_IMAGE);
            break;
         }
      };
      _imageModelListener = event -> {
         assert event.getSource() == _imageModel; // by reference

         _imageView.invalidate();
         if (event.getPropertyName().equals(IImageModel.PROPERTY_SIZE))
            _imageView.setSize((Size)event.getNewValue());
      };

      _imageModel.addListener(_imageModelListener);
      _imageView.addListener(_imageViewListener);
   }

   public TImage getImage() {
      return _imageView.getImage();
   }

   private boolean _deferredNotifications = true;
   public boolean isDeferredNotifications() { return _deferredNotifications; }
   public void setDeferredNotifications(boolean value) { _deferredNotifications = value; }

   /** Deferr notifications */
   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if (!isDeferredNotifications())
         super.onPropertyChanged(oldValue, newValue, propertyName);
      else
         DEFERR_INVOKER.accept( () -> super.onPropertyChanged(oldValue, newValue, propertyName) );
   }

   @Override
   public void close() {
      _imageModel.removeListener(_imageModelListener);
      _imageView.removeListener(_imageViewListener);
      super.close();
   }

}
