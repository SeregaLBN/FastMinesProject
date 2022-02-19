package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;

/**
 * Image MVC: controller
 * Base implementation of image controller (manipulations with the image).
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 */
@Deprecated
public abstract class ImageController<TImage,
                                      TImageView  extends IImageView<TImage, TImageModel>,
                                      TImageModel extends IImageModel>
          implements IImageController<TImage, TImageView, TImageModel>
{

    /** MVC: view */
    private final TImageView imageView;
    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this, true);
    private final PropertyChangeListener onViewPropertyChangedListener = this::onViewPropertyChanged;

    protected ImageController(TImageView imageView) {
        this.imageView = imageView;
        this.imageView.addListener(onViewPropertyChangedListener);
    }

    protected TImageView  getView()  { return imageView; }
    @Override
    public    TImageModel getModel() { return getView().getModel(); }
    @Override
    public    TImage      getImage() { return getView().getImage(); }
    @Override
    public    SizeDouble  getSize()  { return getView().getSize(); }

    protected void onViewPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case IImageView.PROPERTY_MODEL:
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_MODEL);
            break;
        case IImageView.PROPERTY_IMAGE:
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_IMAGE);
            break;
        case IImageView.PROPERTY_SIZE:
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SIZE);
            break;
        default:
            break;
        }
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }

    @Override
    public void close() {
        imageView.removeListener(onViewPropertyChangedListener);
        notifier.close();
    }

}
