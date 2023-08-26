package fmg.android.app.model.dataSource;

import androidx.databinding.Bindable;

import java.util.List;
import java.util.stream.Collectors;

import fmg.android.app.BR;
import fmg.android.app.model.items.LogoDataItem;
import fmg.android.app.model.items.MosaicDataItem;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.Logo;
import fmg.android.img.MosaicImg;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ImageHelper;
import fmg.core.img.LogoModel;
import fmg.core.img.MosaicImageModel;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_GROUP;
import static fmg.core.img.PropertyConst.PROPERTY_SKILL_LEVEL;

public class MosaicDataSource extends BaseDataSource<
          LogoDataItem,      Void,        LogoModel, AndroidBitmapView<LogoModel>       ,      Logo.  LogoAndroidBitmapController,
          MosaicDataItem, EMosaic, MosaicImageModel, AndroidBitmapView<MosaicImageModel>, MosaicImg.MosaicAndroidBitmapController>
{

    private EMosaicGroup mosaicGroup;
    private ESkillLevel skillLevel;

    @Override
    public LogoDataItem getHeader() {
        if (header == null) {
            header = new LogoDataItem();

            var ctrlr = header.getEntity();
            var model = ctrlr.getModel();
            model.setPadding(new BoundDouble(3));
            ctrlr.setPolarLights(true);

            firePropertyChanged(PROPERTY_HEADER);
        }
        return header;
    }

    @Override
    public List<MosaicDataItem> getDataSource() {
        if (!isDisposed() && ((dataSource == null) || dataSource.isEmpty()))
            reloadDataSource();
        return dataSource;
    }

    @Bindable
    public EMosaicGroup getMosaicGroup() { return mosaicGroup; }
    public void setMosaicGroup(EMosaicGroup mosaicGroup) {
        if (mosaicGroup == this.mosaicGroup)
            return;
        this.mosaicGroup = mosaicGroup;
        firePropertyChanged(PROPERTY_MOSAIC_GROUP);
    }

    @Bindable
    public ESkillLevel getSkillLevel() { return skillLevel; }
    public void setSkillLevel(ESkillLevel skillLevel) {
        if (skillLevel == this.skillLevel)
            return;
        this.skillLevel = skillLevel;
        firePropertyChanged(PROPERTY_SKILL_LEVEL);
    }

    private void reloadDataSource() {
        if (getMosaicGroup() == null)
            return;

        List<EMosaic> newEntities = getMosaicGroup().getMosaics();

        if ((dataSource == null) || dataSource.isEmpty()) {
            // first load all
            dataSource = newEntities.stream()
                    .map(this::makeItem)
                    .collect(Collectors.toList());

            firePropertyChanged(PROPERTY_DATA_SOURCE);
            return;
        }

        // Перегружаю не всё, а только то, что нужно. Остальное - обновляю.
        SizeDouble size = getImageSize(); // save
        int pos = getCurrentItemPos(); // save
        int oldSize = dataSource.size();
        int newSize = newEntities.size();
        int max = Math.max(oldSize, newSize);
        int min = Math.min(oldSize, newSize);
        boolean remove = (oldSize > newSize);
        for (int i = 0; i < max; ++i) {
            if ((i >= min) && remove) {
                dataSource.get(min).close();
                dataSource.remove(min);
                continue;
            }
            EMosaic mosaicType = newEntities.get(i);
            if (i < min) {
                MosaicDataItem mi = dataSource.get(i);
                mi.setUniqueId(mosaicType);
                ESkillLevel skill = getSkillLevel();
                if (skill != null)
                    mi.setSkillLevel(skill);
            } else {
                MosaicDataItem mi = makeItem(mosaicType);
                mi.setSize(size); //  restore
                dataSource.add(mi);
            }
        }
        firePropertyChanged(PROPERTY_DATA_SOURCE);
        setCurrentItemPos(Math.min(pos, dataSource.size() - 1)); // restore pos
    }

    private MosaicDataItem makeItem(EMosaic mosaicType) {
        MosaicDataItem mi = new MosaicDataItem(mosaicType);
        ESkillLevel skill = getSkillLevel();
        if (skill != null)
            mi.setSkillLevel(skill);
        var ctrlr = mi.getEntity();
        var model = ctrlr.getModel();
        model.getPenBorder().setWidth(1);
        ctrlr.setAnimatePeriod(2500);
        ctrlr.setFps(30);
        applySelection(mi);
        return mi;
    }

    @Override
    protected void onCurrentItemChanged() {
        for (MosaicDataItem mi : getDataSource())
            applySelection(mi);
    }

    /** for one selected item - start animate; for all other - stop animate */
    private void applySelection(MosaicDataItem item) {
        boolean selected = item == getCurrentItem(); // check by reference
        var ctrlr = item.getEntity();
        var model = ctrlr.getModel();
        ctrlr.setRotateImage(selected);
        model.getPenBorder().setColorLight (selected ? Color.White() : Color.Black());
        model.getPenBorder().setColorShadow(selected ? Color.White() : Color.Black());
        model.setBackgroundColor(selected ? ImageHelper.DEFAULT_BK_COLOR : DefaultBkColor);
        model.setPadding(new BoundDouble(model.getSize().width * (selected ? 10 : 5) /*/(mi.SkillLevel.Ordinal() + 1)*/ / 100));
        model.setRotateAngle(0);
    }

    @Override
    protected void firePropertyChanged(String propertyName) {
        super.firePropertyChanged(propertyName);

        switch (propertyName) {
        case PROPERTY_MOSAIC_GROUP:
        case PROPERTY_SKILL_LEVEL:
            reloadDataSource();
            break;
        }
    }

    @Override
    protected void firePropertyChangedAsync(String propertyName) {
        super.firePropertyChangedAsync(propertyName);

        switch (propertyName) {
        case PROPERTY_MOSAIC_GROUP: notifyPropertyChanged(BR.mosaicGroup); break;
        case PROPERTY_SKILL_LEVEL : notifyPropertyChanged(BR.skillLevel); break;
        }
    }

}
