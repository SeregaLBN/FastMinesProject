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
               Reload();
            }
         }
      }

      private ESkillLevel _currentSkill;
      public ESkillLevel CurrentSkill {
         get { return _currentSkill; }
         set {
            if (SetProperty(ref _currentSkill, value)) {
               Reload();
            }
         }
      }

      private void Reload() {
         var size = ImageSize; // save
         Reset();
         FillDataSource();
         ImageSize = size; // restore
      }

      protected override void FillDataSource() {
         var dataSource = DataSourceInternal;
         foreach (var s in CurrentGroup.GetBind()) {
            var mi = new MosaicTailItem(s) {
               SkillLevel = CurrentSkill,
               MosaicImage = {
                  BorderColor = Color.Dark,
                  BackgroundColor = GraphicContext.DefaultBackgroundFillColor,
                  Padding = new Bound(15)
                  //RedrawInterval = 50,
                  //RotateAngleDelta = 5
               }
            };
            dataSource.Add(mi);
         }
         base.FillDataSource();
      }

      protected override void OnCurrentElementChanged() {
         //LoggerSimple.Put("MosaicsDataSource::OnCurrentElementChanged: CurrentElement=" + CurrentElement?.MosaicType);
         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicImage;
            using (img.Deferring(false)) {
               //img.Rotate = selected;
               img.BorderColor = selected ? Color.White : Color.Dark;
               img.BackgroundColor = selected ? MosaicsImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
               img.Padding = new Bound(selected ? 5 : 15);
            }
         }
      }

   }
}
