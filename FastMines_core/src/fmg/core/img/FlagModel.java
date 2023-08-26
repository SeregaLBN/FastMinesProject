package fmg.core.img;

import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

import java.util.function.Consumer;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;

/** Model of the flag image */
public class FlagModel implements IImageModel {

    private final SizeDouble size = new SizeDouble(ImageHelper.DEFAULT_IMAGE_SIZE, ImageHelper.DEFAULT_IMAGE_SIZE);
    private final BoundDouble pad = new BoundDouble(ImageHelper.DEFAULT_PADDING);
    private Consumer<String> changedCallback;

    @Override
    public SizeDouble getSize() {
        return size;
    }

    @Override
    public void setSize(SizeDouble size) {
        if (this.size.equals(size))
            return;

        ImageHelper.checkSize(size);

        SizeDouble oldSize = new SizeDouble(this.size);
        this.size.width  = size.width;
        this.size.height = size.height;

        firePropertyChanged(PROPERTY_SIZE);

        setPadding(ImageHelper.recalcPadding(pad, size, oldSize));
    }

    @Override
    public BoundDouble getPadding() {
        return pad;
    }

    @Override
    public void setPadding(BoundDouble padding) {
        if (pad.equals(padding))
            return;

        ImageHelper.checkPadding(size, padding);

        this.pad.left   = padding.left;
        this.pad.right  = padding.right;
        this.pad.top    = padding.top;
        this.pad.bottom = padding.bottom;

        firePropertyChanged(PROPERTY_PADDING);
    }

    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

    private void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

}
