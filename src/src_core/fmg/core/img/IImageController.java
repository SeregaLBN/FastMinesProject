package fmg.core.img;

import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;

/**
 * Image MVC: controller
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 */
public interface IImageController<TImage,
                                  TImageView  extends IImageView<TImage, TImageModel>,
                                  TImageModel extends IImageModel>
                 extends INotifyPropertyChanged, AutoCloseable
{

   public static final String PROPERTY_MODEL = IImageView.PROPERTY_MODEL;
   public static final String PROPERTY_IMAGE = IImageView.PROPERTY_IMAGE;
   public static final String PROPERTY_SIZE  = IImageView.PROPERTY_SIZE;

   TImageModel getModel();
   TImage      getImage();
   SizeDouble  getSize();

   @Override
   void close(); // hide throws Exception

}
