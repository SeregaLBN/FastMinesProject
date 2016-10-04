using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.types;
using fmg.data.controller.types;
using MosaicsCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasBmp;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic item for data model </summary>
   public class MosaicDataItem : BaseData<EMosaic, MosaicsCanvasBmp> {
      private const int ZoomKoef = 2;

      public MosaicDataItem(EMosaic mosaicType) : base(mosaicType) {
         Title = mosaicType.GetDescription(false);
      }

      public EMosaic MosaicType => UniqueId;

      private ESkillLevel _skillLevel;
      public ESkillLevel SkillLevel {
         get { return _skillLevel; }
         set {
            if (SetProperty(ref _skillLevel, value)) {
               Image.SizeField = MosaicType.SizeTileField(value);
            }
         }
      }

      private MosaicsCanvasBmp _mosaicImg;
      public override MosaicsCanvasBmp Image {
         get {
            if (_mosaicImg == null) {
               var sizeField = MosaicType.SizeTileField(SkillLevel);
               var tmp = new MosaicsCanvasBmp(MosaicType, sizeField, CanvasDevice.GetSharedDevice()) {
                  PaddingInt = 5 * ZoomKoef,
                  RotateMode = MosaicsCanvasBmp.ERotateMode.SomeCells,
                  //BackgroundColor = MosaicsCanvasBmp.DefaultBkColor,
                  BorderWidth = 3 * ZoomKoef//,
                  //RotateAngle = 45 * new Random(Guid.NewGuid().GetHashCode()).Next(7)
               };
               //var bmp = tmp.Image;
               //System.Diagnostics.Debug.Assert(bmp.PixelWidth == ImageSize * ZoomKoef);
               //System.Diagnostics.Debug.Assert(bmp.PixelHeight == ImageSize * ZoomKoef);
               Image = tmp; // call this setter
            }
            return _mosaicImg;
         }
         protected set {
            var old = _mosaicImg;
            if (SetProperty(ref _mosaicImg, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnMosaicImagePropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnMosaicImagePropertyChanged;
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

      private void OnMosaicImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put(GetType().Name+"::OnPropertyChanged: " + ev.PropertyName);
         System.Diagnostics.Debug.Assert(sender is MosaicsCanvasBmp);
         switch (ev.PropertyName) {
         case nameof(MosaicsCanvasBmp.Size):
            OnSelfPropertyChanged<Size>(ev, nameof(this.ImageSize));
            break;
         case nameof(MosaicsCanvasBmp.Image):
            OnSelfPropertyChanged<MosaicsCanvasBmp>(ev, nameof(this.Image));
            break;
         }
      }

      protected override void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
         base.OnSelfPropertyChanged(ev);
         switch(ev.PropertyName) {
         case nameof(this.UniqueId):
            OnSelfPropertyChanged<EMosaic>(ev, nameof(this.MosaicType)); // recall with another property name
            Image.MosaicType = MosaicType;
            Image.SizeField = MosaicType.SizeTileField(SkillLevel);
            Title = MosaicType.GetDescription(false);
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
