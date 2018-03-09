package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.INotifyPropertyChanged;

/**
 * Image MVC: view
 *
 * @param <TImage> plaform specific image or picture or other display context/canvas/window/panel
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

   /** plaform specific image or picture or other display context/canvas/window/panel */
   TImage getImage();

   /** mark the need to redraw the picture */
   public void invalidate();

   @Override
   void close(); // hide throw Exception

}
