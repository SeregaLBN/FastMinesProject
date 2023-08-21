package fmg.android.app.model.items;

import androidx.databinding.Bindable;

import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.BR;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicGroupImg2;
import fmg.common.geom.BoundDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.MosaicGroupModel2;
import fmg.core.types.EMosaicGroup;

import static fmg.core.img.PropertyConst.PROPERTY_BURGER;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_GROUP;
import static fmg.core.img.PropertyConst.PROPERTY_PADDING;

/** Mosaic group item for data model */
public class MosaicGroupDataItem extends BaseDataItem<EMosaicGroup, MosaicGroupModel2, AndroidBitmapView<MosaicGroupModel2>, MosaicGroupImg2.MosaicGroupAndroidBitmapController> {

    static final String PROPERTY_BURGER_DOT = PROPERTY_BURGER + ".";
    static final String PROPERTY_PADDING_BURGER = PROPERTY_BURGER_DOT + PROPERTY_PADDING;

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
    public MosaicGroupImg2.MosaicGroupAndroidBitmapController getEntity() {
        if (this.entity == null) {
            var tmp = new MosaicGroupImg2.MosaicGroupAndroidBitmapController(getUniqueId());
            var m = tmp.getModel();
            m.setBorderWidth(3);
            m.setRotateAngle(ThreadLocalRandom.current().nextInt(90));
            tmp.getBurgerModel().setListener(this::onBurgerModelPropertyChanged);
            setEntity(tmp);
        }
        return this.entity;
    }

    @Bindable
    public BoundDouble getPaddingBurgerMenu() {
        BoundDouble pad = getEntity().getBurgerModel().getPadding();
        double zoom = getZoom();
        return new BoundDouble(pad.left / zoom, pad.top / zoom, pad.right / zoom, pad.bottom / zoom);
    }

    public void setPaddingBurgerMenu(BoundDouble paddingBurgerMenu) {
        getEntity().getBurgerModel().setPadding(zoomPadding(paddingBurgerMenu));
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
            getEntity().getModel().setMosaicGroup(getMosaicGroup());
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
        case PROPERTY_MOSAIC_GROUP  : notifyPropertyChanged(BR.mosaicGroup      ); break;
        case PROPERTY_PADDING_BURGER: notifyPropertyChanged(BR.paddingBurgerMenu); break;
        }
    }

    private void onBurgerModelPropertyChanged(String propertyName) {
        if (!isDisposed())
            UiInvoker.Deferred.accept(() -> firePropertyChanged(PROPERTY_BURGER_DOT + propertyName));
    }

    @Override
    public void close() {
        getEntity().getBurgerModel().setListener(null);
        super.close();
    }

}
