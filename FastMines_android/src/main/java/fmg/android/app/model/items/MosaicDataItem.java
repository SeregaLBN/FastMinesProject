package fmg.android.app.model.items;

import java.beans.PropertyChangeEvent;

import fmg.android.img.MosaicImg;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Mosaic item for data model */
public class MosaicDataItem extends BaseDataItem<EMosaic, MosaicAnimatedModel<Void>, MosaicImg.Bitmap, MosaicImg.ControllerBitmap> {

    public static final String PROPERTY_MOSAIC_TYPE = MosaicGameModel.PROPERTY_MOSAIC_TYPE;
    public static final String PROPERTY_SKILL_LEVEL = "SkillLevel";

    private ESkillLevel skillLevel;

    public MosaicDataItem(EMosaic mosaicType) {
        super(mosaicType);
        setTitle(fixTitle(mosaicType));
    }

    public EMosaic getMosaicType()                   { return getUniqueId(); }
    public void    setMosaicType(EMosaic mosaicType) {        setUniqueId(mosaicType); }

    public ESkillLevel getSkillLevel() { return skillLevel; }
    public void setSkillLevel(ESkillLevel skillLevel) {
        notifier.setProperty(this.skillLevel, skillLevel, PROPERTY_SKILL_LEVEL);
    }

    @Override
    public MosaicImg.ControllerBitmap getEntity() {
        if (this.entity == null) {
            Matrisize sizeField = getSkillLevel().sizeTileField(getMosaicType());
            MosaicImg.ControllerBitmap tmp = new MosaicImg.ControllerBitmap();
            MosaicAnimatedModel<?> m = tmp.getModel();
            m.setMosaicType(getMosaicType());
            m.setSizeField(sizeField);
            m.setPadding(new BoundDouble(5 * getZoom()));
            m.setRotateMode(MosaicAnimatedModel.ERotateMode.someCells);
//            m.setBackgroundColor(MosaicDrawModel.DefaultBkColor);
            m.getPenBorder().setWidth(3 * getZoom());
//            m.setRotateAngle(45 * ThreadLocalRandom.current().nextInt(7));

//            android.graphics.Bitmap bmp = tmp.getImage();
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
            notifier.onPropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_MOSAIC_TYPE); // recall with another property name
            getEntity().setMosaicType(getMosaicType());
            getEntity().setSizeField(calcSizeField(getSkillLevel()));
            setTitle(fixTitle(getMosaicType()));
            break;
        case PROPERTY_SKILL_LEVEL:
            getEntity().setSizeField(calcSizeField(getSkillLevel()));
            break;
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
