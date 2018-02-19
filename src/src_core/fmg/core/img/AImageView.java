package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/**
 * MVC: view.
 * Base implementation of image view.
 *
 * @param <TImage> plaform specific image
 **/
public abstract class AImageView<TImage> extends NotifyPropertyChanged implements IImageView<TImage> {

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }

   private Size _size;
   /** width and height in pixel */
   @Override
   public Size getSize() { return _size; }
   public void setSize(int widhtAndHeight) { setSize(new Size(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(Size value) {
      if (setProperty(_size, value, PROPERTY_SIZE)) {
         setImage(null);
         //invalidate();
      }
   }

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

   @Override
   public void close() {
      super.close();
      setImage(null);
   }

}
