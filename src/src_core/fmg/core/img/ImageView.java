package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;

/**
 * MVC: view.
 * Base implementation of image view.
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> model data for display
 **/
public abstract class ImageView<TImage, TImageModel extends IImageModel>
          implements IImageView<TImage, TImageModel>
{
    private enum EInvalidate {
        needRedraw,
        redrawing,
        redrawed
    }

    /** MVC: model */
    private final TImageModel _model;
    private TImage _image;
    private EInvalidate _invalidate = EInvalidate.needRedraw;
    protected final NotifyPropertyChanged _notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged _notifierAsync    = new NotifyPropertyChanged(this, true);
    protected boolean _isDisposed;

    private final PropertyChangeListener      onPropertyChangedListener = this::onPropertyChanged;
    private final PropertyChangeListener onPropertyModelChangedListener = this::onPropertyModelChanged;

    protected ImageView(TImageModel imageModel) {
        _model = imageModel;
        _notifier.addListener(onPropertyChangedListener);
        _model.addListener(onPropertyModelChangedListener);
    }

    @Override
    public TImageModel getModel() {
        return _model;
    }

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return getModel().getSize(); }
    @Override
    public void setSize(SizeDouble value) { getModel().setSize(value); }


    protected abstract TImage createImage();
    @Override
    public TImage getImage() {
        if (_image == null) {
            setImage(createImage());
            _invalidate = EInvalidate.needRedraw;
        }
        if (_invalidate == EInvalidate.needRedraw)
            draw();
        return _image;
    }
    protected void setImage(TImage value) {
        TImage old = _image;
        if (_notifier.setProperty(_image, value, PROPERTY_IMAGE)) {
            if (old instanceof AutoCloseable)
                try {
                    ((AutoCloseable)old).close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
    }

    @Override
    public void invalidate() {
        if (_invalidate == EInvalidate.redrawing)
            return;
        //if (_invalidate == EInvalidate.needRedraw)
        //    return;
        _invalidate = EInvalidate.needRedraw;

        // Уведомляю владельца класса что поменялось изображение.
        // Т.е. что нужно вызвать getImage()
        // при котором и отрисуется новое изображение (через вызов draw)
        _notifier.firePropertyChanged(PROPERTY_IMAGE);
    }

    private void draw() {
        assert !_isDisposed;
        if (_isDisposed) {
            System.err.println(" Already disposed! " + this.getClass().getSimpleName());
            return;
        }
        drawBegin();
        drawBody();
        drawEnd();
    }

    protected final void drawBegin() { _invalidate = EInvalidate.redrawing; }
    protected abstract void drawBody();
    protected final void drawEnd() { _invalidate = EInvalidate.redrawed; }

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        _notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    protected void onPropertyModelChanged(PropertyChangeEvent ev) {
//        Logger.info("  ImageView::onPropertyModelChanged: ev.name=" + ev.getPropertyName());
        _notifier.firePropertyChanged(null, getModel(), PROPERTY_MODEL);
        if (IImageModel.PROPERTY_SIZE.equals(ev.getPropertyName())) {
            setImage(null);
//            invalidate();
            _notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SIZE);
            _notifier.firePropertyChanged(PROPERTY_IMAGE);
        } else {
            invalidate();
        }
    }

    @Override
    public void close() {
        _isDisposed = true;

        _notifier.removeListener(onPropertyChangedListener);
        _model.removeListener(onPropertyModelChangedListener);

        _notifier.close();
        _notifierAsync.close();

        setImage(null);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _notifierAsync.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        _notifierAsync.removeListener(listener);
    }

}
