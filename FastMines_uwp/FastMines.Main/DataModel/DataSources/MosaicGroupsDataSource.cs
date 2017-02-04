using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.core.mosaic.draw;
using MosaicsGroupCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasBmp;
using fmg.DataModel.Items;

namespace fmg.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic groups) </summary>
   public class MosaicGroupsDataSource : BaseDataSource<MosaicGroupDataItem, EMosaicGroup?, MosaicsGroupCanvasBmp> {

      MosaicGroupDataItem _itemOfType;

      protected override void FillDataSource() {
         _itemOfType = new MosaicGroupDataItem(null) {
            Image = {
               PaddingInt = 3,
               BackgroundColor = Color.Transparent,
               RedrawInterval = 50,
               PolarLights = true,
               Rotate = true
            }
         };

         var dataSource = DataSourceInternal;
         foreach (var g in EMosaicGroupEx.GetValues()) {
            var mi = new MosaicGroupDataItem(g) {
               Image = {
                  RedrawInterval = 70
               }
            };
            dataSource.Add(mi);
         }
         base.FillDataSource();
      }

      /// <summary> representative typeof(EMosaicGroup) </summary>
      public MosaicGroupDataItem TopElement => _itemOfType;

      protected override void OnCurrentElementChanged() {
         OnSelfPropertyChanged(nameof(this.UnicodeChars));

         // for one selected - start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.Image;
            img.PolarLights = selected;
            img.Rotate = selected;
            img.BorderColor = selected ? Color.Red : Color.Green;
            img.BackgroundColor = selected ? StaticImgConsts.DefaultBkColor : PaintContextCommon.DefaultBackgroundColor;
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


      public string UnicodeChars {
         get {
            var smi = CurrentElement;
            return string.Join(" ", DataSource.Select(mi => {
               var selected = (smi != null) && (mi.Image.MosaicGroup == smi.Image.MosaicGroup);
               return mi.Image.MosaicGroup.Value.UnicodeChar(selected);
            }));
         }
      }

   }
}
