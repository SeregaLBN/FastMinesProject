using System;
using System.Collections.ObjectModel;
using System.Linq;
using FastMines.DataModel.Items;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.DataSources
{
   /// <summary> base DataSource menu items </summary>
   public abstract class BaseDataSource<TItem, T> : NotifyPropertyChanged, IDisposable
      where TItem : BaseData<T>
   {
      private readonly ObservableCollection<TItem> _dataSource = new ObservableCollection<TItem>();
      private TItem _currentElement;

      public ObservableCollection<TItem> DataSource {
         get {
            if (!_dataSource.Any()) {
               FillDataSource(_dataSource);
               CurrentElement = _dataSource.First();
            }
            return _dataSource;
         }
      }

      protected abstract void FillDataSource(Collection<TItem> dataSource);

      /// <summary> Selected element </summary>
      public TItem CurrentElement {
         get { return _currentElement; }
         set {
            if (SetProperty(ref _currentElement, value)) {
               OnCurrentElementChanged();
            }
         }
      }

      protected abstract void OnCurrentElementChanged();

      public int ImageSize {
         get { return DataSource.First().ImageSize; }
         set {
            var old = ImageSize;
            foreach (var mi in DataSource) {
               mi.ImageSize = value;
            }
            if (old != value)
               OnPropertyChanged(this, new PropertyChangedExEventArgs<int>(value, old));
         }
      }

      public void Dispose() {
         foreach (var mi in DataSource)
            mi.Dispose();
         _dataSource.Clear();
      }

   }
}
