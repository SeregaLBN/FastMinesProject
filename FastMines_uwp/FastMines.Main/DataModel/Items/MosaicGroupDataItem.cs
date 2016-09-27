using System;
using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.common.notyfier;
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
               var tmp = new MosaicsGroupCanvasBmp(MosaicGroup, CanvasDevice.GetSharedDevice()) {
                  Size = new Size(ImageSize.Width * ZoomKoef, ImageSize.Height * ZoomKoef),
                  BorderWidth = 3,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
                  //, OnlySyncDraw = true
               };
               System.Diagnostics.Debug.Assert(tmp.Size.Width == ImageSize.Width * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Size.Height == ImageSize.Height * ZoomKoef);
               Image = tmp; // call this setter
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

      private void OnMosaicsGroupImgPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is MosaicsGroupCanvasBmp);
         var pn = ev.PropertyName;
         if (pn == nameof(MosaicsGroupCanvasBmp.Image)) {
            // ! notify parent container
            var ev2 = ev as PropertyChangedExEventArgs<CanvasBitmap>;
            if (ev2 == null)
               OnSelfPropertyChanged(nameof(this.Image));
            else
               OnSelfPropertyChanged(new PropertyChangedExEventArgs<CanvasBitmap>(ev2.NewValue, ev2.OldValue, nameof(this.Image)));
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
