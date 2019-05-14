using System;
using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.notifier;
using fmg.common.geom;
using fmg.core.img;

namespace fmg.DataModel.Items {

    /// <summary> Base item class for <see cref="MosaicDataItem"/> and <see cref="MosaicGroupDataItem"/> and <see cref="MosaicSkillDataItem"/> </summary>
    [Windows.Foundation.Metadata.WebHostHidden]
    public abstract class BaseDataItem<T, TImageModel, TImageView, TImageCtrlr> : INotifyPropertyChanged, IDisposable
        where TImageModel : IAnimatedModel
        where TImageView  : IImageView<CanvasBitmap, TImageModel>
        where TImageCtrlr : ImageController<CanvasBitmap, TImageView, TImageModel>
    {

        private T uniqueId;
        protected TImageCtrlr entity;
        private string title = "";
        public bool Disposed { get; private set; }
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

        protected BaseDataItem(T uniqueId) {
            this.uniqueId = uniqueId;
            _notifier      = new NotifyPropertyChanged(this, false);
            _notifierAsync = new NotifyPropertyChanged(this, true);
            this.PropertyChangedSync += OnPropertyChanged;
        }

        public T UniqueId {
            get { return uniqueId; }
            set { _notifier.SetProperty(ref uniqueId, value); }
        }

        public string Title {
            get { return title; }
            set { _notifier.SetProperty(ref title, value); }
        }

        protected double Zoom() => 2;

        public virtual TImageCtrlr Entity {
            get { throw new NotImplementedException("Must be overridden"); }
            protected set {
                var old = this.entity;
                if (_notifier.SetProperty(ref entity, value)) {
                    if (old != null) {
                        old      .PropertyChanged -= OnControllerPropertyChanged;
                        old.Model.PropertyChanged -= OnModelPropertyChanged;
                        old.Dispose();
                    }
                    if (entity != null) {
                        entity      .PropertyChanged += OnControllerPropertyChanged;
                        entity.Model.PropertyChanged += OnModelPropertyChanged;
                    }
                }
            }
        }

        public CanvasBitmap Image {
            get {
                if (Disposed) {
                    System.Diagnostics.Debug.Assert(false, "Object already disposed! Return faked image...");
                    return null;
                }

                return Entity.Image;
            }
        }

        public SizeDouble Size {
            get {
                var size = Entity.Size;
                var zoom = Zoom();
                return new SizeDouble(size.Width / zoom, size.Height / zoom);
            }
            set {
                Entity.Model.Size = ZoomSize(value);
            }
        }

        public BoundDouble Padding {
            get {
                var pad = Entity.Model.Padding;
                var zoom = Zoom();
                return new BoundDouble(pad.Left / zoom, pad.Top / zoom, pad.Right / zoom, pad.Bottom / zoom);
            }
            set {
                Entity.Model.Padding = ZoomPadding(value);
            }
        }

        SizeDouble ZoomSize(SizeDouble size) {
            double zoom = Zoom();
            return new SizeDouble(size.Width * zoom, size.Height * zoom);
        }

        internal BoundDouble ZoomPadding(BoundDouble pad) {
            double zoom = Zoom();
            return new BoundDouble(pad.Left * zoom, pad.Top * zoom, pad.Right * zoom, pad.Bottom * zoom);
        }

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire as async event
            _notifierAsync.FirePropertyChanged(ev);
        }

        protected void OnControllerPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Entity));
            _notifier.FirePropertyChanged(nameof(this.Entity));

            switch (ev.PropertyName) {
            case nameof(Entity.Image):
                _notifier.FirePropertyChanged(nameof(this.Image));
                break;
            }
        }

        protected void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Entity.Model));

            switch (ev.PropertyName) {
            case nameof(IImageModel.Size):
                if (ev is PropertyChangedExEventArgs<SizeDouble> evx1)
                    _notifier.FirePropertyChanged(ZoomSize(evx1.OldValue), ZoomSize(evx1.NewValue), nameof(this.Size));
                else
                    _notifier.FirePropertyChanged(nameof(this.Size));
                break;
            case nameof(IImageModel.Padding):
                if (ev is PropertyChangedExEventArgs<BoundDouble> evx2)
                    _notifier.FirePropertyChanged(ZoomPadding(evx2.OldValue), ZoomPadding(evx2.NewValue), nameof(this.Padding));
                else
                    _notifier.FirePropertyChanged(nameof(this.Padding));
                break;
            }
        }

        public override string ToString() {
            return Title;
        }

        protected virtual void Disposing() {
            _notifier     .Dispose();
            _notifierAsync.Dispose();
            this.PropertyChangedSync -= OnPropertyChanged;
            Entity = null; // call setter
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
