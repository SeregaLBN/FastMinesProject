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
        MosaicSkillDataItem, ESkillLevel, MosaicSkillModel, MosaicSkillImg.Bitmap, MosaicSkillImg.ControllerBitmap,
        MosaicSkillDataItem, ESkillLevel, MosaicSkillModel, MosaicSkillImg.Bitmap, MosaicSkillImg.ControllerBitmap>
{

    @Override
    public MosaicSkillDataItem getHeader() {
        if (header == null) {
            header = new MosaicSkillDataItem(null);

            MosaicSkillModel model = header.getEntity().getModel();
            model.setPadding(new BoundDouble(3));
            model.setBackgroundColor(Color.Transparent());
            model.setTotalFrames(260);      // RotateAngleDelta = 1.4
            model.setAnimatePeriod(12900);  // RedrawInterval = 50
            model.setPolarLights(true);
            model.setAnimated(true);

            notifier.firePropertyChanged(null, header, PROPERTY_HEADER);
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
                        model.setTotalFrames(72);     // RotateAngleDelta = 5
                        model.setAnimatePeriod(3600); // RedrawInterval = 50
                    })
                    .collect(Collectors.toList());

            notifier.firePropertyChanged(null, dataSource, PROPERTY_DATA_SOURCE);
        }
        return dataSource;
    }

    @Override
    protected void onCurrentItemChanged() {
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
