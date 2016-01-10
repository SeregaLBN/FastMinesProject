using System.Collections.ObjectModel;
using fmg.common;
using fmg.common.geom;
using fmg.data.controller.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Menu;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic skills) </summary>
   public class MosaicSkillsDataSource : BaseDataSource<MosaicSkillMenuItem, ESkillLevel> {

      protected override void FillDataSource(Collection<MosaicSkillMenuItem> dataSource) {
         foreach (var s in ESkillLevelEx.GetValues()) {
            var mi = new MosaicSkillMenuItem(s);
            mi.MosaicSkillImage.BorderColor = Color.Green;
            mi.MosaicSkillImage.RedrawInterval = 50;
            mi.MosaicSkillImage.RotateAngleDelta = 5;
            dataSource.Add(mi);
         }
      }

      protected override void OnCurrentElementChanged() {
         //OnPropertyChanged("SelectedPageType");
         //OnPropertyChanged("UnicodeChars");

         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicSkillImage;
            img.Rotate = selected;
            img.BackgroundColor = selected ? MosaicsSkillImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
            img.Padding = new Bound(selected ? 5 : 15);
         }
      }

      public override void Dispose() {
         foreach (var mi in DataSource)
            mi.Dispose();
         base.Dispose();
      }

   }
}
