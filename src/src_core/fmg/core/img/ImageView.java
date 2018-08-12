package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

/**
 * MVC: view.
 * Base implementation of image view.
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> model data for display
 **/
public abstract class ImageView<TImage, TImageModel extends IImageModel>
                implements IImageView<TImage, TImageModel>
{
   private enum EInvalidate {
      needRedraw,
      redrawing,
      redrawed
   }

   /** MVC: model */
   private final TImageModel _model;
   private TImage _image;
   private EInvalidate _invalidate = EInvalidate.needRedraw;
   private final PropertyChangeListener _selfListener  = ev -> onPropertyChanged(     ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
   private final PropertyChangeListener _modelListener = ev -> onPropertyModelChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
   protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);
   protected boolean _isDisposed;

   protected ImageView(TImageModel imageModel) {
      _model = imageModel;
      this.addListener(_selfListener);
      _model.addListener(_modelListener);
   }

   @Override
   public TImageModel getModel() {
      return _model;
   }

   /** width and height in pixel */
   @Override
   public SizeDouble getSize() { return getModel().getSize(); }
   public void setSize(double widhtAndHeight) { setSize(new SizeDouble(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(SizeDouble value) { getModel().setSize(value); }


   protected abstract TImage createImage();
   @Override
   public TImage getImage() {
      if (_image == null) {
         setImage(createImage());
         _invalidate = EInvalidate.needRedraw;
      }
      if (_invalidate == EInvalidate.needRedraw)
         draw();
      return _image;
   }
   protected void setImage(TImage value) {
      TImage old = _image;
      if (_notifier.setProperty(_image, value, PROPERTY_IMAGE)) {
         if (old instanceof AutoCloseable)
            try {
               ((AutoCloseable)old).close();
            } catch (Exception ex) {
               ex.printStackTrace();
            }
      }
   }

   @Override
   public void invalidate() {
      if (_invalidate == EInvalidate.redrawing)
         return;
      //if (_invalidate == EInvalidate.needRedraw)
      //   return;
      _invalidate = EInvalidate.needRedraw;

      // Уведомляю владельца класса что поменялось изображение.
      // Т.е. что нужно вызвать getImage()
      // при котором и отрисуется новое изображение (через вызов draw)
      _notifier.onPropertyChanged(PROPERTY_IMAGE);
   }

   private void draw() {
      if (_isDisposed)
         System.err.println(" Already disposed! " + this.getClass().getSimpleName());
      assert !_isDisposed;
      drawBegin();
      drawBody();
      drawEnd();
   }

   protected final void drawBegin() { _invalidate = EInvalidate.redrawing; }
   protected abstract void drawBody();
   protected final void drawEnd() { _invalidate = EInvalidate.redrawed; }

   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) { }

   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      _notifier.onPropertyChanged(null, getModel(), PROPERTY_MODEL);
      if (IImageModel.PROPERTY_SIZE.equals(propertyName)) {
         setImage(null);
//         invalidate();
         _notifier.onPropertyChanged(oldValue, newValue, PROPERTY_SIZE);
         _notifier.onPropertyChanged(PROPERTY_IMAGE);
      } else {
         invalidate();
      }
   }

   @Override
   public void close() {
      _isDisposed = true;
      this.removeListener(_selfListener);
      _model.removeListener(_modelListener);
      _notifier.close();
      setImage(null);
   }

   @Override
   public void addListener(PropertyChangeListener listener) {
      _notifier.addListener(listener);
   }
   @Override
   public void removeListener(PropertyChangeListener listener) {
      _notifier.removeListener(listener);
   }

}
