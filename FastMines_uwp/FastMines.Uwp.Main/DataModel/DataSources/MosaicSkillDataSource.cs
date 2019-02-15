using System.Linq;
using System.Collections.ObjectModel;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.DataModel.Items;
using MosaicSkillView       = fmg.uwp.img.win2d.MosaicSkillImg.CanvasBmp;
using MosaicSkillController = fmg.uwp.img.win2d.MosaicSkillImg.ControllerBitmap;

namespace fmg.DataModel.DataSources {

    /// <summary> DataSource menu items (mosaic skills) </summary>
    public class MosaicSkillDataSource : BaseDataSource<
        MosaicSkillDataItem, ESkillLevel?, MosaicSkillModel, MosaicSkillView, MosaicSkillController,
        MosaicSkillDataItem, ESkillLevel?, MosaicSkillModel, MosaicSkillView, MosaicSkillController>
    {

        public override MosaicSkillDataItem Header {
            get {
                if (header == null) {
                    header = new MosaicSkillDataItem(null);

                    var model = header.Entity.Model;
                    model.Padding = new BoundDouble(3);
                    model.BackgroundColor = Color.Transparent;
                    model.TotalFrames = 60;
                    model.AnimatePeriod = 12900;
                    model.PolarLights = true;
                    model.Animated = true;
                }
                return header;
            }
        }

        public override ObservableCollection<MosaicSkillDataItem> DataSource {
            get {
                if (!dataSource.Any()) {
                    foreach (var e in ESkillLevelEx.GetValues()) {
                        var item = new MosaicSkillDataItem(e);
                        var model = item.Entity.Model;
                        model.TotalFrames = 60;
                        model.AnimatePeriod = 3600;
                        dataSource.Add(item);
                    }
                    notifier.FirePropertyChanged();
                }
                return dataSource;
            }
        }

        protected override void OnCurrentItemChanged() {
            // for one selected- start animate; for all other - stop animate
            foreach (var item in DataSource) {
                var selected = ReferenceEquals(item, CurrentItem);
                var model = item.Entity.Model;
                model.Animated = selected;
                model.PolarLights = selected;
                model.BorderColor = selected ? Color.Red : Color.Green;
                model.BackgroundColor = selected ? AnimatedImageModelConst.DefaultBkColor : MosaicDrawModelConst.DefaultBkColor;
                model.Padding = new BoundDouble(selected ? 5 : 15);
                if (!selected)
                    model.ForegroundColor = AnimatedImageModelConst.DefaultForegroundColor;
                //else {
                //    HSV hsv = new HSV(ImageModelConsts.DefaultForegroundColor);
                //    hsv.s = hsv.v = 100;
                //    model.ForegroundColor = hsv.ToColor();
                //}
            }
        }

    }

}
