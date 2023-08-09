package fmg.android.app.model.items;

import androidx.databinding.Bindable;

import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.BR;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicSkillImg2;
import fmg.common.geom.BoundDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.MosaicSkillModel2;
import fmg.core.types.ESkillLevel;

import static fmg.core.img.PropertyConst.PROPERTY_SKILL_LEVEL;

/** Mosaic skill level item for data model */
public class MosaicSkillDataItem extends BaseDataItem<ESkillLevel, MosaicSkillModel2, AndroidBitmapView<MosaicSkillModel2>, MosaicSkillImg2.MosaicSkillAndroidBitmapController> {

    private static final String PROPERTY_BURGER_SUFFIX = MosaicGroupDataItem.PROPERTY_BURGER_SUFFIX;
    private static final String PROPERTY_PADDING_BURGER = MosaicGroupDataItem.PROPERTY_PADDING_BURGER;

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
    public MosaicSkillImg2.MosaicSkillAndroidBitmapController getEntity() {
        if (this.entity == null) {
            var tmp = new MosaicSkillImg2.MosaicSkillAndroidBitmapController(getSkillLevel());
            var m = tmp.getModel();
            m.setBorderWidth(2);
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
        return "MosaicSkillItem:" + getSkillLevel().getDescription();
    }

    @Override
    protected void onPropertyChanged(String propertyName) {
        if (isDisposed())
            return;

        super.onPropertyChanged(propertyName);

        switch (propertyName) {
        case PROPERTY_UNIQUE_ID:
            getEntity().getModel().setMosaicSkill(getSkillLevel());
            break;
        case PROPERTY_SKILL_LEVEL:
            super.onPropertyChanged(PROPERTY_UNIQUE_ID);
            break;
        }
    }

    @Override
    protected void onAsyncPropertyChanged(String propertyName) {
        if (isDisposed())
            return;

        super.onAsyncPropertyChanged(propertyName);

        // refire as android data binding event
        switch (propertyName) {
        case PROPERTY_SKILL_LEVEL   : notifyPropertyChanged(BR.skillLevel       ); break;
        case PROPERTY_PADDING_BURGER: notifyPropertyChanged(BR.paddingBurgerMenu); break;
        }
    }

    private void onBurgerModelPropertyChanged(String propertyName) {
        if (!isDisposed())
            UiInvoker.Deferred.accept(() -> onPropertyChanged(PROPERTY_BURGER_SUFFIX + propertyName));
    }

    @Override
    public void close() {
        getEntity().getBurgerModel().setListener(null);
        super.close();
    }

}
