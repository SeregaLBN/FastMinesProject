package fmg.android.app.model.items;

import android.graphics.Bitmap;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.Objects;
import java.util.function.Consumer;

import fmg.android.app.BR;
import fmg.common.Logger;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.IImageModel2;
import fmg.core.img.IImageView2;
import fmg.core.img.ImageController2;

import static fmg.core.img.PropertyConst.PROPERTY_IMAGE;
import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

/** Base item class for @see {@link MosaicDataItem} and @see {@link MosaicGroupDataItem"} and @see {@link MosaicSkillDataItem} */
public abstract class BaseDataItem<T,
                                   TImageModel extends IImageModel2,
                                   TImageView  extends IImageView2<Bitmap>,
                                   TImageCtrlr extends ImageController2<Bitmap, TImageView, TImageModel>>
    extends BaseObservable
    implements AutoCloseable
{

    public static final String PROPERTY_UNIQUE_ID = "UniqueId";
    public static final String PROPERTY_TITLE     = "Title";
    public static final String PROPERTY_ENTITY    = "Entity";

    private T uniqueId;
    protected TImageCtrlr entity;
    private String title = "";
    private boolean disposed;

    private Consumer<String> changedCallback;

    protected BaseDataItem(T uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Bindable
    public T getUniqueId() { return uniqueId; }
    public void setUniqueId(T uniqueId) {
        if (Objects.equals(this.uniqueId, uniqueId))
            return;
        this.uniqueId = uniqueId;
        firePropertyChanged(PROPERTY_UNIQUE_ID);
    }

    @Bindable
    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (Objects.equals(this.title, title))
            return;
        this.title = title;
        firePropertyChanged(PROPERTY_TITLE);
    }

    protected double getZoom() { return 1; }

    @Bindable
    public abstract TImageCtrlr getEntity();
    protected void setEntity(TImageCtrlr entity) {
        TImageCtrlr old = this.entity;
        if (Objects.equals(this.entity, entity))
            return;

        this.entity = entity;

        if (old != null) {
            old.setListener(null);
            old.close();
        }
        if (entity != null)
            entity.setListener(this::onControllerPropertyChanged);

        firePropertyChanged(PROPERTY_ENTITY);
    }

    @Bindable
    public Bitmap getImage() {
        if (disposed) {
            Logger.error("Object already disposed! Return faked image...");
            return android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.RGB_565);
        }
        return getEntity().getImage();
    }

    @Bindable
    public SizeDouble getSize() {
        SizeDouble size = getEntity().getModel().getSize();
        double zoom = getZoom();
        return new SizeDouble(size.width / zoom, size.height / zoom);
    }
    public void setSize(SizeDouble size) {
        getEntity().getModel().setSize(zoomSize(size));
    }

    @Bindable
    public BoundDouble getPadding() {
        BoundDouble pad = getEntity().getModel().getPadding();
        double zoom = getZoom();
        return new BoundDouble (pad.left / zoom, pad.top / zoom, pad.right / zoom, pad.bottom / zoom);
    }
    public void setPadding(BoundDouble pad) {
        getEntity().getModel().setPadding(zoomPadding(pad));
    }

    SizeDouble zoomSize(SizeDouble size) {
        if (size == null)
            return null;
        double zoom = getZoom();
        return new SizeDouble(size.width * zoom, size.height * zoom);
    }

    BoundDouble zoomPadding(BoundDouble pad) {
        if (pad == null)
            return null;
        double zoom = getZoom();
        return new BoundDouble(pad.left * zoom, pad.top * zoom, pad.right * zoom, pad.bottom * zoom);
    }

    private void onControllerPropertyChanged(String propertyName) {
        if (!disposed)
            UiInvoker.Deferred.accept(() -> firePropertyChanged(propertyName));
    }

    protected void firePropertyChanged(String propertyName) {
        if (disposed)
            return;

        if (changedCallback != null) {
            changedCallback.accept(PROPERTY_ENTITY);

            switch (propertyName) {
            case PROPERTY_IMAGE:
            case PROPERTY_SIZE:
            case PROPERTY_PADDING:
                changedCallback.accept(propertyName);
                break;
            }
        }
        // refire as async event
        UiInvoker.Deferred.accept(() -> firePropertyChangedAsync(propertyName));
    }

    protected void firePropertyChangedAsync(String propertyName) {
        if (disposed)
            return;

        // refire as android data binding event
        switch (propertyName) {
        case PROPERTY_UNIQUE_ID: notifyPropertyChanged(BR.uniqueId); break;
        case PROPERTY_TITLE    : notifyPropertyChanged(BR.title   ); break;
        case PROPERTY_ENTITY   : notifyPropertyChanged(BR.entity  ); break;
        case PROPERTY_IMAGE    : notifyPropertyChanged(BR.image   ); break;
        case PROPERTY_SIZE     : notifyPropertyChanged(BR.size    ); break;
        case PROPERTY_PADDING  : notifyPropertyChanged(BR.padding ); break;
        }
    }

    @Override
    public String toString() { return title; }

    public void setListener(Consumer<String> callback) {
        if (callback == null) {
            // unset
            changedCallback = null;
        } else {
            // set
            if (changedCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedCallback = callback;
        }
    }

    public boolean isDisposed() { return disposed; }

    @Override
    public void close() {
        disposed = true;
        changedCallback = null;
        setEntity(null);
    }

}
