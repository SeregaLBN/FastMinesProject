package fmg.core.img;

import static fmg.core.img.PropertyConst.PROPERTY_IMAGE;
import static fmg.core.img.PropertyConst.PROPERTY_MODEL;
import static fmg.core.img.PropertyConst.PROPERTY_MODEL_DOT;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

import java.util.function.Consumer;


/** Image MVC: controller
 * Default implementation of image controller.
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TView> image view
 * @param <TModel> image model */
public class ImageController2<TImage, TView extends IImageView2<TImage>, TModel extends IImageModel2> implements IImageController2<TImage, TModel> {

    protected TModel model;
    protected TView view;
    private Consumer<String> changedCallback;

    protected void init(TModel model, TView view) {
        this.model = model;
        this.view = view;
        model.setListener(this::onModelChanged);
    }

    @Override
    public TModel getModel() {
        return model;
    }

    @Override
    public TImage getImage() {
        return view.getImage();
    }

    @Override
    public void setListener(Consumer<String> callback) {
        if (callback == null) {
            // unset
            changedCallback = null;
        } else {
            // set
            if (changedCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedCallback = callback;
            view.invalidate();
        }
    }

    protected void onModelChanged(String property) {
        if (PROPERTY_SIZE.equals(property))
            view.reset();
        else
            view.invalidate();

        firePropertyChanged(PROPERTY_IMAGE);
        firePropertyChanged(PROPERTY_MODEL);
        firePropertyChanged(PROPERTY_MODEL_DOT + property);
    }

    protected void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

    @Override
    public void close() {
        changedCallback = null;
        model.setListener(null);
        view.reset();
    }

}
