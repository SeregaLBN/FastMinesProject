using System;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Controls;

namespace FastMines.DataModel.DataSources {

   /// <summary> DataSource mosaics items </summary>
   public class MosaicsDataSource : BaseDataSource<MosaicTailItem, EMosaic> {

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

      protected override void FillDataSource() {
         foreach (var mosaicType in CurrentGroup.GetBind()) {
            AddItem(mosaicType);
         }
         base.FillDataSource();
      }

      private void ReloadDataSource() {
         var size = ImageSize; // save
         CurrentElement = null;
         var dataSource = DataSourceInternal;
         var newEntities = CurrentGroup.GetBind().ToList();
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
         base.FillDataSource();
      }

      private MosaicTailItem AddItem(EMosaic mosaicType) {
         var mi = new MosaicTailItem(mosaicType) {
            SkillLevel = CurrentSkill,
            MosaicImage = {
                  BorderWidth = 1,
                  BorderColor = Color.Dark,
                  BackgroundColor = GraphicContext.DefaultBackgroundFillColor,
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
            var img = mi.MosaicImage;
            img.Rotate = selected;
            img.BorderColor = selected ? Color.White : Color.Dark;
            img.BackgroundColor = selected ? MosaicsImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
            img.Padding = new Bound(img.Width*(selected ? 10 : 5) /*/(mi.SkillLevel.Ordinal() + 1)*//100);
            img.RotateAngle = 0;
         }
      }

   }
}
