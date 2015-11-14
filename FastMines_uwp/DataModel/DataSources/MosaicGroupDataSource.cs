using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.common;
using fmg.core.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Menu;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items </summary>
   public class MosaicGroupDataSource : NotifyPropertyChanged, IDisposable
   {
      private readonly ObservableCollection<MosaicGroupMenuItem> _menuItems = new ObservableCollection<MosaicGroupMenuItem>();
      private MosaicGroupMenuItem _selectedMenuItem;

      public MosaicGroupMenuItem SelectedMenuItem
      {
         get { return _selectedMenuItem; }
         set
         {
            if (SetProperty(ref _selectedMenuItem, value))
            {
               OnPropertyChanged("SelectedPageType");
               OnPropertyChanged("UnicodeChars");

               //var marginUnselected = new Bound(20, 5, 20, 5);
               //var marginSelected = new Bound(0, 0, 0, 0);

               // for one selected- start animate; for all other - stop animate
               foreach (var mi in MenuItems) {
                  var selected = ReferenceEquals(mi, value);
                  var img = mi.MosaicGroupImage;
                  img.PolarLights = selected;
                  img.Rotate = selected;
                  img.BkColor = selected ? MosaicsGroupImg.DefaultBkColor.ToFmColor() : GraphicContext.DefaultBackgroundFillColor;
                  //img.Margin = selected ? marginSelected : marginUnselected;
                  img.Padding = selected ? 5 : 15;
               }
            }
         }
      }

      public string UnicodeChars
      {
         get
         {
            var smi = SelectedMenuItem;
            return string.Join(" ", MenuItems.Select(mi =>
            {
               var selected = (smi != null) && (mi.MosaicGroupImage.MosaicGroup == smi.MosaicGroupImage.MosaicGroup);
               return mi.MosaicGroupImage.MosaicGroup.UnicodeChar(selected);
            }));
         }
      }

      public Type SelectedPageType
      {
         get
         {
            return _selectedMenuItem?.PageType;
         }
         set
         {
            // select associated menu item
            SelectedMenuItem = (value == null)
               ? null
               : MenuItems.FirstOrDefault(m => m.PageType == value);
         }
      }

      public ObservableCollection<MosaicGroupMenuItem> MenuItems
      {
         get
         {
            if (!_menuItems.Any()) {
               // add elements
               var groups = EMosaicGroupEx.GetValues();
               foreach (var g in groups) {
                  _menuItems.Add(new MosaicGroupMenuItem(g));
               }
               SelectedMenuItem = _menuItems.First();
            }
            return _menuItems;
         }
      }

      public int ImageSize {
         get { return MenuItems.First().ImageSize; }
         set {
            foreach (var mi in MenuItems) {
               mi.ImageSize = value;
            }
         }
      }

      public void Dispose()
      {
         foreach (var mi in _menuItems)
            mi.Dispose();
         _menuItems.Clear();
      }

   }
}
