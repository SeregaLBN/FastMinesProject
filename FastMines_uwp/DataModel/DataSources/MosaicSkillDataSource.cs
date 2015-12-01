using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.common;
using fmg.data.controller.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Presentation.Menu;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic skills) </summary>
   public class MosaicSkillDataSource : NotifyPropertyChanged, IDisposable
   {
      private readonly ObservableCollection<MosaicSkillMenuItem> _dataSource = new ObservableCollection<MosaicSkillMenuItem>();
      private MosaicSkillMenuItem _currentElement;

      public ObservableCollection<MosaicSkillMenuItem> DataSource {
         get {
            if (!_dataSource.Any()) {
               // add elements
               foreach (var s in ESkillLevelEx.GetValues()) {
                  var mi = new MosaicSkillMenuItem(s);
                  mi.MosaicSkillImage.RedrawInterval = 50;
                  mi.MosaicSkillImage.RotateAngleDelta = -5;
                  _dataSource.Add(mi);
               }
               CurrentElement = _dataSource.First();
            }
            return _dataSource;
         }
      }

      /// <summary> Selected element </summary>
      public MosaicSkillMenuItem CurrentElement {
         get { return _currentElement; }
         set {
            if (SetProperty(ref _currentElement, value)) {
               //OnPropertyChanged("SelectedPageType");
               //OnPropertyChanged("UnicodeChars");

               // for one selected- start animate; for all other - stop animate
               foreach (var mi in DataSource) {
                  var selected = ReferenceEquals(mi, value);
                  var img = mi.MosaicSkillImage;
                  img.Rotate = selected;
                  img.BkColor = selected ? MosaicsSkillImg.DefaultBkColor.ToFmColor() : GraphicContext.DefaultBackgroundFillColor;
                  img.Padding = selected ? 5 : 15;
               }
            }
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
