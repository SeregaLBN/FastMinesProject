package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

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
    private final PropertyChangeListener _imageViewListener = ev -> onPropertyViewChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this, true);

    protected ImageController(TImageView imageView) {
        _imageView = imageView;
        _imageView.addListener(_imageViewListener);
    }

    protected TImageView  getView()  { return _imageView; }
    @Override
    public    TImageModel getModel() { return getView().getModel(); }
    @Override
    public    TImage      getImage() { return getView().getImage(); }
    @Override
    public    SizeDouble  getSize()  { return getView().getSize(); }

    protected void onPropertyViewChanged(Object oldValue, Object newValue, String propertyName) {
        switch (propertyName) {
        case IImageView.PROPERTY_MODEL:
            _notifier.onPropertyChanged(oldValue, newValue, PROPERTY_MODEL);
            break;
        case IImageView.PROPERTY_IMAGE:
            _notifier.onPropertyChanged(oldValue, newValue, PROPERTY_IMAGE);
            break;
        case IImageView.PROPERTY_SIZE:
            _notifier.onPropertyChanged(oldValue, newValue, PROPERTY_SIZE);
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
        _imageView.removeListener(_imageViewListener);
        _notifier.close();
    }

}
