using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.ObjectModel;
using Microsoft.Graphics.Canvas;
using Fmg.DataModel.Items;
using Fmg.Common.Notifier;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Uwp.Utils;

namespace Fmg.DataModel.DataSources {

    /// <summary> Base container for image items </summary>
    public abstract class BaseDataSource<THeader, THeaderId, THeaderModel, THeaderView, THeaderCtrlr,
                                         TItem  ,   TItemId,   TItemModel,   TItemView,   TItemCtrlr>
            : INotifyPropertyChanged, IDisposable

        where THeader : BaseDataItem<THeaderId, THeaderModel, THeaderView, THeaderCtrlr>
        where TItem   : BaseDataItem<  TItemId,   TItemModel,   TItemView,   TItemCtrlr>
        where THeaderModel : IAnimatedModel
        where THeaderView  : IImageView<CanvasBitmap, THeaderModel>
        where THeaderCtrlr : ImageController<CanvasBitmap, THeaderView, THeaderModel>
        where TItemModel : IAnimatedModel
        where TItemView  : IImageView<CanvasBitmap, TItemModel>
        where TItemCtrlr : ImageController<CanvasBitmap, TItemView, TItemModel>
    {
        /// <summary> Images that describes this data source </summary>
        protected THeader header;
        /// <summary> Data source - images that describes the elements </summary>
        protected readonly ObservableCollection<TItem> dataSource = new ObservableCollection<TItem>();
        /// <summary> Current item index in {@link #dataSource} </summary>
        protected int currentItemPos = NOT_SELECTED_POS;

        private const int NOT_SELECTED_POS = -1;

        protected bool Disposed { get; private set; }
        private event PropertyChangedEventHandler PropertyChangedSync {
            add    { _notifier/*Sync*/.PropertyChanged += value;  }
            remove { _notifier/*Sync*/.PropertyChanged -= value;  }
        }
        public event PropertyChangedEventHandler PropertyChanged/*Async*/ {
            add    { _notifierAsync.PropertyChanged += value;  }
            remove { _notifierAsync.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier/*Sync*/;
        private   readonly NotifyPropertyChanged _notifierAsync;

        protected BaseDataSource() {
            _notifier      = new NotifyPropertyChanged(this, false);
            _notifierAsync = new NotifyPropertyChanged(this, true);
            this.PropertyChangedSync += OnPropertyChanged;
        }

        /// <summary> the top item that this data source describes </summary>
        public abstract THeader Header { get; }

        /// <summary> list of items </summary>
        public abstract ObservableCollection<TItem> DataSource { get; }

        /// <summary> Selected element </summary>
        public TItem CurrentItem {
            get {
                var pos = CurrentItemPos;
                if (pos < 0)
                    return null;
                return DataSource[pos];
            }
            set {
                //LoggerSimple.Put("> " + this.GetType().Name + "::" + nameof(CurrentItem) + ": value=" + ((value==null) ? "null" : value.ToString()));
                CurrentItemPos = DataSource.IndexOf(value);
            }
        }

        /// <summary> Selected index of element </summary>
        public int CurrentItemPos {
            get { return currentItemPos; }
            set {
                //LoggerSimple.Put("> " + this.GetType().Name + "::" + nameof(CurrentItemPos) + ": value=" + value);
                if ((value < 0) || (value >= DataSource.Count)) {
                    if (value != NOT_SELECTED_POS)
                        throw new ArgumentException("Illegal index of value=" + value);
                }
                if (value == currentItemPos)
                    return;
                _notifier.SetProperty(ref this.currentItemPos, value);
            }
        }

        public SizeDouble ImageSize {
            get { return DataSource.Select(x => x.Size).FirstOrDefault(); }
            set {
                var old = ImageSize;
                foreach (var mi in DataSource)
                    mi.Size = value;
                if (old != value)
                    _notifier.FirePropertyChanged(old, value);
            }
        }

        /// <summary> for one selected - start animate; for all other - stop animate </summary>
        protected abstract void OnCurrentItemChanged();

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire as async event
            _notifierAsync.FirePropertyChanged(ev);

            switch (ev.PropertyName) {
            case nameof(this.CurrentItemPos):
                OnCurrentItemChanged();
                _notifier.FirePropertyChanged(null, CurrentItem, nameof(CurrentItem));
                break;
            }
        }

        protected virtual void Disposing() {
            header?.Dispose();
            foreach (var item in dataSource)
                item.Dispose();
            dataSource.Clear();

            //CurrentItem = null;

            this.PropertyChangedSync -= OnPropertyChanged;
            _notifier     .Dispose();
            _notifierAsync.Dispose();
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;
            Disposing();
            GC.SuppressFinalize(this);
        }

    }

}
