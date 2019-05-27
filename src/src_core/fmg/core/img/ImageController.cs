using System;
using System.ComponentModel;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;

namespace Fmg.Core.Img {

    /// <summary>
    /// Image MVC: controller.
    /// Base implementation of image controller (manipulations with the image).
    /// </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageView">image view</typeparam>
    /// <typeparam name="TImageModel">image model</typeparam>
    public abstract class ImageController<TImage, TImageView, TImageModel>
                       : IImageController<TImage, TImageView, TImageModel>
        where TImage : class
        where TImageView : IImageView<TImage, TImageModel>
        where TImageModel : IImageModel
    {
        /// <summary> MVC: view </summary>
        private readonly TImageView _imageView;
        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier;
        public bool Disposed { get; private set; }

        protected ImageController(TImageView imageView) {
            _notifier = new NotifyPropertyChanged(this, true);
            _imageView = imageView;
            _imageView.PropertyChanged += OnPropertyViewChanged;
        }

        protected TImageView View => _imageView;
        public TImageModel Model => View.Model;
        public TImage      Image => View.Image;
        public SizeDouble  Size  => View.Size;

        protected virtual void OnPropertyViewChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(View.Model):
                _notifier.FirePropertyChanged<TImageModel>(ev, nameof(this.Model));
                break;
            case nameof(View.Image):
                _notifier.FirePropertyChanged<TImage>(ev, nameof(this.Image));
                break;
            case nameof(View.Size):
                _notifier.FirePropertyChanged<SizeDouble>(ev, nameof(this.Size));
                break;
            }
        }

        // <summary>  Dispose managed resources </summary>/
        protected virtual void Disposing() {
            _imageView.PropertyChanged -= OnPropertyViewChanged;
            _notifier.Dispose();
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
