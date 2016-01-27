using System.Collections.ObjectModel;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
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
               Reset();
               OnPropertyChanged("DataSource");
            }
         }
      }

      protected override void FillDataSource(Collection<MosaicTailItem> dataSource) {
         foreach (var s in CurrentGroup.GetBind()) {
            var mi = new MosaicTailItem(s) {
               MosaicImage = {
                  //RedrawInterval = 50,
                  //RotateAngleDelta = 5
               }
            };
            dataSource.Add(mi);
         }
      }

      protected override void OnCurrentElementChanged() {
         //LoggerSimple.Put("MosaicsDataSource::OnCurrentElementChanged: CurrentElement=" + CurrentElement?.MosaicType);
         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicImage;
            using (img.DeferredLock) {
               //img.Rotate = selected;
               img.BorderColor = selected ? Color.White : Color.Dark;
               img.BackgroundColor = selected ? MosaicsSkillImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
               img.Padding = new Bound(selected ? 5 : 15);
            }
         }
      }

   }
}
