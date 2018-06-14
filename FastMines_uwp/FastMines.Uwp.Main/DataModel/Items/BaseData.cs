using Microsoft.Graphics.Canvas;
using System.ComponentModel;
using fmg.common.notyfier;
using fmg.common.geom;
using fmg.core.img;

namespace fmg.DataModel.Items {

   /// <summary> Base item class for <see cref="MosaicDataItem"/> and <see cref="MosaicGroupDataItem"/> and <see cref="MosaicSkillDataItem"/> </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class BaseData<T, TImage> : NotifyPropertyChanged
      where TImage : ImageModel<CanvasBitmap>
   {
      protected BaseData(T uniqueId) {
         UniqueId = uniqueId;
      }

      private T _uniqueId;
      public T UniqueId {
         get { return _uniqueId; }
         set { SetProperty(ref _uniqueId, value); }
      }

      private string _title = string.Empty;
      public string Title {
         get { return _title; }
         set { SetProperty(ref _title, value); }
      }

      public abstract int Zoom();

      public abstract TImage Image { get; protected set; }

      public Size ImageSize {
         get {
            var size = Image.Size;
            var zoom = Zoom();
            return new Size(size.Width / zoom, size.Height / zoom);
         }
         set {
            var zoom = Zoom();
            Image.Size = new Size(value.Width * zoom, value.Height * zoom);
         }
      }

      public Bound ImagePadding {
         get {
            var pad = Image.Padding;
            var zoom = Zoom();
            return new Bound(pad.Left / zoom, pad.Top / zoom, pad.Right / zoom, pad.Bottom / zoom);
         }
         set {
            var zoom = Zoom();
            Image.Padding = new Bound(value.Left * zoom, value.Top * zoom, value.Right * zoom, value.Bottom * zoom);
         }
      }

      protected virtual void OnImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is TImage);
         switch (ev.PropertyName) {
         case nameof(Image.Size):
            OnPropertyChanged<Size>(ev, nameof(this.ImageSize));
            break;
         case nameof(Image.Image):
            OnPropertyChanged<TImage>(ev, nameof(this.Image));
            break;
         case nameof(Image.Padding):
            OnPropertyChanged<Bound>(ev, nameof(this.ImagePadding));
            break;
         }
      }

      public override string ToString() {
         return Title;
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         Image = null; // call setter
      }

   }

}
