package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/**
 * MVC: view.
 * Base implementation of image view.
 *
 * @param <TImage> plaform specific image
 * @param <TImageModel> model data for display
 **/
public abstract class AImageView<TImage, TImageModel extends IImageModel>
                extends NotifyPropertyChanged
                implements IImageView<TImage, TImageModel>
{

   private final TImageModel _imageModel;
   private final PropertyChangeListener _imageModelListener;

   protected AImageView(TImageModel imageModel) {
      _imageModel = imageModel;
      _imageModelListener = event -> {
         assert event.getSource() == _imageModel; // by reference
         onPropertyModelChanged(event.getOldValue(), event.getNewValue(), event.getPropertyName());
      };
      imageModel.addListener(_imageModelListener);
   }

   @Override
   public TImageModel getModel() {
      return _imageModel;
   }

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }

   /** width and height in pixel */
   @Override
   public Size getSize() { return getModel().getSize(); }
   public void setSize(int widhtAndHeight) { setSize(new Size(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(Size value) { getModel().setSize(value); }

   private enum EInvalidate {
      needRedraw,
      redrawing,
      redrawed
   }
   private EInvalidate _invalidate = EInvalidate.needRedraw;

   protected abstract TImage createImage();
   private TImage _image;
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
      if (setProperty(_image, value, PROPERTY_IMAGE)) {
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
      onPropertyChanged(PROPERTY_IMAGE);
   }

   private void draw() {
      drawBegin();
      drawBody();
      drawEnd();
   }

   protected void drawBegin() { _invalidate = EInvalidate.redrawing; }
   protected abstract void drawBody();
   protected void drawEnd() { _invalidate = EInvalidate.redrawed; }

   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      if (IImageModel.PROPERTY_SIZE.equals(propertyName)) {
         setImage(null);
//         invalidate();
         onPropertyChanged(PROPERTY_IMAGE);
      } else {
         invalidate();
      }
   }

   @Override
   public void close() {
      _imageModel.removeListener(_imageModelListener);
      super.close();
      setImage(null);
   }

}
