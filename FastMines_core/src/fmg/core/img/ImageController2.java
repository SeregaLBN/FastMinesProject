package fmg.core.img;

import java.util.function.Consumer;

import fmg.common.ui.UiInvoker;

/** Image MVC: controller
 * Default implementation of image controller.
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TView> image view
 * @param <TModel> image model */
public class ImageController2<TImage, TModel extends IImageModel2, TView extends IImageView2<TImage>> implements IImageController2<TImage, TModel> {

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
        var isValidBefore = view.isValid();

        if (ImageHelper.PROPERTY_SIZE.equals(property)) {
            view.reset();
            firePropertyChanged(ImageHelper.PROPERTY_SIZE);
        }

        view.invalidate();

        if (isValidBefore)
            firePropertyChanged(ImageHelper.PROPERTY_IMAGE);
    }

    protected void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            UiInvoker.Deferred.accept(() -> changedCallback.accept(propertyName));
    }

    @Override
    public void close() {
        changedCallback = null;
        model.setListener(null);
        view.reset();
    }

}
