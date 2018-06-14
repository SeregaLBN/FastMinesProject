package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Model of the flag image */
public class FlagModel implements IImageModel {

   private SizeDouble _size;
   protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

   public FlagModel() {
      _size = new SizeDouble(40, 40);
   }

   /** width and height in pixel */
   @Override
   public SizeDouble getSize() { return _size; }
   public void setSize(double widhtAndHeight) { setSize(new SizeDouble(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(SizeDouble size) {
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
