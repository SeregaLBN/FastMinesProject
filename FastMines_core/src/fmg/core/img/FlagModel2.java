package fmg.core.img;

import java.util.function.Consumer;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;

/** Model of the flag image */
public class FlagModel2 implements IImageModel2 {

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

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_SIZE);

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

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
