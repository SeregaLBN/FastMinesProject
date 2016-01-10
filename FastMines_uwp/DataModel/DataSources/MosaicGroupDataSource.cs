using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Menu;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic groups) </summary>
   public class MosaicGroupDataSource : BaseDataSource<MosaicGroupMenuItem, EMosaicGroup> {

      protected override void FillDataSource(Collection<MosaicGroupMenuItem> dataSource) {
         foreach (var g in EMosaicGroupEx.GetValues()) {
            dataSource.Add(new MosaicGroupMenuItem(g));
         }
      }

      protected override void OnCurrentElementChanged() {
         OnPropertyChanged("SelectedPageType");
         OnPropertyChanged("UnicodeChars");

         // for one selected- start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicGroupImage;
            img.PolarLights = selected;
            img.Rotate = selected;
            img.BackgroundColor = selected ? MosaicsGroupImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
            img.Padding = new Bound(selected ? 5 : 15);
         }
      }

      public string UnicodeChars {
         get {
            var smi = CurrentElement;
            return string.Join(" ", DataSource.Select(mi => {
               var selected = (smi != null) && (mi.MosaicGroupImage.MosaicGroup == smi.MosaicGroupImage.MosaicGroup);
               return mi.MosaicGroupImage.MosaicGroup.UnicodeChar(selected);
            }));
         }
      }

      public Type SelectedPageType {
         get {
            return CurrentElement?.PageType;
         }
         set {
            // select associated menu item
            CurrentElement = (value == null)
               ? null
               : DataSource.FirstOrDefault(m => m.PageType == value);
         }
      }

      public override void Dispose() {
         foreach (var mi in DataSource)
            mi.Dispose();
         base.Dispose();
      }

   }
}
