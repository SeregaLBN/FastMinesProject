using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Controls;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic groups) </summary>
   public class MosaicGroupsDataSource : BaseDataSource<MosaicGroupMenuItem, EMosaicGroup> {

      protected override void FillDataSource(Collection<MosaicGroupMenuItem> dataSource) {
         foreach (var g in EMosaicGroupEx.GetValues()) {
            var mi = new MosaicGroupMenuItem(g);
            dataSource.Add(mi);
         }
      }

      protected override void OnCurrentElementChanged() {
         if (CurrentElement != null)  // if unselected item
            OnPropertyChanged("SelectedPageType");
         OnPropertyChanged("UnicodeChars");

         // for one selected - start animate; for all other - stop animate
         foreach (var mi in DataSource) {
            var selected = ReferenceEquals(mi, CurrentElement);
            var img = mi.MosaicGroupImage;
            using (img.Deferring()) {
               img.PolarLights = selected;
               img.Rotate = selected;
               img.BorderColor = selected ? Color.Red : Color.Green;
               img.BackgroundColor = selected ? MosaicsGroupImg.DefaultBkColor : GraphicContext.DefaultBackgroundFillColor;
               img.Padding = new Bound(selected ? 5 : 15);
            }
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

      public Type SelectedPageType => CurrentElement?.PageType;
   }
}
