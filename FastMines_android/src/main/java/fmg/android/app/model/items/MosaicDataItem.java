package fmg.android.app.model.items;

import androidx.databinding.Bindable;

import java.util.Objects;

import fmg.android.app.BR;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicImg2;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.core.img.MosaicImageModel2;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_TYPE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;
import static fmg.core.img.PropertyConst.PROPERTY_SKILL_LEVEL;

/** Mosaic item for data model */
public class MosaicDataItem extends BaseDataItem<EMosaic, MosaicImageModel2, AndroidBitmapView<MosaicImageModel2>, MosaicImg2.MosaicAndroidBitmapController> {

    private ESkillLevel skillLevel;

    public MosaicDataItem(EMosaic mosaicType) {
        super(mosaicType);
        setTitle(fixTitle(mosaicType));
    }

    @Bindable
    public EMosaic getMosaicType()                   { return getUniqueId(); }
    public void    setMosaicType(EMosaic mosaicType) {        setUniqueId(mosaicType); }

    @Bindable
    public ESkillLevel getSkillLevel() {
        if (skillLevel == null)
            setSkillLevel(ESkillLevel.eBeginner);
        return skillLevel;
    }
    public void setSkillLevel(ESkillLevel skillLevel) {
        Objects.requireNonNull(skillLevel, "Value of type " + ESkillLevel.class.getSimpleName() + " must be defined");
        if (this.skillLevel == skillLevel)
            return;
        this.skillLevel = skillLevel;
        firePropertyChanged(PROPERTY_SKILL_LEVEL);
    }

    @Override
    public MosaicImg2.MosaicAndroidBitmapController getEntity() {
        if (this.entity == null) {
            Matrisize sizeField = getSkillLevel().sizeTileField(getMosaicType());
            var tmp = new MosaicImg2.MosaicAndroidBitmapController();
            var m = tmp.getModel();
            m.setMosaicType(getMosaicType());
            m.setSizeField(sizeField);
            m.setPadding(new BoundDouble(5 * getZoom()));
            m.setRotateMode(MosaicImageModel2.ERotateMode.SOME_CELLS);
//            m.setBackgroundColor(MosaicDrawModel.DefaultBkColor);
            m.getPenBorder().setWidth(3 * getZoom());
//            m.setRotateAngle(45 * ThreadLocalRandom.current().nextInt(7));

//            android.graphics.BitmapView bmp = tmp.getImage();
//            assert (bmp.getWidth()  == (int)(getSize().width * getZoom()));
//            assert (bmp.getHeight() == (int)(getSize().height * getZoom()));
            setEntity(tmp);
        }
        return this.entity;
    }

    @Override
    protected void firePropertyChanged(String propertyName) {
        if (isDisposed())
            return;

        super.firePropertyChanged(propertyName);

        switch (propertyName) {
        case PROPERTY_SIZE: // TODO delete this case!!
            super.firePropertyChanged(PROPERTY_TITLE);
            break;
        case PROPERTY_UNIQUE_ID:
            getEntity().getModel().setMosaicType(getMosaicType());
            getEntity().getModel().setSizeField(calcSizeField(getSkillLevel()));
            setTitle(fixTitle(getMosaicType()));
            break;
        case PROPERTY_SKILL_LEVEL:
            getEntity().getModel().setSizeField(calcSizeField(getSkillLevel()));
            break;
        case PROPERTY_MOSAIC_TYPE:
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
        case PROPERTY_MOSAIC_TYPE: notifyPropertyChanged(BR.mosaicType); break;
        case PROPERTY_SKILL_LEVEL: notifyPropertyChanged(BR.skillLevel); break;
        }
    }

    private static String fixTitle(EMosaic mosaicType) {
        return mosaicType.getDescription(false);//.replace("-", "\u2006-\u2006");
    }

    private Matrisize calcSizeField(ESkillLevel skill) {
        return ((skill == ESkillLevel.eCustom)
                    ? ESkillLevel.eBeginner
                    : skill)
                .sizeTileField(getMosaicType());
    }

}
