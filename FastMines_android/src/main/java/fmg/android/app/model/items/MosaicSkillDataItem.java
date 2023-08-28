package fmg.android.app.model.items;

import androidx.databinding.Bindable;

import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.BR;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicSkillImg;
import fmg.common.geom.BoundDouble;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

import static fmg.core.img.PropertyConst.PROPERTY_MODEL_BURGER_MENU_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_SKILL_LEVEL;

/** Mosaic skill level item for data model */
public class MosaicSkillDataItem extends BaseDataItem<ESkillLevel, MosaicSkillModel, AndroidBitmapView<MosaicSkillModel>, MosaicSkillImg.MosaicSkillAndroidBitmapController> {

    public MosaicSkillDataItem(ESkillLevel eSkill) {
        super(eSkill);
        setTitle(eSkill==null ? null : eSkill.getDescription());
    }

    @Bindable
    public ESkillLevel getSkillLevel()                   { return getUniqueId(); }
    public void        setSkillLevel(ESkillLevel eSkill) {        setUniqueId(eSkill); }

    @Deprecated
    public String getUnicodeChar() { return getSkillLevel() == null ? null : Character.toString(getSkillLevel().unicodeChar()); }

    @Override
    public MosaicSkillImg.MosaicSkillAndroidBitmapController getController() {
        if (this.controller == null) {
            var ctrl = new MosaicSkillImg.MosaicSkillAndroidBitmapController(getSkillLevel());
            var m = ctrl.getModel();
            m.setBorderWidth(2);
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
        return "MosaicSkillItem:" + getSkillLevel().getDescription();
    }

    @Override
    protected void firePropertyChanged(String propertyName) {
        if (isDisposed())
            return;

        super.firePropertyChanged(propertyName);

        switch (propertyName) {
        case PROPERTY_UNIQUE_ID:
            getController().getModel().setMosaicSkill(getSkillLevel());
            break;
        case PROPERTY_SKILL_LEVEL:
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
        case PROPERTY_SKILL_LEVEL              : notifyPropertyChanged(BR.skillLevel       ); break;
        case PROPERTY_MODEL_BURGER_MENU_PADDING: notifyPropertyChanged(BR.paddingBurgerMenu); break;
        }
    }

    @Override
    public void close() {
        getController().getBurgerMenuModel().setListener(null);
        super.close();
    }

}
