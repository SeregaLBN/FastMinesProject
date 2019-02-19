package fmg.android.app.model.dataSource;

import android.databinding.Bindable;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.BR;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.img.MosaicGroupImg;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.types.EMosaicGroup;

public class MosaicGroupDataSource extends BaseDataSource<
        MosaicGroupDataItem, EMosaicGroup, MosaicGroupModel, MosaicGroupImg.Bitmap, MosaicGroupImg.ControllerBitmap,
        MosaicGroupDataItem, EMosaicGroup, MosaicGroupModel, MosaicGroupImg.Bitmap, MosaicGroupImg.ControllerBitmap>
{

    public static final String PROPERTY_UNICODE_CHARS = "UnicodeChars";

    @Override
    public MosaicGroupDataItem getHeader() {
        if (header == null) {
            header = new MosaicGroupDataItem(null);

            MosaicGroupModel model = header.getEntity().getModel();
            model.setPadding(new BoundDouble(3));
            model.setBackgroundColor(Color.Transparent());
            model.setTotalFrames(260);     // rotateAngleDelta = 1.4
            model.setAnimatePeriod(12900); // RedrawInterval = 50
            model.setPolarLights(true);
            model.setAnimated(true);

            notifier.firePropertyChanged(null, header, PROPERTY_HEADER);
        }
        return header;
    }

    @Override
    public List<MosaicGroupDataItem> getDataSource() {
        if ((dataSource == null) || dataSource.isEmpty()) {
            dataSource = Stream.of(EMosaicGroup.values())
                .map(MosaicGroupDataItem::new)
                .peek(item -> {
                    MosaicGroupModel model = item.getEntity().getModel();
                    model.setTotalFrames(260);     // rotateAngleDelta = 1.4
                    model.setAnimatePeriod(18000); // RedrawInterval = 70
                    onItemChanged(item);
                })
                .collect(Collectors.toList());

            notifier.firePropertyChanged(null, dataSource, PROPERTY_DATA_SOURCE);
        }
        return dataSource;
    }

    @Override
    protected void onCurrentItemChanged() {
        getDataSource().forEach(this::onItemChanged);
    }

    private void onItemChanged(MosaicGroupDataItem item) {
        // for one selected - start animate; for all other - stop animate
        boolean selected = (item.getUniqueId().ordinal() == currentItemPos);
        MosaicGroupModel model = item.getEntity().getModel();
        model.setPolarLights(selected);
        model.setAnimated(selected);
        model.setBorderColor(selected ? Color.Red() : Color.Green());
        model.setBackgroundColor(selected ? AnimatedImageModel.DefaultBkColor : MosaicDrawModel.DefaultBkColor);
        model.setPadding(new BoundDouble(selected ? 5 : 15));
        if (!selected)
            model.setForegroundColor(AnimatedImageModel.DefaultForegroundColor);
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
                return "" + item.getMosaicGroup().UnicodeChar(selected);
            })
            .collect(Collectors.joining(" "));
    }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);

        switch (ev.getPropertyName()) {
        case PROPERTY_CURRENT_ITEM:
            notifier.firePropertyChanged(PROPERTY_UNICODE_CHARS);
            break;
        }
    }

    @Override
    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        super.onAsyncPropertyChanged(ev);

        switch (ev.getPropertyName()) {
        case PROPERTY_UNICODE_CHARS: notifyPropertyChanged(BR.unicodeChars); break;
        }
    }

}