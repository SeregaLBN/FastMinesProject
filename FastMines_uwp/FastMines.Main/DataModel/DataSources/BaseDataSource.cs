using System;
using System.Collections.ObjectModel;
using System.Linq;
using fmg.DataModel.Items;
using fmg.common.notyfier;

namespace fmg.DataModel.DataSources
{
   /// <summary> base DataSource menu items </summary>
   public abstract class BaseDataSource<TItem, T> : NotifyPropertyChanged, IDisposable
      where TItem : BaseData<T>
   {
      private readonly ObservableCollection<TItem> _dataSource = new ObservableCollection<TItem>();
      private TItem _currentElement;

      protected ObservableCollection<TItem> DataSourceInternal => _dataSource;
      public ObservableCollection<TItem> DataSource {
         get {
            if (!_dataSource.Any()) {
               FillDataSource();
            }
            return _dataSource;
         }
      }

      protected virtual void FillDataSource() {
         OnPropertyChanged("DataSource");
      }

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

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            CurrentElement = null;
            foreach (var mi in _dataSource)
               mi.Dispose();
            _dataSource.Clear();
         }
      }

   }

}
