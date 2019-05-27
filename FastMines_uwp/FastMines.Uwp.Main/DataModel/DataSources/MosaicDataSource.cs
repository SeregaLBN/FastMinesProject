using System;
using System.Collections.Generic;
using System.Linq;
using System.ComponentModel;
using System.Collections.ObjectModel;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using Fmg.Core.Mosaic;
using Fmg.DataModel.Items;
using MosaicModel      = Fmg.Core.Img.MosaicAnimatedModel<Fmg.Common.Nothing>;
using MosaicView       = Fmg.Uwp.Img.Win2d.MosaicImg.CanvasBmpView;
using MosaicController = Fmg.Uwp.Img.Win2d.MosaicImg.CanvasBmpController;
using LogoView         = Fmg.Uwp.Img.Win2d.Logo.CanvasBmpView;
using LogoController   = Fmg.Uwp.Img.Win2d.Logo.CanvasBmpController;

namespace Fmg.DataModel.DataSources {

    /// <summary> DataSource mosaics items </summary>
    public class MosaicDataSource : BaseDataSource<
        LogoDataItem  , Nothing,   LogoModel,   LogoView,   LogoController,
        MosaicDataItem, EMosaic, MosaicModel, MosaicView, MosaicController>
    {

        private EMosaicGroup? mosaicGroup;
        private ESkillLevel?  skillLevel;

        public override LogoDataItem Header {
            get {
                if (header == null) {
                    header = new LogoDataItem();

                    var model = header.Entity.Model;
                    model.Padding = new BoundDouble(3);
                    model.BackgroundColor = Color.Transparent;
                    model.PolarLights = true;
                    model.Animated = true;

                    _notifier.FirePropertyChanged(null, header);
                }
                return header;
            }
        }

        public override ObservableCollection<MosaicDataItem> DataSource {
            get {
                if (!Disposed && ((dataSource == null) || !dataSource.Any()))
                    ReloadDataSource();
                return dataSource;
            }
        }

        public EMosaicGroup? MosaicGroup {
            get { return mosaicGroup; }
            set { _notifier.SetProperty(ref mosaicGroup, value); }
        }

        public ESkillLevel? SkillLevel {
            get { return skillLevel; }
            set { _notifier.SetProperty(ref skillLevel, value); }
        }

        private void ReloadDataSource() {
            //LoggerSimple.Put("> " + nameof(MosaicDataSource) + "::" + nameof(ReloadDataSource));
            IList<EMosaic> newEntities = MosaicGroup.HasValue
                    ? MosaicGroup.Value.GetMosaics()
                    : EMosaicEx.GetValues().ToList();

            if ((dataSource == null) || !dataSource.Any()) {
                // first load all
                newEntities
                    .Select(e => MakeItem(e))
                    .ToList()
                    .ForEach(mi => dataSource.Add(mi));

                _notifier.FirePropertyChanged(null, dataSource, nameof(DataSource));
                return;
            }

            // Перегружаю не всё, а только то, что нужно. Остальное - обновляю.
            var size = ImageSize; // save
            int pos = CurrentItemPos; // save
            //LoggerSimple.Put("  " + nameof(MosaicDataSource) + "::" + nameof(ReloadDataSource) + ": saved item pos=" + pos);
            int oldSize = dataSource.Count();
            int newSize = newEntities.Count();
            int max = Math.Max(oldSize, newSize);
            int min = Math.Min(oldSize, newSize);
            bool remove = (oldSize > newSize);
            for (int i = 0; i < max; ++i) {
                if ((i >= min) && remove) {
                    dataSource[min].Dispose();
                    dataSource.RemoveAt(min);
                    continue;
                }
                EMosaic mosaicType = newEntities[i];
                if (i < min) {
                    var mi = dataSource[i];
                    mi.UniqueId = mosaicType;
                    var skill = SkillLevel;
                    if (skill.HasValue)
                        mi.SkillLevel = skill.Value;
                } else {
                    var mi = MakeItem(mosaicType);
                    mi.Size = size; //  restore
                    dataSource.Add(mi);
                }
            }
            _notifier.FirePropertyChanged(null, dataSource, nameof(DataSource));
            CurrentItemPos = Math.Min(pos, dataSource.Count() - 1); // restore pos
            //LoggerSimple.Put("  " + nameof(MosaicDataSource) + "::" + nameof(ReloadDataSource) + ": restored item pos=" + CurrentItemPos);
        }

        private MosaicDataItem MakeItem(EMosaic mosaicType) {
            var mi = new MosaicDataItem(mosaicType);
            ESkillLevel? skill = SkillLevel;
            if (skill.HasValue)
                mi.SkillLevel = skill.Value;
            var model = mi.Entity.Model;
            model.PenBorder.Width = 1;
            model.AnimatePeriod = 2500;
            model.TotalFrames = 70;
            ApplySelection(mi);
            return mi;
        }

        protected override void OnCurrentItemChanged() {
            //LoggerSimple.Put("> " + nameof(MosaicDataSource) + "::" + nameof(OnCurrentItemChanged) + ": CurrentElement={0}; itemPos={1}", CurrentItem?.MosaicType, CurrentItemPos);
            foreach (var mi in DataSource)
                ApplySelection(mi);
        }

        /// <summary> for one selected item - start animate; for all other - stop animate </summary>
        private void ApplySelection(MosaicDataItem item) {
            var selected = ReferenceEquals(item, CurrentItem);
            var model = item.Entity.Model;
            model.Animated = selected;
            model.PenBorder.ColorLight =
            model.PenBorder.ColorShadow = selected ? Color.White : Color.Black;
            model.BackgroundColor = selected ? AnimatedImageModelConst.DefaultBkColor : MosaicDrawModelConst.DefaultBkColor;
            model.Padding = new BoundDouble(model.Size.Width * (selected ? 10 : 5) /*/(mi.SkillLevel.Ordinal() + 1)*/ / 100);
            model.RotateAngle = 0;
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(sender, ev);

            switch (ev.PropertyName) {
            case nameof(MosaicGroup):
            case nameof(SkillLevel):
                ReloadDataSource();
                break;
            }
        }

    }

}
