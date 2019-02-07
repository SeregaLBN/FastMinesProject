package fmg.android.app.model.dataSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.img.MosaicSkillImg;
import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.MosaicSkillModel;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.types.ESkillLevel;

public class MosaicSkillDataSource extends BaseDataSource<
        MosaicSkillDataItem,
        MosaicSkillDataItem,
        ESkillLevel,
        ESkillLevel,
        MosaicSkillModel, MosaicSkillImg.Bitmap, MosaicSkillImg.ControllerBitmap,
        MosaicSkillModel, MosaicSkillImg.Bitmap, MosaicSkillImg.ControllerBitmap>
{

    @Override
    public MosaicSkillDataItem getHeader() {
        if (header == null) {
            header = new MosaicSkillDataItem(null);

            MosaicSkillModel model = header.getEntity().getModel();
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
    public List<MosaicSkillDataItem> getDataSource() {
        if ((dataSource == null) || dataSource.isEmpty()) {
            dataSource = Stream.of(ESkillLevel.values())
                    .map(MosaicSkillDataItem::new)
                    .peek(item -> {
                        MosaicSkillModel model = item.getEntity().getModel();
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
        for (MosaicSkillDataItem item : getDataSource()) {
            boolean selected = (item.getUniqueId().ordinal() == currentItemPos);
            MosaicSkillModel model = item.getEntity().getModel();
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
