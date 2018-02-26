package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Model of the flag image */
public class FlagModel extends NotifyPropertyChanged implements IImageModel {

   private Size _size;

   public FlagModel() {
      _size = new Size(40, 40);
   }

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }


   /** width and height in pixel */
   @Override
   public Size getSize() { return _size; }
   public void setSize(int widhtAndHeight) { setSize(new Size(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(Size size) {
      setProperty(_size, size, PROPERTY_SIZE);
   }

}
