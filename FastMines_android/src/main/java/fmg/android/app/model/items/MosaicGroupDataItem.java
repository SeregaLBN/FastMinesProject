package fmg.android.app.model.items;

import androidx.databinding.Bindable;

import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.BR;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicGroupImg;
import fmg.common.geom.BoundDouble;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

import static fmg.core.img.PropertyConst.PROPERTY_MODEL_BURGER_MENU_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_GROUP;

/** Mosaic group item for data model */
public class MosaicGroupDataItem extends BaseDataItem<EMosaicGroup, MosaicGroupModel, AndroidBitmapView<MosaicGroupModel>, MosaicGroupImg.MosaicGroupAndroidBitmapController> {

    public MosaicGroupDataItem(EMosaicGroup eMosaicGroup) {
        super(eMosaicGroup);
        setTitle(eMosaicGroup==null ? null : eMosaicGroup.getDescription());
    }

    @Bindable
    public EMosaicGroup getMosaicGroup()                          { return getUniqueId(); }
    public void         setMosaicGroup(EMosaicGroup eMosaicGroup) {        setUniqueId(eMosaicGroup); }

    @Deprecated
    public String getUnicodeChar() { return getMosaicGroup() == null ? null : Character.toString(getMosaicGroup().unicodeChar(false)); }

    @Override
    public MosaicGroupImg.MosaicGroupAndroidBitmapController getController() {
        if (this.controller == null) {
            var ctrl = new MosaicGroupImg.MosaicGroupAndroidBitmapController(getUniqueId());
            var m = ctrl.getModel();
            m.setBorderWidth(3);
            m.setRotateAngle(ThreadLocalRandom.current().nextInt(90));
            setController(ctrl);
        }
        return this.controller;
    }

    @Bindable
    public BoundDouble getPaddingBurgerMenu() {
        BoundDouble pad = getController().getBurgerMenuModel().getPadding();
        double zoom = getZoom();
        return new BoundDouble(pad.left / zoom, pad.top / zoom, pad.right / zoom, pad.bottom / zoom);
    }

    public void setPaddingBurgerMenu(BoundDouble paddingBurgerMenu) {
        getController().getBurgerMenuModel().setPadding(zoomPadding(paddingBurgerMenu));
    }

    @Bindable
    public String getContentDescription() {
        return "MosaicGroupItem:" + getMosaicGroup().getDescription();
    }

    @Override
    protected void firePropertyChanged(String propertyName) {
        if (isDisposed())
            return;

        super.firePropertyChanged(propertyName);

        switch (propertyName) {
        case PROPERTY_UNIQUE_ID:
            getController().getModel().setMosaicGroup(getMosaicGroup());
            break;
        case PROPERTY_MOSAIC_GROUP:
            super.firePropertyChanged(PROPERTY_UNIQUE_ID);
            break;
        }
    }

    @Override
    protected void firePropertyChangedAsync(String propertyName) {
        if (isDisposed())
            return;

        super.firePropertyChangedAsync(propertyName);

        // refire as android data binding event
        switch (propertyName) {
        case PROPERTY_MOSAIC_GROUP             : notifyPropertyChanged(BR.mosaicGroup      ); break;
        case PROPERTY_MODEL_BURGER_MENU_PADDING: notifyPropertyChanged(BR.paddingBurgerMenu); break;
        }
    }

    @Override
    public void close() {
        getController().getBurgerMenuModel().setListener(null);
        super.close();
    }

}
