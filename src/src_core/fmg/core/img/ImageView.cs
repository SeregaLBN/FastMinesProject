using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary>
   /// MVC: view.
   /// Base implementation of image view.
   /// </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageModel">model data for display</typeparam>
   public abstract class ImageView<TImage, TImageModel>
                      : IImageView<TImage, TImageModel>, INotifyPropertyChanged
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
      public event PropertyChangedEventHandler PropertyChanged;
      protected NotifyPropertyChanged _notifier;

      protected ImageView(TImageModel imageModel) {
         _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
         Model = imageModel;
         Model.PropertyChanged += OnPropertyModelChanged;
      }

      /// <summary> width and height in pixel </summary>
      public SizeDouble Size {
         get { return Model.Size; }
         set { Model.Size = value; }
      }
      public void SetSize(double widhtAndHeight) { Size = new SizeDouble(widhtAndHeight, widhtAndHeight); }

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

      public void Invalidate() {
         if (_invalidate == EInvalidate.redrawing)
            return;
         //if (_invalidate == EInvalidate.needRedraw)
         //   return;
         _invalidate = EInvalidate.needRedraw;

         // Уведомляю владельца класса что поменялось изображение.
         // Т.е. что нужно вызвать getImage()
         // при котором и отрисуется новое изображение (через вызов draw)
         _notifier.OnPropertyChanged(nameof(this.Image));
      }

      private void Draw() {
         DrawBegin();
         DrawBody();
         DrawEnd();
      }

      protected void DrawBegin() { _invalidate = EInvalidate.redrawing; }
      protected abstract void DrawBody();
      protected void DrawEnd() { _invalidate = EInvalidate.redrawed; }

      protected void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Model));
         _notifier.OnPropertyChanged(default(TImageModel), Model, nameof(this.Model));
         if (nameof(IImageModel.Size) == ev.PropertyName) {
            Image = null;
          //Invalidate();
            _notifier.OnPropertyChanged<SizeDouble>(ev, nameof(this.Size));
            _notifier.OnPropertyChanged(nameof(this.Image));
         } else {
            Invalidate();
         }
      }

      protected virtual void Dispose(bool disposing) {
         if (disposing) {
            // Dispose managed resources
            _notifier.Dispose();
            Model.PropertyChanged -= OnPropertyModelChanged;
            Image = null;
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

      ~ImageView() {
         if (!Disposed) {
            Disposed = true;
            Dispose(false);
         }
      }

   }

}
