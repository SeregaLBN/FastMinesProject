using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notifier;

namespace fmg.core.img {

    /// <summary>
    /// MVC: view.
    /// Base implementation of image view.
    /// </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageModel">model data for display</typeparam>
    public abstract class ImageView<TImage, TImageModel>
                       : IImageView<TImage, TImageModel>
            where TImage : class
            where TImageModel : IImageModel
    {
        private enum EInvalidate {
            needRedraw,
            redrawing,
            redrawed
        }

        /// <summary> MVC: model </summary>
        public TImageModel Model { get; private set; }
        private TImage _image;
        private EInvalidate _invalidate = EInvalidate.needRedraw;
        protected bool Disposed { get; private set; }
        private event PropertyChangedEventHandler PropertyChangedSync;
        public  event PropertyChangedEventHandler PropertyChanged/*Async*/;
        protected readonly NotifyPropertyChanged _notifier/*Sync*/;
        private   readonly NotifyPropertyChanged _notifierAsync;

        protected ImageView(TImageModel imageModel) {
            Model = imageModel;
            _notifier      = new NotifyPropertyChanged(this, ev => PropertyChangedSync?.Invoke(this, ev), false);
            _notifierAsync = new NotifyPropertyChanged(this, ev => PropertyChanged    ?.Invoke(this, ev), true);
            this .PropertyChangedSync += OnPropertyChanged;
            Model.PropertyChanged     += OnPropertyModelChanged;
        }

        /// <summary> width and height in pixel </summary>
        public SizeDouble Size {
            get => Model.Size;
            set  { Model.Size = value; }
        }

        protected abstract TImage CreateImage();
        public TImage Image {
            get {
                if (_image == null) {
                    Image = CreateImage();
                    _invalidate = EInvalidate.needRedraw;
                }
                if (_invalidate == EInvalidate.needRedraw)
                    Draw();
                return _image;
            }
            set {
                TImage old = _image;
                if (_notifier.SetProperty(ref _image, value))
                    try {
                        (old as IDisposable)?.Dispose();
                    } catch (Exception ex) {
                        System.Diagnostics.Debug.WriteLine(ex.ToString());
                    }
            }
        }

        public virtual void Invalidate() {
            if (_invalidate == EInvalidate.redrawing)
                return;
            //if (_invalidate == EInvalidate.needRedraw)
            //   return;
            _invalidate = EInvalidate.needRedraw;

            // Уведомляю владельца класса что поменялось изображение.
            // Т.е. что нужно вызвать getImage()
            // при котором и отрисуется новое изображение (через вызов draw)
            _notifier.FirePropertyChanged(nameof(this.Image));
        }

        private void Draw() {
            System.Diagnostics.Debug.Assert(!Disposed);
            if (Disposed)
                return;
            DrawBegin();
            DrawBody();
            DrawEnd();
        }

        protected void DrawBegin() { _invalidate = EInvalidate.redrawing; }
        protected abstract void DrawBody();
        protected void DrawEnd() { _invalidate = EInvalidate.redrawed; }

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, this));
            //LoggerSimple.Put(GetType().Name + ".OnPropertyChanged: PropertyName=" + ev.PropertyName);

            // refire as async event
            _notifierAsync.FirePropertyChanged(ev);
        }
        protected virtual void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Model));
            _notifier.FirePropertyChanged(default(TImageModel), Model, nameof(this.Model));
            if (nameof(IImageModel.Size) == ev.PropertyName) {
                Image = null;
                //Invalidate();
                _notifier.FirePropertyChanged<SizeDouble>(ev, nameof(this.Size));
                _notifier.FirePropertyChanged(nameof(this.Image));
            } else {
                Invalidate();
            }
        }

        // <summary>  Dispose managed resources </summary>/
        protected virtual void Disposing() {
            _notifier.Dispose();
            _notifierAsync.Dispose();
            this .PropertyChanged -= OnPropertyChanged;
            Model.PropertyChanged -= OnPropertyModelChanged;
            Image = null;
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
