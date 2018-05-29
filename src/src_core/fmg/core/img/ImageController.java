package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import fmg.common.notyfier.NotifyPropertyChanged;

/**
 * MVC: controller.
 * Base implementation of image controller (manipulations with the image).
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 **/   // TODO refactoring - rename ImageController to DisplayedController
public abstract class ImageController<TImage,
                                      TImageView  extends IImageView<TImage, TImageModel>,
                                      TImageModel extends IImageModel>
                extends NotifyPropertyChanged
{

   public static Consumer<Runnable> DEFERR_INVOKER;

   /** MVC: view */
   private final TImageView _imageView;
   private final PropertyChangeListener _imageViewListener = ev -> onPropertyViewChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
   private Map<String /* propertyName */, Boolean /* sheduled */> _deferrNotifications = new HashMap<>();

   protected ImageController(TImageView imageView) {
      _imageView = imageView;
      _imageView.addListener(_imageViewListener);
   }

   public static final String PROPERTY_IMAGE = IImageView.PROPERTY_IMAGE;
   public static final String PROPERTY_SIZE  = IImageView.PROPERTY_SIZE;

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
      if (!isDeferredNotifications()) {
         super.onPropertyChanged(oldValue, newValue, propertyName);
         return;
      }

      switch (propertyName) {
      default:
         super.onPropertyChanged(oldValue, newValue, propertyName);
         break;

      case PROPERTY_IMAGE:
      case PROPERTY_SIZE:
         if (Boolean.TRUE.equals(_deferrNotifications.get(propertyName)))
            return;
         _deferrNotifications.put(propertyName, true);
         DEFERR_INVOKER.accept(() -> {
            super.onPropertyChanged(oldValue, newValue, propertyName);
            _deferrNotifications.put(propertyName, false);
         });
         break;
      }
   }

   protected void onPropertyViewChanged(Object oldValue, Object newValue, String propertyName) {
      switch (propertyName) {
      case IImageView.PROPERTY_IMAGE:
         this.onPropertyChanged(oldValue, newValue, PROPERTY_IMAGE);
         break;
      case IImageView.PROPERTY_SIZE:
         this.onPropertyChanged(oldValue, newValue, PROPERTY_SIZE);
         break;
      default:
         break;
      }
   }

   @Override
   public void close() {
      _imageView.removeListener(_imageViewListener);
      _imageView.close();
      super.close();
   }

}
