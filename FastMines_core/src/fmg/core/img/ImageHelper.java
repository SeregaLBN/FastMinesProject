package fmg.core.img;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;

public final class ImageHelper {
    private ImageHelper() {}

    public static final Color DEFAULT_BK_COLOR         = Color.DarkOrange(); // Color.Coral(); //
    public static final Color DEFAULT_FOREGROUND_COLOR = Color.LightSeaGreen(); // Color.Orchid(); //
    public static final int   DEFAULT_IMAGE_SIZE = 100;
    public static final int   DEFAULT_PADDING = (int)(DEFAULT_IMAGE_SIZE * 0.05); // 5%

    public static final String PROPERTY_NAME_IMAGE = "Image";
    public static final String PROPERTY_NAME_SIZE  = "Size";
    public static final String PROPERTY_NAME_OTHER = "Other";
    public static final String PROPERTY_NAME_MOSAIC_GROUP = "MosaicGroup";


    /** proportionally adjust when resizing */
    public static BoundDouble recalcPadding(BoundDouble paddingOld, SizeDouble sizeNew, SizeDouble sizeOld) {
        return new BoundDouble(paddingOld.left   * sizeNew.width  / sizeOld.width,
                               paddingOld.top    * sizeNew.height / sizeOld.height,
                               paddingOld.right  * sizeNew.width  / sizeOld.width,
                               paddingOld.bottom * sizeNew.height / sizeOld.height);
    }

    public static void checkSize(SizeDouble size) {
        if (size == null)
            throw new IllegalArgumentException("Size must be defined.");
        if (size.width <= 0)
            throw new IllegalArgumentException("Size.width must be positive.");
        if (size.height <= 0)
            throw new IllegalArgumentException("Size.height must be positive.");
    }

    public static void checkPadding(SizeDouble size, BoundDouble padding) {
        if (padding == null)
            throw new IllegalArgumentException("Padding must be defined.");
        if (padding.getLeftAndRight() >= size.width)
            throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
        if (padding.getTopAndBottom() >= size.height)
            throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
    }

    /** to diapason (0° .. +360°] */
    public static double fixAngle(double value) {
        if (value >= 360)
            return value % 360;
        if (value < 0)
            return (value % 360) + 360;
        return value;
    }

}
