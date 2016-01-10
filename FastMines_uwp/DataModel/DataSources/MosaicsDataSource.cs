using System.Collections.ObjectModel;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.DataModel.Items;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource mosaics items </summary>
   public class MosaicsDataSource : BaseDataSource<MosaicDataItem, EMosaic> {

      private EMosaicGroup _currentGroup;
      public EMosaicGroup CurrentGroup {
         get { return _currentGroup; }
         set {
            if (SetProperty(ref _currentGroup, value)) {
               Reset();
               OnPropertyChanged("DataSource");
            }
         }
      }

      protected override void FillDataSource(Collection<MosaicDataItem> dataSource) {
         foreach (var s in CurrentGroup.GetBind()) {
            var mi = new MosaicDataItem(s);
            mi.MosaicImage.BorderColor = Color.Green;
            //mi.MosaicImage.RedrawInterval = 50;
            //mi.MosaicImage.RotateAngleDelta = 5;
            dataSource.Add(mi);
         }
      }

      protected override void OnCurrentElementChanged() {
         //OnPropertyChanged("SelectedPageType");
         //OnPropertyChanged("UnicodeChars");

         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicImage;
            //img.Rotate = selected;
            img.BackgroundColor = selected ? MosaicsSkillImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
            img.Padding = new Bound(selected ? 5 : 15);
         }
      }

   }
}
