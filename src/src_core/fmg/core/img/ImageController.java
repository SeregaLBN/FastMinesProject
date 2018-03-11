package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import fmg.common.notyfier.NotifyPropertyChanged;

/**
 * MVC: controller.
 * Base implementation of image controller (manipulations with the image).
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 **/
public abstract class ImageController<TImage,
                                      TImageView  extends IImageView<TImage, TImageModel>,
                                      TImageModel extends IImageModel>
                extends NotifyPropertyChanged
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

   private boolean _deferredImageNotifications = true;
   public boolean isDeferredImageNotifications() { return _deferredImageNotifications; }
   public void setDeferredImageNotifications(boolean value) { _deferredImageNotifications = value; }

   /** Deferr notifications */
   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if (!PROPERTY_IMAGE.equals(propertyName) || !isDeferredImageNotifications()) {
         super.onPropertyChanged(oldValue, newValue, propertyName);
         return;
      }
      if (_sheduled)
         return;
      _sheduled = true;
      DEFERR_INVOKER.accept(() -> {
         super.onPropertyChanged(oldValue, newValue, propertyName);
         _sheduled = false;
      });
   }
   private boolean _sheduled;

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
