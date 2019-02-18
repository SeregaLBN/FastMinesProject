using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.ObjectModel;
using Microsoft.Graphics.Canvas;
using fmg.DataModel.Items;
using fmg.common.notyfier;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.DataModel.DataSources {

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
        private event PropertyChangedEventHandler PropertyChangedSync;
        public  event PropertyChangedEventHandler PropertyChanged/*Async*/;
        protected readonly NotifyPropertyChanged notifier/*Sync*/;
        private   readonly NotifyPropertyChanged notifierAsync;

        static BaseDataSource() {
            StaticInitializer.Init();
        }

        protected BaseDataSource() {
            notifier      = new NotifyPropertyChanged(this, ev => PropertyChangedSync?.Invoke(this, ev), false);
            notifierAsync = new NotifyPropertyChanged(this, ev => PropertyChanged    ?.Invoke(this, ev), true);
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
            set { CurrentItemPos = DataSource.IndexOf(value); }
        }

        /// <summary> Selected index of element </summary>
        public int CurrentItemPos {
            get { return currentItemPos; }
            set {
                if ((value < 0) || (value >= DataSource.Count)) {
                    if (value != NOT_SELECTED_POS)
                        throw new ArgumentException("Illegal index of value=" + value);
                }
                if (value == currentItemPos)
                    return;
                notifier.SetProperty(ref this.currentItemPos, value);
            }
        }

        public SizeDouble ImageSize {
            get { return DataSource.First().Size; }
            set {
                var old = ImageSize;
                foreach (var mi in DataSource) {
                    mi.Size = value;
                }
                if (old != value) {
                    notifier.FirePropertyChanged(old, value);
                }
            }
        }

        /// <summary> for one selected - start animate; for all other - stop animate </summary>
        protected abstract void OnCurrentItemChanged();

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire as async event
            notifierAsync.FirePropertyChanged(ev);

            switch (ev.PropertyName) {
            case nameof(this.CurrentItemPos):
                OnCurrentItemChanged();
                notifier.FirePropertyChanged(nameof(CurrentItem));
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
            notifier.Dispose();
            notifierAsync.Dispose();
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
