package fmg.core.img;

import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;

/**
 * Image MVC: model
 * Model of image data/properties/characteristics
 */
public interface IImageModel extends INotifyPropertyChanged, AutoCloseable {

    public static final String PROPERTY_SIZE = "Size";

    /** width and height of the displayed part in pixels */
    SizeDouble getSize();
    void setSize(SizeDouble value);

    @Override
    void close(); // hide throws Exception

}
