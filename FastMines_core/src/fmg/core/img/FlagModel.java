package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;

/** Model of the flag image */
public class FlagModel implements IImageModel {

    private SizeDouble _size = new SizeDouble(AnimatedImageModel.DefaultImageSize, AnimatedImageModel.DefaultImageSize);
    private BoundDouble _padding = new BoundDouble(AnimatedImageModel.DefaultPadding);
    protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return _size; }
    @Override
    public void setSize(SizeDouble size) {
        IImageModel.checkSize(size);
        SizeDouble old = _size;
        if (_notifier.setProperty(_size, size, PROPERTY_SIZE))
            setPadding(IImageModel.recalcPadding(getPadding(), _size, old));
    }

    @Override
    public BoundDouble getPadding() { return _padding; }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        _notifier.setProperty(this._padding, new BoundDouble(padding), PROPERTY_PADDING);
    }

    @Override
    public void close() {
        _notifier.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        _notifier.removeListener(listener);
    }

}
