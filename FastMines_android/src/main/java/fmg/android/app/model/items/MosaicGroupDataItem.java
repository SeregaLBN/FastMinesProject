package fmg.android.app.model.items;

import android.databinding.Bindable;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.BR;
import fmg.android.img.MosaicGroupImg;
import fmg.common.geom.BoundDouble;
import fmg.core.img.BurgerMenuModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

/** Mosaic group item for data model */
public class MosaicGroupDataItem extends BaseDataItem<EMosaicGroup, MosaicGroupModel, MosaicGroupImg.Bitmap, MosaicGroupImg.ControllerBitmap> {

    public static final String PROPERTY_MOSAIC_GROUP   = "MosaicGroup";
    public static final String PROPERTY_PADDING_BURGER = "PaddingBurgerMenu";

    public MosaicGroupDataItem(EMosaicGroup eMosaicGroup) {
        super(eMosaicGroup);
        setTitle(eMosaicGroup==null ? null : eMosaicGroup.getDescription());
    }

    @Bindable
    public EMosaicGroup getMosaicGroup()                          { return getUniqueId(); }
    public void         setMosaicGroup(EMosaicGroup eMosaicGroup) {        setUniqueId(eMosaicGroup); }

    @Override
    public MosaicGroupImg.ControllerBitmap getEntity() {
        if (this.entity == null) {
            MosaicGroupImg.ControllerBitmap tmp = new MosaicGroupImg.ControllerBitmap(getMosaicGroup());
            MosaicGroupModel m = tmp.getModel();
            m.setBorderWidth(3);
            m.setRotateAngle(ThreadLocalRandom.current().nextInt(90));
            tmp.getBurgerMenuModel().addListener(this::onBurgerMenuModelPropertyChanged);
            setEntity(tmp);
        }
        return this.entity;
    }

    @Bindable
    public BoundDouble getPaddingBurgerMenu() {
        BoundDouble pad = getEntity().getBurgerMenuModel().getPadding();
        double zoom = getZoom();
        return new BoundDouble(pad.left / zoom, pad.top / zoom, pad.right / zoom, pad.bottom / zoom);
    }
    public void setPaddingBurgerMenu(BoundDouble paddingBurgerMenu) {
        getEntity().getBurgerMenuModel().setPadding(zoomPadding(paddingBurgerMenu));
    }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);

        switch (ev.getPropertyName()) {
        case PROPERTY_UNIQUE_ID:
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_MOSAIC_GROUP); // recall with another property name
            break;
        }
    }

    @Override
    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        super.onAsyncPropertyChanged(ev);

        // refire as android data binding event
        switch (ev.getPropertyName()) {
        case PROPERTY_MOSAIC_GROUP  : notifyPropertyChanged(BR.mosaicGroup      ); break;
        case PROPERTY_PADDING_BURGER: notifyPropertyChanged(BR.paddingBurgerMenu); break;
        }
    }

    protected void onBurgerMenuModelPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case BurgerMenuModel.PROPERTY_PADDING:
            notifier.firePropertyChanged(zoomPadding((BoundDouble)ev.getOldValue()), zoomPadding((BoundDouble)ev.getNewValue()), PROPERTY_PADDING_BURGER);
            break;
        }
    }

    @Override
    public void close() {
        getEntity().getBurgerMenuModel().removeListener(this::onBurgerMenuModelPropertyChanged);
        super.close();
    }

}
