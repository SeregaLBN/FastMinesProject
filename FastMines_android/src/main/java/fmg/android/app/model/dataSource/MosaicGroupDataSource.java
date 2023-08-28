package fmg.android.app.model.dataSource;

import androidx.databinding.Bindable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.BR;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.img.AndroidBitmapView;
import fmg.android.img.MosaicGroupImg;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.core.img.ImageHelper;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

public class MosaicGroupDataSource extends BaseDataSource<
        MosaicGroupDataItem, EMosaicGroup, MosaicGroupModel, AndroidBitmapView<MosaicGroupModel>, MosaicGroupImg.MosaicGroupAndroidBitmapController,
        MosaicGroupDataItem, EMosaicGroup, MosaicGroupModel, AndroidBitmapView<MosaicGroupModel>, MosaicGroupImg.MosaicGroupAndroidBitmapController>
{

    public static final String PROPERTY_UNICODE_CHARS = "UnicodeChars";

    @Override
    public MosaicGroupDataItem getHeader() {
        if (header == null) {
            header = new MosaicGroupDataItem(null);

            var ctrlr = header.getController();
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
    public List<MosaicGroupDataItem> getDataSource() {
        if (!isDisposed() && ((dataSource == null) || dataSource.isEmpty())) {
            dataSource = Stream.of(EMosaicGroup.values())
                .map(MosaicGroupDataItem::new)
                .peek(item -> {
                    var ctrlr = item.getController();
                    ctrlr.setFps(20);
                    ctrlr.setAnimatePeriod(18000);
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
    private void applySelection(MosaicGroupDataItem item) {
        boolean selected = (item.getUniqueId().ordinal() == currentItemPos);
        var ctrlr = item.getController();
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

    @Bindable
    public String getUnicodeChars() {
        MosaicGroupDataItem ci = getCurrentItem();
        return getDataSource().stream()
            .map(item -> {
                boolean selected = (ci != null) && (item.getMosaicGroup() == ci.getMosaicGroup());
                return "" + item.getMosaicGroup().unicodeChar(selected);
            })
            .collect(Collectors.joining(" "));
    }

    @Override
    protected void firePropertyChanged(String propertyName) {
        super.firePropertyChanged(propertyName);

        switch (propertyName) {
        case PROPERTY_CURRENT_ITEM:
            super.firePropertyChanged(PROPERTY_UNICODE_CHARS);
            break;
        }
    }

    @Override
    protected void firePropertyChangedAsync(String propertyName) {
        super.firePropertyChangedAsync(propertyName);

        switch (propertyName) {
        case PROPERTY_UNICODE_CHARS: notifyPropertyChanged(BR.unicodeChars); break;
        }
    }

}