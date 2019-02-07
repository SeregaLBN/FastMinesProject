package fmg.android.app.model.dataSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.img.MosaicGroupImg;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.types.EMosaicGroup;

public class MosaicGroupDataSource extends BaseDataSource<
        MosaicGroupDataItem,
        MosaicGroupDataItem,
        EMosaicGroup,
        EMosaicGroup,
        MosaicGroupModel, MosaicGroupImg.Bitmap, MosaicGroupImg.ControllerBitmap,
        MosaicGroupModel, MosaicGroupImg.Bitmap, MosaicGroupImg.ControllerBitmap>
{

    @Override
    public MosaicGroupDataItem getHeader() {
        if (header == null) {
            header = new MosaicGroupDataItem(null);

            MosaicGroupModel model = header.getEntity().getModel();
            model.setPadding(new BoundDouble(3));
            model.setBackgroundColor(Color.Transparent());
            model.setAnimatePeriod(12857);
            model.setTotalFrames(257);
            model.setPolarLights(true);
            model.setAnimated(true);

            notifier.onPropertyChanged(null, header, PROPERTY_HEADER);
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
                    model.setAnimatePeriod(18000);
                    model.setTotalFrames(257);
                })
                .collect(Collectors.toList());

            notifier.onPropertyChanged(null, dataSource, PROPERTY_DATA_SOURCE);
        }
        return dataSource;
    }

    @Override
    public void onCurrentItemChanged() {
        // for one selected - start animate; for all other - stop animate
        for (MosaicGroupDataItem item : getDataSource()) {
            boolean selected = (item.getUniqueId().ordinal() == currentItemPos);
            MosaicGroupModel model = item.getEntity().getModel();
            model.setPolarLights(selected);
            model.setAnimated(selected);
            model.setBorderColor(selected ? Color.Red() : Color.Green());
            model.setBackgroundColor(selected ? AnimatedImageModel.DefaultBkColor : MosaicDrawModel.DefaultBkColor);
            model.setPadding(new BoundDouble(selected ? 5 : 15));
            if (!selected)
                model.setForegroundColor(AnimatedImageModel.DefaultForegroundColor);
//            else {
//                HSV hsv = new HSV(AnimatedImageModel.DefaultForegroundColor);
//                hsv.s = hsv.v = 100;
//                model.setForegroundColor(hsv.toColor());
//            }
        }
    }

}