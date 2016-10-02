using System;
using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.types;
using MosaicsGroupCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasBmp;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic group item for data model </summary>
   public class MosaicGroupDataItem : BaseData<EMosaicGroup?, MosaicsGroupCanvasBmp> {

      private const int ZoomKoef = 2;

      public MosaicGroupDataItem(EMosaicGroup? eMosaicGroup)
         : base(eMosaicGroup)
      {
         Title = eMosaicGroup?.GetDescription();
      }

      public EMosaicGroup? MosaicGroup => UniqueId;

      private MosaicsGroupCanvasBmp _mosaicGroupImg;
      public override MosaicsGroupCanvasBmp Image {
         get {
            if (_mosaicGroupImg == null) {
               // call this setter
               Image = new MosaicsGroupCanvasBmp(MosaicGroup, CanvasDevice.GetSharedDevice()) {
                  BorderWidth = 3,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
               };
            }
            return _mosaicGroupImg;
         }
         protected set {
            var old = _mosaicGroupImg;
            if (SetProperty(ref _mosaicGroupImg, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnMosaicsGroupImgPropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnMosaicsGroupImgPropertyChanged;
               }
               OnSelfPropertyChanged(nameof(this.Image));
            }
         }
      }

      private Size _imageSize = new Size(MosaicsGroupCanvasBmp.DefaultImageSize, MosaicsGroupCanvasBmp.DefaultImageSize);
      public override Size ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               Image.Size = new Size(_imageSize.Width * ZoomKoef, _imageSize.Height * ZoomKoef);
            }
         }
      }

      private Bound _imagePadding = new Bound(MosaicsGroupCanvasBmp.DefaultPaddingInt);
      public Bound ImagePadding {
         get { return _imagePadding; }
         set {
            if (SetProperty(ref _imagePadding, value)) {
               Image.Padding = new Bound(_imagePadding.Left * ZoomKoef, _imagePadding.Top * ZoomKoef, _imagePadding.Right * ZoomKoef, _imagePadding.Bottom * ZoomKoef);
            }
         }
      }

      private void OnMosaicsGroupImgPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is MosaicsGroupCanvasBmp);
         switch (ev.PropertyName) {
         case nameof(MosaicsGroupCanvasBmp.Size):
            OnSelfPropertyChanged<Size>(ev, nameof(this.ImageSize));
            break;
         case nameof(MosaicsGroupCanvasBmp.Image):
            OnSelfPropertyChanged<MosaicsGroupCanvasBmp>(ev, nameof(this.Image));
            break;
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         Image = null; // call setter
      }

   }
}
