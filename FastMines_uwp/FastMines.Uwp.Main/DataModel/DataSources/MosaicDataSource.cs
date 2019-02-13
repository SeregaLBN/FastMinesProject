using System;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.uwp.draw.mosaic;
using MosaicsCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasBmp;
using fmg.DataModel.Items;

namespace fmg.DataModel.DataSources {

    /// <summary> DataSource mosaics items </summary>
    public class MosaicsDataSource : BaseDataSource<MosaicDataItem, EMosaic, MosaicsCanvasBmp> {

        private EMosaicGroup _currentGroup;
        public EMosaicGroup CurrentGroup {
            get { return _currentGroup; }
            set {
                if (SetProperty(ref _currentGroup, value)) {
                    ReloadDataSource();
                }
            }
        }

        private ESkillLevel _currentSkill;
        public ESkillLevel CurrentSkill {
            get { return _currentSkill; }
            set {
                if (SetProperty(ref _currentSkill, value)) {
                    ReloadDataSource();
                }
            }
        }

        private void ReloadDataSource() {
            if (!true) {
                // reload all
                var size = ImageSize; // save
                DataSourceInternal.Clear();
                FillDataSource();
                foreach (var mi in DataSourceInternal)
                    mi.ImageSize = size; //  restore
            } else {
                // Перегружаю не всё, а только то, что нужно. Остальное - обновляю.
                var size = ImageSize; // save
                CurrentElement = null;
                var dataSource = DataSourceInternal;
                var newEntities = CurrentGroup.GetMosaics().ToList();
                var max = Math.Max(dataSource.Count, newEntities.Count);
                var min = Math.Min(dataSource.Count, newEntities.Count);
                var remove = (dataSource.Count > newEntities.Count);
                for (var i = 0; i < max; ++i) {
                    if ((i >= min) && remove) {
                        dataSource.RemoveAt(min);
                        continue;
                    }
                    var mosaicType = newEntities[i];
                    if (i < min) {
                        var mi = dataSource[i];
                        mi.UniqueId = mosaicType;
                        mi.SkillLevel = CurrentSkill;
                    } else {
                        var mi = AddItem(mosaicType);
                        mi.ImageSize = size; //  restore
                    }
                }
                OnPropertyChanged(nameof(DataSource));
            }
        }

        protected override void FillDataSource() {
            foreach (var mosaicType in CurrentGroup.GetMosaics()) {
                AddItem(mosaicType);
            }
            base.FillDataSource();
        }

        private MosaicDataItem AddItem(EMosaic mosaicType) {
            var mi = new MosaicDataItem(mosaicType) {
                SkillLevel = CurrentSkill,
                Image = {
                    BorderWidth = 1,
                    BorderColor = Color.Black,
                    BackgroundColor = PaintUwpContextCommon.DefaultBackgroundColor,
                    Padding = new Bound(15),
                    RedrawInterval = 5,
                    RotateAngleDelta = 3.37
                }
            };
            DataSourceInternal.Add(mi);
            return mi;
        }

        protected override void OnCurrentElementChanged() {
           //LoggerSimple.Put("MosaicsDataSource::OnCurrentElementChanged: CurrentElement=" + CurrentElement?.MosaicType);
           // for one selected- start animate; for all other - stop animate
           foreach (var mi in DataSource) {
              var selected = ReferenceEquals(mi, CurrentElement);
              var img = mi.Image;
              img.Rotate = selected;
              img.BorderColor = selected ? Color.White : Color.Black;
              img.BackgroundColor = selected ? ImageModelConsts.DefaultBkColor : PaintUwpContextCommon.DefaultBackgroundColor;
              img.Padding = new Bound(img.Size.Width *(selected ? 10 : 5) /*/(mi.SkillLevel.Ordinal() + 1)*//100);
              img.RotateAngle = 0;
           }
        }

    }

}
