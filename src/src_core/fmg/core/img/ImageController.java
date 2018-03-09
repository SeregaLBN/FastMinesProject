package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import fmg.common.notyfier.NotifyPropertyChanged;

/**
 * MVC: controller.
 * Base implementation of image controller (manipulations with the image).
 *
 * @param <TImage> plaform specific image or picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 **/
public abstract class ImageController<TImage,
                                      TImageView  extends IImageView<TImage, TImageModel>,
                                      TImageModel extends IImageModel>
                extends NotifyPropertyChanged
                implements AutoCloseable
{

   public static Consumer<Runnable> DEFERR_INVOKER;

   public static final String PROPERTY_IMAGE = IImageView.PROPERTY_IMAGE;

   private final TImageView  _imageView;
   private final PropertyChangeListener _imageViewListener;

   protected ImageController(TImageView imageView) {
      _imageView = imageView;

      _imageViewListener = event -> {
         assert event.getSource() == _imageView; // by reference
         onPropertyViewChanged(event.getOldValue(), event.getNewValue(), event.getPropertyName());
      };

      _imageView.addListener(_imageViewListener);
   }

   public    TImageModel getModel() { return getView().getModel(); }
   protected TImageView  getView()  { return _imageView; }

   public TImage getImage() {
      return getView().getImage();
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

   protected void onPropertyViewChanged(Object oldValue, Object newValue, String propertyName) {
      if (IImageView.PROPERTY_IMAGE.equals(propertyName))
         this.onPropertyChanged(oldValue, newValue, PROPERTY_IMAGE);
   }

   @Override
   public void close() {
      _imageView.removeListener(_imageViewListener);
      super.close();
   }

}
