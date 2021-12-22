package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** Model of the flag image */
public class FlagModel implements IImageModel {

    @Property(PROPERTY_SIZE)
    private SizeDouble size = new SizeDouble(AnimatedImageModel.DEFAULT_IMAGE_SIZE, AnimatedImageModel.DEFAULT_IMAGE_SIZE);

    @Property(PROPERTY_PADDING)
    private BoundDouble padding = new BoundDouble(AnimatedImageModel.DEFAULT_PADDING);

    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return size; }
    @Override
    public void setSize(SizeDouble size) {
        IImageModel.checkSize(size);
        SizeDouble old = this.size;
        if (notifier.setProperty(this.size, size, PROPERTY_SIZE))
            setPadding(IImageModel.recalcPadding(getPadding(), this.size, old));
    }

    @Override
    public BoundDouble getPadding() { return padding; }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        notifier.setProperty(this.padding, new BoundDouble(padding), PROPERTY_PADDING);
    }

    @Override
    public void close() {
        notifier.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }

}
