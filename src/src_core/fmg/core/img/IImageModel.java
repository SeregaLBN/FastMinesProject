package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.INotifyPropertyChanged;

/** MVC: model of image data/properties/characteristics */
public interface IImageModel extends INotifyPropertyChanged {

   public static final String PROPERTY_SIZE = "Size";

   /** width and height in pixel */
   Size getSize();
   void setSize(Size value);

}
