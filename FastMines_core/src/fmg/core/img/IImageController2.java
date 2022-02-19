package fmg.core.img;

import java.util.function.Consumer;

/** Image MVC: controller
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TModel> image model */
public interface IImageController2<TImage,
                                   TModel extends IImageModel2>
{

    TModel getModel();
    TImage getImage();


    /** set callback */
    void setListener(Consumer<String /* property name */> callback);

}
