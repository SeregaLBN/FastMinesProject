package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.INotifyPropertyChanged;

/**
 * Image MVC: view (displayed view)
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> model data for display
 **/
public interface IImageView<TImage, TImageModel extends IImageModel> extends AutoCloseable, INotifyPropertyChanged {

   public static final String PROPERTY_SIZE  = IImageModel.PROPERTY_SIZE;
   public static final String PROPERTY_IMAGE = "Image";

   /** model data for display */
   TImageModel getModel();

   /** image size in pixels */
   Size getSize();
   /** image size in pixels */
   void setSize(Size size);

   /** plaform specific view/image/picture or other display context/canvas/window/panel */
   TImage getImage();

   /** Mark the need to redraw the picture
    * Performs a call to the inner draw method (synchronously or asynchronously or implicitly, depending on the implementation) */
   public void invalidate();

   @Override
   void close(); // hide throw Exception

}
