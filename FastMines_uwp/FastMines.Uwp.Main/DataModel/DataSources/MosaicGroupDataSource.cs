using System.Linq;
using System.ComponentModel;
using System.Collections.ObjectModel;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using Fmg.Core.Mosaic;
using Fmg.DataModel.Items;
using MosaicGroupView       = Fmg.Uwp.Img.Win2d.MosaicGroupImg.CanvasBmpView;
using MosaicGroupController = Fmg.Uwp.Img.Win2d.MosaicGroupImg.CanvasBmpController;

namespace Fmg.DataModel.DataSources {

    /// <summary> DataSource menu items (mosaic groups) </summary>
    public class MosaicGroupDataSource : BaseDataSource<
        MosaicGroupDataItem, EMosaicGroup?, MosaicGroupModel, MosaicGroupView, MosaicGroupController,
        MosaicGroupDataItem, EMosaicGroup?, MosaicGroupModel, MosaicGroupView, MosaicGroupController>
    {

        public override MosaicGroupDataItem Header {
            get {
                if (header == null) {
                    header = new MosaicGroupDataItem(null);

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

        public override ObservableCollection<MosaicGroupDataItem> DataSource {
            get {
                if (!dataSource.Any() && !Disposed) {
                    foreach( var e in EMosaicGroupEx.GetValues()) {
                        var item = new MosaicGroupDataItem(e);
                        var model = item.Entity.Model;
                        model.TotalFrames = 260;
                        model.AnimatePeriod = 18000;
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
        private void ApplySelection(MosaicGroupDataItem item) {
            var selected = ReferenceEquals(item, CurrentItem);
            var model = item.Entity.Model;
            model.PolarLights = selected;
            model.Animated = selected;
            model.BorderColor = selected ? Color.LawnGreen : Color.IndianRed;
            model.BackgroundColor = selected ? AnimatedImageModelConst.DefaultBkColor : MosaicDrawModelConst.DefaultBkColor;
            model.Padding = new BoundDouble(selected ? 5 : 15);
            if (!selected)
                model.ForegroundColor = AnimatedImageModelConst.DefaultForegroundColor.Brighter();
            else
                model.ForegroundColor = Color.Orchid;
            //else {
            //    HSV hsv = new HSV(AnimatedImageModelConst.DefaultForegroundColor);
            //    hsv.s = hsv.v = 100;
            //    model.ForegroundColor = hsv.ToColor();
            //}
        }

        public string UnicodeChars {
            get {
                var ci = CurrentItem;
                return string.Join(" ", DataSource.Select(item => {
                    var selected = (ci != null) && (item.MosaicGroup == ci.MosaicGroup);
                    return item.MosaicGroup.Value.UnicodeChar(selected);
                }));
            }
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(sender, ev);

            switch (ev.PropertyName) {
            case nameof(this.CurrentItem):
                _notifier.FirePropertyChanged(nameof(this.UnicodeChars));
                break;
            }
        }

    }

}
