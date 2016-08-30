using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.draw.mosaic;
using fmg.data.controller.types;
using MosaicsSkillImg = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using fmg.DataModel.Items;

namespace fmg.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic skills) </summary>
   public class MosaicSkillsDataSource : BaseDataSource<MosaicSkillDataItem, ESkillLevel, MosaicsSkillImg> {

      protected override void FillDataSource() {
         var dataSource = DataSourceInternal;
         foreach (var s in ESkillLevelEx.GetValues()) {
            var mi = new MosaicSkillDataItem(s) {
               Image = {
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
            var img = mi.Image;
            img.Rotate = selected;
            img.BorderColor = selected ? Color.Red : Color.Green;
            img.BackgroundColor = selected ? StaticImgConsts.DefaultBkColor : PaintUwpContext<object>.DefaultBackgroundColor;
            img.Padding = new Bound(selected ? 5 : 15);
         }
      }

   }
}
