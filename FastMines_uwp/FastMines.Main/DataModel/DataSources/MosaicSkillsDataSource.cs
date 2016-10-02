using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.draw.mosaic;
using fmg.data.controller.types;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;
using fmg.DataModel.Items;

namespace fmg.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic skills) </summary>
   public class MosaicSkillsDataSource : BaseDataSource<MosaicSkillDataItem, ESkillLevel?, MosaicsSkillCanvasBmp> {

      MosaicSkillDataItem _itemOfType;

      protected override void FillDataSource() {
         _itemOfType = new MosaicSkillDataItem(null) {
            Image = {
               PaddingInt = 3,
               BackgroundColor = Color.Transparent,
               RedrawInterval = 50,
               PolarLights = true,
               Rotate = true
            }
         };

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

      /// <summary> representative typeof(ESkillLevel) </summary>
      public MosaicSkillDataItem TopElement => _itemOfType;

      protected override void OnCurrentElementChanged() {
         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.Image;
            img.Rotate = selected;
            img.PolarLights = selected;
            img.BorderColor = selected ? Color.Red : Color.Green;
            img.BackgroundColor = selected ? StaticImgConsts.DefaultBkColor : PaintUwpContext<object>.DefaultBackgroundColor;
            img.Padding = new Bound(selected ? 5 : 15);
            if (!selected)
               img.ForegroundColor = StaticImgConsts.DefaultForegroundColor;
            //else {
            //   HSV hsv = new HSV(StaticImgConsts.DefaultForegroundColor);
            //   hsv.s = hsv.v = 100;
            //   img.ForegroundColor = hsv.ToColor();
            //}
         }
      }

   }
}
