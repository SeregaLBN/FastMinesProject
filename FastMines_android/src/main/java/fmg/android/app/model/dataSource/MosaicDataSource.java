package fmg.android.app.model.dataSource;

import android.databinding.Bindable;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.BR;
import fmg.android.app.model.items.LogoDataItem;
import fmg.android.app.model.items.MosaicDataItem;
import fmg.android.img.Logo;
import fmg.android.img.MosaicImg;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.LogoModel;
import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.draw.PenBorder;

public class MosaicDataSource extends BaseDataSource<
        LogoDataItem  ,    Void,           LogoModel      ,      Logo.Bitmap,      Logo.ControllerBitmap,
        MosaicDataItem, EMosaic, MosaicAnimatedModel<Void>, MosaicImg.Bitmap, MosaicImg.ControllerBitmap>
{

    public static final String PROPERTY_CURRENT_GROUP = "CurrentGroup";
    public static final String PROPERTY_CURRENT_SKILL = "CurrentSkill";

    private EMosaicGroup currentGroup;
    private ESkillLevel  currentSkill;

    @Override
    public LogoDataItem getHeader() {
        if (header == null) {
            header = new LogoDataItem();

            LogoModel model = header.getEntity().getModel();
            model.setPadding(new BoundDouble(3));
            model.setBackgroundColor(Color.Transparent());
            model.setPolarLights(true);
            model.setAnimated(true);

            notifier.firePropertyChanged(null, header, PROPERTY_HEADER);
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
    public EMosaicGroup getCurrentGroup() { return currentGroup; }
    public void setCurrentGroup(EMosaicGroup currentGroup) {
        notifier.setProperty(this.currentGroup, currentGroup, PROPERTY_CURRENT_GROUP);
    }

    @Bindable
    public ESkillLevel getCurrentSkill() { return currentSkill; }
    public void setCurrentSkill(ESkillLevel currentSkill) {
        notifier.setProperty(this.currentSkill, currentSkill, PROPERTY_CURRENT_SKILL);
    }

    private void reloadDataSource() {
        List<EMosaic> newEntities = (getCurrentGroup() == null)
                ? getCurrentGroup().getMosaics()
                : Stream.of(EMosaic.values()).collect(Collectors.toList());

        if ((dataSource == null) || dataSource.isEmpty()) {
            // first load all
            dataSource = newEntities.stream()
                    .map(this::makeItem)
                    .collect(Collectors.toList());

            notifier.firePropertyChanged(null, dataSource, PROPERTY_DATA_SOURCE);
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
                ESkillLevel skill = getCurrentSkill();
                if (skill != null)
                    mi.setSkillLevel(skill);
            } else {
                MosaicDataItem mi = makeItem(mosaicType);
                mi.setSize(size); //  restore
            }
        }
        notifier.firePropertyChanged(null, dataSource, PROPERTY_DATA_SOURCE);
        setCurrentItemPos(Math.min(pos, dataSource.size() - 1)); // restore pos
    }

    private MosaicDataItem makeItem(EMosaic mosaicType) {
        MosaicDataItem mi = new MosaicDataItem(mosaicType);
        ESkillLevel skill = getCurrentSkill();
        if (skill != null)
            mi.setSkillLevel(skill);
        MosaicAnimatedModel<?> model = mi.getEntity().getModel();
        model.getPenBorder().setWidth(1);
        model.setAnimatePeriod(2500);
        model.setTotalFrames(70);
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
        boolean selected = (item.getUniqueId().ordinal() == currentItemPos);
        MosaicAnimatedModel<?> model = item.getEntity().getModel();
        model.setAnimated(selected);
        model.getPenBorder().setColorLight (selected ? Color.White() : Color.Black());
        model.getPenBorder().setColorShadow(selected ? Color.White() : Color.Black());
        model.setBackgroundColor(selected ? AnimatedImageModel.DefaultBkColor : MosaicDrawModel.DefaultBkColor);
        model.setPadding(new BoundDouble(model.getSize().width * (selected ? 10 : 5) /*/(mi.SkillLevel.Ordinal() + 1)*/ / 100));
        model.setRotateAngle(0);
    }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);

        switch (ev.getPropertyName()) {
        case PROPERTY_CURRENT_GROUP:
        case PROPERTY_CURRENT_SKILL:
            reloadDataSource();
            break;
        }
    }

    @Override
    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        super.onAsyncPropertyChanged(ev);

        switch (ev.getPropertyName()) {
        case PROPERTY_CURRENT_GROUP: notifyPropertyChanged(BR.currentGroup); break;
        case PROPERTY_CURRENT_SKILL: notifyPropertyChanged(BR.currentSkill); break;
        }
    }

}
