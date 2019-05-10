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
        private event PropertyChangedEventHandler PropertyChangedSync;
        public  event PropertyChangedEventHandler PropertyChanged/*Async*/;
        protected readonly NotifyPropertyChanged notifier/*Sync*/;
        private   readonly NotifyPropertyChanged notifierAsync;

        protected BaseDataItem(T uniqueId) {
            this.uniqueId = uniqueId;
            notifier      = new NotifyPropertyChanged(this, false);
            notifierAsync = new NotifyPropertyChanged(this, true);
            notifier     .PropertyChanged     += OnNotifierPropertyChanged;
            notifierAsync.PropertyChanged     += OnNotifierAsyncPropertyChanged;
            this         .PropertyChangedSync += OnPropertyChanged;
        }

        public T UniqueId {
            get { return uniqueId; }
            set { notifier.SetProperty(ref uniqueId, value); }
        }

        public string Title {
            get { return title; }
            set { notifier.SetProperty(ref title, value); }
        }

        protected double Zoom() => 2;

        public virtual TImageCtrlr Entity {
            get { throw new NotImplementedException("Must be overridden"); }
            protected set {
                var old = this.entity;
                if (notifier.SetProperty(ref entity, value)) {
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
            notifierAsync.FirePropertyChanged(ev);
        }

        protected void OnControllerPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Entity));
            notifier.FirePropertyChanged(nameof(this.Entity));

            switch (ev.PropertyName) {
            case nameof(Entity.Image):
                notifier.FirePropertyChanged(nameof(this.Image));
                break;
            }
        }

        protected void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Entity.Model));

            switch (ev.PropertyName) {
            case nameof(IImageModel.Size):
                if (ev is PropertyChangedExEventArgs<SizeDouble> evx1)
                    notifier.FirePropertyChanged(ZoomSize(evx1.OldValue), ZoomSize(evx1.NewValue), nameof(this.Size));
                else
                    notifier.FirePropertyChanged(nameof(this.Size));
                break;
            case nameof(IImageModel.Padding):
                if (ev is PropertyChangedExEventArgs<BoundDouble> evx2)
                    notifier.FirePropertyChanged(ZoomPadding(evx2.OldValue), ZoomPadding(evx2.NewValue), nameof(this.Padding));
                else
                    notifier.FirePropertyChanged(nameof(this.Padding));
                break;
            }
        }

        private void OnNotifierPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, notifier));
            PropertyChangedSync?.Invoke(this, ev);
        }

        private void OnNotifierAsyncPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, notifierAsync));
            PropertyChanged/*Async*/?.Invoke(this, ev);
        }

        public override string ToString() {
            return Title;
        }

        protected virtual void Disposing() {
            notifier     .PropertyChanged -= OnNotifierPropertyChanged;
            notifierAsync.PropertyChanged -= OnNotifierAsyncPropertyChanged;
            notifier     .Dispose();
            notifierAsync.Dispose();
            this.PropertyChangedSync -= OnPropertyChanged;
            Entity = null; // call setter
            NotifyPropertyChanged.AssertCheckSubscribers(this, nameof(PropertyChangedSync));
            NotifyPropertyChanged.AssertCheckSubscribers(this);
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
