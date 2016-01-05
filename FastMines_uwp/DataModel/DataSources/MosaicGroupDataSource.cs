using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Menu;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic groups) </summary>
   public class MosaicGroupDataSource : NotifyPropertyChanged, IDisposable
   {
      private readonly ObservableCollection<MosaicGroupMenuItem> _dataSource = new ObservableCollection<MosaicGroupMenuItem>();
      private MosaicGroupMenuItem _currentElement;

      public ObservableCollection<MosaicGroupMenuItem> DataSource {
         get {
            if (!_dataSource.Any()) {
               // add elements
               foreach (var g in EMosaicGroupEx.GetValues()) {
                  _dataSource.Add(new MosaicGroupMenuItem(g));
               }
               CurrentElement = _dataSource.First();
            }
            return _dataSource;
         }
      }

      /// <summary> Selected element </summary>
      public MosaicGroupMenuItem CurrentElement {
         get { return _currentElement; }
         set {
            if (SetProperty(ref _currentElement, value)) {
               OnPropertyChanged("SelectedPageType");
               OnPropertyChanged("UnicodeChars");

               // for one selected- start animate; for all other - stop animate
               foreach (var mi in DataSource) {
                  var selected = ReferenceEquals(mi, value);
                  var img = mi.MosaicGroupImage;
                  img.PolarLights = selected;
                  img.Rotate = selected;
                  img.BkColor = selected ? MosaicsGroupImg.DefaultBkColor.ToFmColor() : GraphicContext.DefaultBackgroundFillColor;
                  img.Padding = new Bound(selected ? 5 : 15);
               }
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

      public Type SelectedPageType {
         get {
            return _currentElement?.PageType;
         }
         set {
            // select associated menu item
            CurrentElement = (value == null)
               ? null
               : DataSource.FirstOrDefault(m => m.PageType == value);
         }
      }

      public int ImageSize {
         get { return DataSource.First().ImageSize; }
         set {
            var old = ImageSize;
            foreach (var mi in DataSource) {
               mi.ImageSize = value;
            }
            if (old != value)
               OnPropertyChanged(this, new PropertyChangedExEventArgs<int>("ImageSize", value, old));
         }
      }

      public void Dispose() {
         foreach (var mi in _dataSource)
            mi.Dispose();
         _dataSource.Clear();
      }

   }
}
