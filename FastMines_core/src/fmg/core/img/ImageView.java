package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

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
    @Property(PROPERTY_MODEL)
    private final TImageModel model;

    @Property(PROPERTY_IMAGE)
    private TImage image;

    private EInvalidate invalidate = EInvalidate.needRedraw;
    protected final NotifyPropertyChanged notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged notifierAsync    = new NotifyPropertyChanged(this, true);
    protected boolean isDisposed;

    private final PropertyChangeListener      onPropertyChangedListener = this::onPropertyChanged;
    private final PropertyChangeListener onModelPropertyChangedListener = this::onModelPropertyChanged;

    protected ImageView(TImageModel imageModel) {
        this.model = imageModel;
        notifier.addListener(onPropertyChangedListener);
        this.model.addListener(onModelPropertyChangedListener);
    }

    @Override
    public TImageModel getModel() {
        return model;
    }

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return getModel().getSize(); }
    @Override
    public void setSize(SizeDouble value) { getModel().setSize(value); }


    protected abstract TImage createImage();
    @Override
    public TImage getImage() {
        if (image == null) {
            setImage(createImage());
            invalidate = EInvalidate.needRedraw;
        }
        if (invalidate == EInvalidate.needRedraw)
            draw();
        return image;
    }
    protected void setImage(TImage value) {
        TImage old = image;
        if (notifier.setProperty(image, value, PROPERTY_IMAGE)) {
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
        if (invalidate == EInvalidate.redrawing)
            return;
        //if (_invalidate == EInvalidate.needRedraw)
        //    return;
        invalidate = EInvalidate.needRedraw;

        // Уведомляю владельца класса что поменялось изображение.
        // Т.е. что нужно вызвать getImage()
        // при котором и отрисуется новое изображение (через вызов draw)
        notifier.firePropertyChanged(PROPERTY_IMAGE);
    }

    private void draw() {
        assert !isDisposed;
        if (isDisposed) {
            Logger.error(" Already disposed! " + this.getClass().getSimpleName());
            return;
        }
        drawBegin();
        drawBody();
        drawEnd();
    }

    protected final void drawBegin() { invalidate = EInvalidate.redrawing; }
    protected abstract void drawBody();
    protected final void drawEnd() { invalidate = EInvalidate.redrawed; }

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
//        Logger.info("  ImageView::onModelPropertyChanged: ev.name=" + ev.getPropertyName());
        notifier.firePropertyChanged(null, getModel(), PROPERTY_MODEL);
        if (IImageModel.PROPERTY_SIZE.equals(ev.getPropertyName())) {
            setImage(null);
//            invalidate();
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SIZE);
            notifier.firePropertyChanged(PROPERTY_IMAGE);
        } else {
            invalidate();
        }
    }

    @Override
    public void close() {
        isDisposed = true;

        notifier.removeListener(onPropertyChangedListener);
        model.removeListener(onModelPropertyChangedListener);

        notifier.close();
        notifierAsync.close();

        setImage(null);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifierAsync.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifierAsync.removeListener(listener);
    }

}
