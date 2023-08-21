package fmg.android.app.model.dataSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.ProjSettings;
import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicSkillImg2;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.core.img.ImageHelper;
import fmg.core.img.MosaicSkillModel2;
import fmg.core.types.ESkillLevel;

public class MosaicSkillDataSource extends BaseDataSource<
        MosaicSkillDataItem, ESkillLevel, MosaicSkillModel2, AndroidBitmapView<MosaicSkillModel2>, MosaicSkillImg2.MosaicSkillAndroidBitmapController,
        MosaicSkillDataItem, ESkillLevel, MosaicSkillModel2, AndroidBitmapView<MosaicSkillModel2>, MosaicSkillImg2.MosaicSkillAndroidBitmapController>
{

    @Override
    public MosaicSkillDataItem getHeader() {
        if (header == null) {
            header = new MosaicSkillDataItem(null);

            var ctrlr = header.getEntity();
            var model = ctrlr.getModel();
            model.setPadding(new BoundDouble(3));
            model.setBackgroundColor(Color.Transparent());
            ctrlr.setFps(20);
            ctrlr.setAnimatePeriod(13000);
            ctrlr.setPolarLightsForeground(true);

            firePropertyChanged(PROPERTY_HEADER);
        }
        return header;
    }

    @Override
    public List<MosaicSkillDataItem> getDataSource() {
        if (!isDisposed() && ((dataSource == null) || dataSource.isEmpty())) {
            dataSource = Stream.of(ESkillLevel.values())
                    .filter(sk ->
                        !ProjSettings.isReleaseMode() || (sk != ESkillLevel.eCustom)
                    )
                    .map(MosaicSkillDataItem::new)
                    .peek(item -> {
                        var ctrlr = item.getEntity();
                        ctrlr.setFps(20);
                        ctrlr.setAnimatePeriod(3600);
                        applySelection(item);
                    })
                    .collect(Collectors.toList());

            firePropertyChanged(PROPERTY_DATA_SOURCE);
        }
        return dataSource;
    }

    @Override
    protected void onCurrentItemChanged() {
        getDataSource().forEach(this::applySelection);
    }

    /** for one selected item - start animate; for all other - stop animate */
    private void applySelection(MosaicSkillDataItem item) {
        boolean selected = (item.getUniqueId().ordinal() == currentItemPos);
        var ctrlr = item.getEntity();
        var model = ctrlr.getModel();
        ctrlr.setPolarLightsForeground(selected);
        model.setBorderColor(selected ? Color.LawnGreen() : Color.IndianRed());
        model.setBackgroundColor(selected ? ImageHelper.DEFAULT_BK_COLOR : DefaultBkColor);
        model.setPadding(new BoundDouble(selected ? 5 : 15));
        if (!selected)
            model.setForegroundColor(ImageHelper.DEFAULT_FOREGROUND_COLOR.brighter());
//        else {
//            HSV hsv = new HSV(AnimatedImageModel.DefaultForegroundColor);
//            hsv.s = hsv.v = 100;
//            model.setForegroundColor(hsv.toColor());
//        }
    }

}
