package fmg.android.app.model.items;

import android.databinding.Bindable;

import java.beans.PropertyChangeEvent;

import fmg.android.app.BR;
import fmg.android.img.MosaicImg;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.core.img.IMosaicAnimatedModel;
import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Mosaic item for data model */
public class MosaicDataItem extends BaseDataItem<EMosaic, MosaicAnimatedModel<Void>, MosaicImg.BitmapView, MosaicImg.BitmapController> {

    public static final String PROPERTY_MOSAIC_TYPE = MosaicGameModel.PROPERTY_MOSAIC_TYPE;
    public static final String PROPERTY_SKILL_LEVEL = "SkillLevel";

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
        if (skillLevel == null)
            throw new IllegalArgumentException("Value of type " + ESkillLevel.class.getSimpleName() + " must be defined");
        notifier.setProperty(this.skillLevel, skillLevel, PROPERTY_SKILL_LEVEL);
    }

    @Override
    public MosaicImg.BitmapController getEntity() {
        if (this.entity == null) {
            Matrisize sizeField = getSkillLevel().sizeTileField(getMosaicType());
            MosaicImg.BitmapController tmp = new MosaicImg.BitmapController();
            MosaicAnimatedModel<?> m = tmp.getModel();
            m.setMosaicType(getMosaicType());
            m.setSizeField(sizeField);
            m.setPadding(new BoundDouble(5 * getZoom()));
            m.setRotateMode(IMosaicAnimatedModel.EMosaicRotateMode.someCells);
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
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);

        switch (ev.getPropertyName()) {
        case PROPERTY_UNIQUE_ID:
            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_MOSAIC_TYPE); // recall with another property name
            getEntity().setMosaicType(getMosaicType());
            getEntity().setSizeField(calcSizeField(getSkillLevel()));
            setTitle(fixTitle(getMosaicType()));
            break;
        case PROPERTY_SKILL_LEVEL:
            getEntity().setSizeField(calcSizeField(getSkillLevel()));
            break;
        }
    }

    @Override
    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        super.onAsyncPropertyChanged(ev);

        // refire as android data binding event
        switch (ev.getPropertyName()) {
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

    @Override
    public String getTitle() { return super.getTitle() + " " + getSize(); }

}
