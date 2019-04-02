package fmg.android.app.model.items;

import android.databinding.Bindable;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.BR;
import fmg.android.img.MosaicSkillImg;
import fmg.common.geom.BoundDouble;
import fmg.core.img.BurgerMenuModel;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

/** Mosaic skill level item for data model */
public class MosaicSkillDataItem extends BaseDataItem<ESkillLevel, MosaicSkillModel, MosaicSkillImg.BitmapView, MosaicSkillImg.BitmapController> {

    public static final String PROPERTY_SKILL_LEVEL    = "SkillLevel";
    public static final String PROPERTY_PADDING_BURGER = "PaddingBurgerMenu";

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
    public MosaicSkillImg.BitmapController getEntity() {
        if (this.entity == null) {
            MosaicSkillImg.BitmapController tmp = new MosaicSkillImg.BitmapController(getSkillLevel());
            MosaicSkillModel m = tmp.getModel();
            m.setBorderWidth(2);
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
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SKILL_LEVEL); // recall with another property name
            break;
        }
    }

    @Override
    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        super.onAsyncPropertyChanged(ev);

        // refire as android data binding event
        switch (ev.getPropertyName()) {
        case PROPERTY_SKILL_LEVEL   : notifyPropertyChanged(BR.skillLevel       ); break;
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
