package fmg.core.img;

import java.util.function.Consumer;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;

/**
 * Image MVC: model
 * Model of image data/properties/characteristics
 */
public interface IImageModel2 {

    /** width and height of the displayed part in pixels */
    SizeDouble getSize();
    void setSize(SizeDouble size);

    /** inner padding */
    BoundDouble getPadding();
    void setPadding(BoundDouble padding);


    /** set callback. Used by the controller */
    void setListener(Consumer<String /* property name */> callback);

}
