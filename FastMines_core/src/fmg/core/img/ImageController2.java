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
    protected Consumer<String> changedCallback;

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
        changedCallback = callback;
        view.invalidate();
    }

    protected void onModelChanged(String property) {
        var isValidBefore = view.isValid();
        var callback = changedCallback;

        if (ImageHelper.PROPERTY_SIZE.equals(property)) {
            view.reset();
            if (callback != null)
                UiInvoker.Deferred.accept(() -> callback.accept(ImageHelper.PROPERTY_SIZE));
        }

        view.invalidate();

        if ((callback != null) && isValidBefore)
            UiInvoker.Deferred.accept(() -> callback.accept(ImageHelper.PROPERTY_IMAGE));
    }

    @Override
    public void close() {
        changedCallback = null;
        model.setListener(null);
        view.reset();
    }

}
