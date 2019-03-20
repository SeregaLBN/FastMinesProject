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
public abstract class ImageController<TImage,
                                      TImageView  extends IImageView<TImage, TImageModel>,
                                      TImageModel extends IImageModel>
          implements IImageController<TImage, TImageView, TImageModel>
{

    /** MVC: view */
    private final TImageView _imageView;
    protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this, true);

    protected ImageController(TImageView imageView) {
        _imageView = imageView;
        _imageView.addListener(this::onPropertyViewChanged);
    }

    protected TImageView  getView()  { return _imageView; }
    @Override
    public    TImageModel getModel() { return getView().getModel(); }
    @Override
    public    TImage      getImage() { return getView().getImage(); }
    @Override
    public    SizeDouble  getSize()  { return getView().getSize(); }

    protected void onPropertyViewChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case IImageView.PROPERTY_MODEL:
            _notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_MODEL);
            break;
        case IImageView.PROPERTY_IMAGE:
            _notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_IMAGE);
            break;
        case IImageView.PROPERTY_SIZE:
            _notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SIZE);
            break;
        default:
            break;
        }
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        _notifier.removeListener(listener);
    }

    @Override
    public void close() {
        _imageView.removeListener(this::onPropertyViewChanged);
        _notifier.close();
    }

}
