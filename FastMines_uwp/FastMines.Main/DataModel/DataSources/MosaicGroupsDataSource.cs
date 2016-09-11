using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.uwp.draw.mosaic;
using MosaicsGroupImg = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using fmg.DataModel.Items;

namespace fmg.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic groups) </summary>
   public class MosaicGroupsDataSource : BaseDataSource<MosaicGroupDataItem, EMosaicGroup, MosaicsGroupImg> {

      protected override void FillDataSource() {
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

      protected override void OnCurrentElementChanged() {
         OnSelfPropertyChanged(nameof(this.UnicodeChars));

         // for one selected - start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.Image;
            img.PolarLights = selected;
            img.Rotate = selected;
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
