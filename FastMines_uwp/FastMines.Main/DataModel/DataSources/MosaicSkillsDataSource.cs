using fmg.common;
using fmg.common.geom;
using fmg.data.controller.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using fmg.common.Controls;
using fmg.uwp.draw.mosaic;

namespace fmg.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic skills) </summary>
   public class MosaicSkillsDataSource : BaseDataSource<MosaicSkillMenuItem, ESkillLevel> {

      protected override void FillDataSource() {
         var dataSource = DataSourceInternal;
         foreach (var s in ESkillLevelEx.GetValues()) {
            var mi = new MosaicSkillMenuItem(s) {
               MosaicSkillImage = {
                  RedrawInterval = 50,
                  RotateAngleDelta = 5
               }
            };
            dataSource.Add(mi);
         }
         base.FillDataSource();
      }

      protected override void OnCurrentElementChanged() {
         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicSkillImage;
            img.Rotate = selected;
            img.BorderColor = selected ? Color.Red : Color.Green;
            img.BackgroundColor = selected ? MosaicsSkillImg.DefaultBkColor : PaintContext<object>.DefaultBackgroundFillColor;
            img.Padding = new Bound(selected ? 5 : 15);
         }
      }

   }
}
