package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

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
    protected final NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged _notifierAsync;
    protected boolean _isDisposed;

    protected ImageView(TImageModel imageModel) {
        this(imageModel, false);
    }
    protected ImageView(TImageModel imageModel, boolean deferredNotifications) {
        _model = imageModel;
        _notifierAsync = deferredNotifications ? new NotifyPropertyChanged(this, true) : null;
        this  .addListener(this::onPropertyChanged);
        _model.addListener(this::onPropertyModelChanged);
    }

    @Override
    public TImageModel getModel() {
        return _model;
    }

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return getModel().getSize(); }
    public void setSize(double widhtAndHeight) { setSize(new SizeDouble(widhtAndHeight, widhtAndHeight)) ; }
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
        _notifier.onPropertyChanged(PROPERTY_IMAGE);
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
        if (_notifierAsync != null)
            _notifierAsync.onPropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    protected void onPropertyModelChanged(PropertyChangeEvent ev) {
        _notifier.onPropertyChanged(null, getModel(), PROPERTY_MODEL);
        if (IImageModel.PROPERTY_SIZE.equals(ev.getPropertyName())) {
            setImage(null);
//            invalidate();
            _notifier.onPropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SIZE);
            _notifier.onPropertyChanged(PROPERTY_IMAGE);
        } else {
            invalidate();
        }
    }

    @Override
    public void close() {
        _isDisposed = true;

        this  .removeListener(this::onPropertyChanged);
        _model.removeListener(this::onPropertyModelChanged);

        _notifier.close();
        if (_notifierAsync != null)
            _notifierAsync.close();

        setImage(null);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        (_notifierAsync != null
            ? _notifierAsync
            : _notifier
        ).addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        (_notifierAsync != null
            ? _notifierAsync
            : _notifier
        ).removeListener(listener);
    }

}
