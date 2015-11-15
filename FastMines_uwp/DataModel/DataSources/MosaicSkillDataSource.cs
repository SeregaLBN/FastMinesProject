using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.data.controller.types;
using FastMines.Presentation.Menu;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.DataSources
{
   /// <summary> DataSource menu items (mosaic skills) </summary>
   public class MosaicSkillDataSource : NotifyPropertyChanged, IDisposable
   {
      private readonly ObservableCollection<MosaicSkillMenuItem> _dataSource = new ObservableCollection<MosaicSkillMenuItem>();
      private MosaicSkillMenuItem _currentElement;

      public ObservableCollection<MosaicSkillMenuItem> DataSource
      {
         get
         {
            if (!_dataSource.Any()) {
               // add elements
               foreach (var s in ESkillLevelEx.GetValues()) {
                  _dataSource.Add(new MosaicSkillMenuItem(s));
               }
               CurrentElement = _dataSource.First();
            }
            return _dataSource;
         }
      }

      public MosaicSkillMenuItem CurrentElement
      {
         get { return _currentElement; }
         set { SetProperty(ref _currentElement, value); }
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

      public void Dispose()
      {
         foreach (var mi in _dataSource)
            mi.Dispose();
         _dataSource.Clear();
      }

   }
}
