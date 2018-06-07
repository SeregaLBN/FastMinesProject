package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Model of the flag image */
public class FlagModel implements IImageModel {

   private Size _size;
   protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);


   public FlagModel() {
      _size = new Size(40, 40);
   }

   /** width and height in pixel */
   @Override
   public Size getSize() { return _size; }
   public void setSize(int widhtAndHeight) { setSize(new Size(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(Size size) {
      _notifier.setProperty(_size, size, PROPERTY_SIZE);
   }

   @Override
   public void close() {
      _notifier.close();
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
