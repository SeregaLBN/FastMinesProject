package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.INotifyPropertyChanged;

/** MVC: model of image data/properties/characteristics */
public interface IImageModel extends INotifyPropertyChanged {
   // TODO refactoring - rename ImageModel to DisplayedModel

   public static final String PROPERTY_SIZE = "Size";

   /** width and height of the displayed part in pixels */
   Size getSize();
   void setSize(Size value);

}
