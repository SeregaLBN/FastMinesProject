using System.Linq;
using System.Collections.ObjectModel;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Core.Types;
using Fmg.Core.Mosaic;
using Fmg.Uwp.App.Model.Items;
using MosaicSkillView       = Fmg.Uwp.Img.Win2d.MosaicSkillImg.CanvasBmpView;
using MosaicSkillController = Fmg.Uwp.Img.Win2d.MosaicSkillImg.CanvasBmpController;

namespace Fmg.Uwp.App.Model.DataSources {

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
                    model.TotalFrames = 260;
                    model.AnimatePeriod = 12900;
                    model.PolarLights = true;
                    model.Animated = true;
                }
                return header;
            }
        }

        public override ObservableCollection<MosaicSkillDataItem> DataSource {
            get {
                if (!dataSource.Any() && !Disposed) {
                    foreach (var e in ESkillLevelEx.GetValues()) {
                        var item = new MosaicSkillDataItem(e);
                        var model = item.Entity.Model;
                        model.TotalFrames = 72;
                        model.AnimatePeriod = 3600;
                        ApplySelection(item);
                        dataSource.Add(item);
                    }
                    _notifier.FirePropertyChanged();
                }
                return dataSource;
            }
        }

        protected override void OnCurrentItemChanged() {
            foreach (var item in DataSource)
                ApplySelection(item);
        }

        /// <summary> for one selected item - start animate; for all other - stop animate </summary>
        private void ApplySelection(MosaicSkillDataItem item) {
            var selected = ReferenceEquals(item, CurrentItem);
            var model = item.Entity.Model;
            model.Animated = selected;
            model.PolarLights = selected;
            model.BorderColor = selected ? Color.LawnGreen : Color.IndianRed;
            model.BackgroundColor = selected ? AnimatedImageModelConst.DefaultBkColor : DefaultBkColor;
            model.Padding = new BoundDouble(selected ? 5 : 15);
            if (!selected)
                model.ForegroundColor = AnimatedImageModelConst.DefaultForegroundColor.Brighter();
            else
                model.ForegroundColor = Color.Orchid;
            //else {
            //    HSV hsv = new HSV(ImageModelConsts.DefaultForegroundColor);
            //    hsv.s = hsv.v = 100;
            //    model.ForegroundColor = hsv.ToColor();
            //}
        }

    }

}
