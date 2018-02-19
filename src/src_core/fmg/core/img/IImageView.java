package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.INotifyPropertyChanged;

/**
 * Image MVC: view
 *
 * @param <TImage> plaform specific image
 **/
public interface IImageView<TImage> extends AutoCloseable, INotifyPropertyChanged {

   public static final String PROPERTY_SIZE  = IImageModel.PROPERTY_SIZE;
   public static final String PROPERTY_IMAGE = "Image";

   /** image size in pixels */
   Size getSize();
   /** image size in pixels */
   void setSize(Size size);

   /** plaform specific image */
   TImage getImage();

   /** mark the need to redraw the picture */
   public void invalidate();

   @Override
   void close(); // hide throw Exception

}
