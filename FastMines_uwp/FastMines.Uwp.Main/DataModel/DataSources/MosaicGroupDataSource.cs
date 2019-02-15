using System.Linq;
using System.ComponentModel;
using System.Collections.ObjectModel;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.DataModel.Items;
using MosaicGroupView       = fmg.uwp.img.win2d.MosaicGroupImg.CanvasBmp;
using MosaicGroupController = fmg.uwp.img.win2d.MosaicGroupImg.ControllerBitmap;

namespace fmg.DataModel.DataSources {

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
                    model.TotalFrames = 257; // RedrawInterval = 50;
                    model.PolarLights = true;
                    model.Animated = true;
                }
                return header;
            }
        }

        public override ObservableCollection<MosaicGroupDataItem> DataSource {
            get {
                if (!dataSource.Any()) {
                    foreach( var e in EMosaicGroupEx.GetValues()) {
                        var item = new MosaicGroupDataItem(e);
                        var model = item.Entity.Model;
                        model.AnimatePeriod = 18000; // RedrawInterval = 70
                        model.TotalFrames = 257;
                        dataSource.Add(item);
                    }
                    notifier.FirePropertyChanged();
                }
                return dataSource;
            }
        }

        protected override void OnCurrentItemChanged() {
            // for one selected - start animate; for all other - stop animate
            foreach (var item in DataSource) {
                var selected = ReferenceEquals(item, CurrentItem);
                var model = item.Entity.Model;
                model.PolarLights = selected;
                model.Animated = selected;
                model.BorderColor = selected ? Color.Red : Color.Green;
                model.BackgroundColor = selected ? AnimatedImageModelConst.DefaultBkColor : MosaicDrawModelConst.DefaultBkColor;
                model.Padding = new BoundDouble(selected ? 5 : 15);
                if (!selected)
                    model.ForegroundColor = AnimatedImageModelConst.DefaultForegroundColor;
                //else {
                //    HSV hsv = new HSV(AnimatedImageModelConst.DefaultForegroundColor);
                //    hsv.s = hsv.v = 100;
                //    model.ForegroundColor = hsv.ToColor();
                //}
            }
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
                notifier.FirePropertyChanged(nameof(this.UnicodeChars));
                break;
            }
        }

    }

}
