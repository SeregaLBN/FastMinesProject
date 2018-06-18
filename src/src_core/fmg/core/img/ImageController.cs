using System;
using System.ComponentModel;
using fmg.common.notyfier;
using fmg.common.geom;

namespace fmg.core.img {

   /// <summary>
   /// Image MVC: controller.
   /// Base implementation of image controller (manipulations with the image).
   /// </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
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
      protected bool Disposed { get; private set; }
      public event PropertyChangedEventHandler PropertyChanged;
      protected NotifyPropertyChanged _notifier;

      protected ImageController(TImageView imageView) {
         _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev), true);
         _imageView = imageView;
         _imageView.PropertyChanged += OnPropertyViewChanged;
      }

      protected TImageView  View  => _imageView;
      public    TImageModel Model => View.Model;
      public    TImage      Image => View.Image;
      public    SizeDouble  Size  => View.Size;

      private void OnPropertyViewChanged(object sender, PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case nameof(View.Model):
            _notifier.OnPropertyChanged<TImageModel>(ev, nameof(this.Model));
            break;
         case nameof(View.Image):
            _notifier.OnPropertyChanged<TImage>(ev, nameof(this.Image));
            break;
         case nameof(View.Size):
            _notifier.OnPropertyChanged<SizeDouble>(ev, nameof(this.Size));
            break;
         }
      }

      protected virtual void Dispose(bool disposing) {
         if (disposing) {
            // Dispose managed resources
            _imageView.PropertyChanged -= OnPropertyViewChanged;
            _imageView.Dispose();
            _notifier.Dispose();
         }

         // Dispose unmanaged resources
      }

      public void Dispose() {
         if (!Disposed) {
            Disposed = true;
            Dispose(true);
         }
         GC.SuppressFinalize(this);
      }

      ~ImageController() {
         if (!Disposed) {
            Disposed = true;
            Dispose(false);
         }
      }

   }

}
