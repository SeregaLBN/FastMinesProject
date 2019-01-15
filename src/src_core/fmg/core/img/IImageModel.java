package fmg.core.img;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;

/**
 * Image MVC: model
 * Model of image data/properties/characteristics
 */
public interface IImageModel extends INotifyPropertyChanged, AutoCloseable {

    public static final String PROPERTY_SIZE    = "Size";
    public static final String PROPERTY_PADDING = "Padding";

    /** width and height of the displayed part in pixels */
    SizeDouble getSize();
    void setSize(SizeDouble size);

    /** inner padding */
    BoundDouble getPadding();
    void setPadding(BoundDouble padding);

    @Override
    void close(); // hide throws Exception


    /** proportionally adjust when resizing */
    public static BoundDouble recalcPadding(BoundDouble paddingOld, SizeDouble sizeNew, SizeDouble sizeOld) {
        return new BoundDouble(paddingOld.left   * sizeNew.width  / sizeOld.width,
                               paddingOld.top    * sizeNew.height / sizeOld.height,
                               paddingOld.right  * sizeNew.width  / sizeOld.width,
                               paddingOld.bottom * sizeNew.height / sizeOld.height);
    }

    static void checkSize(SizeDouble size) {
        if (size == null)
            throw new IllegalArgumentException("Size must be defined.");
        if (size.width <= 0)
            throw new IllegalArgumentException("Size.width must be positive.");
        if (size.height <= 0)
            throw new IllegalArgumentException("Size.height must be positive.");
    }

    static void checkPadding(IImageModel self, BoundDouble padding) {
        if (padding == null)
            throw new IllegalArgumentException("Padding must be defined.");
        SizeDouble size = self.getSize();
        if (padding.getLeftAndRight() >= size.width)
            throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
        if (padding.getTopAndBottom() >= size.height)
            throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
    }

}
