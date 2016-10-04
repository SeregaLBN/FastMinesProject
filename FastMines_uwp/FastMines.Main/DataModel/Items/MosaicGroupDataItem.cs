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

      public override Size ImageSize {
         get {
            var size = Image.Size;
            return new Size(size.Width / ZoomKoef, size.Height / ZoomKoef);
         }
         set {
            Image.Size = new Size(value.Width * ZoomKoef, value.Height * ZoomKoef);
         }
      }

      public Bound ImagePadding {
         get {
            var pad = Image.Padding;
            return new Bound(pad.Left / ZoomKoef, pad.Top / ZoomKoef, pad.Right / ZoomKoef, pad.Bottom / ZoomKoef);
         }
         set {
            Image.Padding = new Bound(value.Left * ZoomKoef, value.Top * ZoomKoef, value.Right * ZoomKoef, value.Bottom * ZoomKoef);
         }
      }

      public Bound ImagePaddingBurgerMenu {
         get {
            var pad = Image.PaddingBurgerMenu;
            return new Bound(pad.Left / ZoomKoef, pad.Top / ZoomKoef, pad.Right / ZoomKoef, pad.Bottom / ZoomKoef);
         }
         set {
            Image.PaddingBurgerMenu = new Bound(value.Left * ZoomKoef, value.Top * ZoomKoef, value.Right * ZoomKoef, value.Bottom * ZoomKoef);
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
         case nameof(MosaicsGroupCanvasBmp.Padding):
            OnSelfPropertyChanged<Bound>(ev, nameof(this.ImagePadding));
            break;
         case nameof(MosaicsGroupCanvasBmp.PaddingBurgerMenu):
            OnSelfPropertyChanged<Bound>(ev, nameof(this.ImagePaddingBurgerMenu));
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
