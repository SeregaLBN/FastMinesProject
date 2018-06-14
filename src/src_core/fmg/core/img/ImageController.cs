using System;
using System.ComponentModel;
using fmg.common;
using fmg.common.notyfier;
using fmg.common.geom;

namespace fmg.core.img {

   /// <summary>
   /// Image MVC: controller
   /// Base implementation of image controller (manipulations with the image).
   /// </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageView">image view</typeparam>
   /// <typeparam name="TImageModel">image model</typeparam>
   public abstract class ImageController<TImage, TImageView, TImageModel>
                  : Disposable, INotifyPropertyChanged
      where TImage : class
      where TImageView : IImageView<TImage, TImageModel>
      where TImageModel : IImageModel
   {

      /// <summary> MVC: view </summary>
      private readonly TImageView _imageView;
      public event PropertyChangedEventHandler PropertyChanged;
      protected NotifyPropertyChanged _notifier;

      protected ImageController(TImageView imageView) {
         _notifier = new NotifyPropertyChanged(this, true);
         _notifier.PropertyChanged += OnPropertyChanged;
         _imageView = imageView;
         _imageView.PropertyChanged += OnPropertyViewChanged;
      }

      protected TImageView  View  => _imageView;
      public    TImage      Image => View.Image;
      public    TImageModel Model => View.Model;
      public    SizeDouble  Size  => View.Size;

      private void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, this));
         PropertyChanged?.Invoke(this, ev);
      }

      private void OnPropertyViewChanged(object sender, PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case nameof(View.Image):
            _notifier.OnPropertyChanged<TImage>(ev, nameof(this.Image));
            break;
         case nameof(View.Size):
            _notifier.OnPropertyChanged<SizeDouble>(ev, nameof(this.Size));
            break;
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            _imageView.PropertyChanged -= OnPropertyViewChanged;
            _imageView.Dispose();
            _notifier.PropertyChanged -= OnPropertyChanged;
            _notifier.Dispose();
         }
      }

   }

}