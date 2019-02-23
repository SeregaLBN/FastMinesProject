package fmg.android.app.model.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.android.app.BR;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.img.IAnimatedModel;
import fmg.core.img.IImageController;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;
import fmg.core.img.ImageController;

/** Base item class for <see cref="MosaicDataItem"/> and <see cref="MosaicGroupDataItem"/> and <see cref="MosaicSkillDataItem"/> */
public abstract class BaseDataItem<T,
                                   TImageModel extends IAnimatedModel,
                                   TImageView  extends IImageView<android.graphics.Bitmap, TImageModel>,
                                   TImageCtrlr extends ImageController<android.graphics.Bitmap, TImageView, TImageModel>>
    extends BaseObservable
    implements INotifyPropertyChanged, AutoCloseable
{

    public static final String PROPERTY_UNIQUE_ID = "UniqueId";
    public static final String PROPERTY_TITLE     = "Title";
    public static final String PROPERTY_ENTITY    = "Entity";
    public static final String PROPERTY_IMAGE     = IImageController.PROPERTY_IMAGE;
    public static final String PROPERTY_SIZE      = IImageModel.PROPERTY_SIZE;
    public static final String PROPERTY_PADDING   = IImageModel.PROPERTY_PADDING;

    private T uniqueId;
    protected TImageCtrlr entity;
    private String title = "";
    protected final NotifyPropertyChanged notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged notifierAsync    = new NotifyPropertyChanged(this, true);

    protected BaseDataItem(T uniqueId) {
        this.uniqueId = uniqueId;
        notifier     .addListener(this::onPropertyChanged);
        notifierAsync.addListener(this::onAsyncPropertyChanged);
    }

    @Bindable
    public T getUniqueId() { return uniqueId; }
    public void setUniqueId(T uniqueId) {
        notifier.setProperty(this.uniqueId, uniqueId, PROPERTY_UNIQUE_ID);
    }

    @Bindable
    public String getTitle() { return title; }
    public void setTitle(String title) {
        notifier.setProperty(this.title, title, PROPERTY_TITLE);
    }

    protected double getZoom() { return 2; }

    @Bindable
    public abstract TImageCtrlr getEntity();
    protected void setEntity(TImageCtrlr entity) {
        TImageCtrlr old = this.entity;
        if (notifier.setProperty(this.entity, entity, PROPERTY_ENTITY)) {
            if (old != null) {
                old           .removeListener(this::onControllerPropertyChanged);
                old.getModel().removeListener(this::onModelPropertyChanged);
                old.close();
            }
            if (entity != null) {
                entity.           addListener(this::onControllerPropertyChanged);
                entity.getModel().addListener(this::onModelPropertyChanged);
            }
        }
    }

    @Bindable
    public Bitmap getImage() { return getEntity().getImage(); }

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

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        // refire as android data binding event
        switch (ev.getPropertyName()) {
        case PROPERTY_UNIQUE_ID: notifyPropertyChanged(BR.uniqueId); break;
        case PROPERTY_TITLE    : notifyPropertyChanged(BR.title   ); break;
        case PROPERTY_ENTITY   : notifyPropertyChanged(BR.entity  ); break;
        case PROPERTY_IMAGE    : notifyPropertyChanged(BR.image   ); break;
        case PROPERTY_SIZE     : notifyPropertyChanged(BR.size    ); break;
        case PROPERTY_PADDING  : notifyPropertyChanged(BR.padding ); break;
        }
    }

    protected void onControllerPropertyChanged(PropertyChangeEvent ev) {
        assert (ev.getSource() == getEntity());
        notifier.firePropertyChanged(PROPERTY_ENTITY);

        switch (ev.getPropertyName()) {
        case IImageController.PROPERTY_IMAGE:
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_IMAGE);
            break;
        }
    }

    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        assert (ev.getSource() == getEntity().getModel());

        switch (ev.getPropertyName()) {
        case IImageModel.PROPERTY_SIZE:
            notifier.firePropertyChanged(zoomSize((SizeDouble)ev.getOldValue()), zoomSize((SizeDouble)ev.getNewValue()), PROPERTY_SIZE);
            break;
        case IImageModel.PROPERTY_PADDING:
            notifier.firePropertyChanged(zoomPadding((BoundDouble)ev.getOldValue()), zoomPadding((BoundDouble)ev.getNewValue()), PROPERTY_PADDING);
            break;
        }
    }

    @Override
    public String toString() { return title; }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifierAsync.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifierAsync.removeListener(listener);
    }

    @Override
    public void close() {
        notifier     .removeListener(this::onPropertyChanged);
        notifierAsync.removeListener(this::onAsyncPropertyChanged);
        notifier.close();
        notifierAsync.close();
        setEntity(null); // call setter
    }

}
