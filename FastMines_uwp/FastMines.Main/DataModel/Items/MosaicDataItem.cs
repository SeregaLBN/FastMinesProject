using System.ComponentModel;
using Windows.UI.Xaml.Media;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.common.notyfier;
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
                  Size = new Size(ImageSize.Width * ZoomKoef, ImageSize.Height * ZoomKoef),
                  PaddingInt = 5 * ZoomKoef,
                  RotateMode = MosaicsCanvasBmp.ERotateMode.SomeCells,
                  //BackgroundColor = MosaicsCanvasBmp.DefaultBkColor,
                  BorderWidth = 3*ZoomKoef//,
                  //RotateAngle = 45 * new Random(Guid.NewGuid().GetHashCode()).Next(7)
                  //, OnlySyncDraw = true
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

      private Size _imageSize = new Size(MosaicsCanvasBmp.DefaultImageSize, MosaicsCanvasBmp.DefaultImageSize);
      public override Size ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               Image.Size = new Size(_imageSize.Width * ZoomKoef, _imageSize.Height * ZoomKoef);
            }
         }
      }

      private void OnMosaicImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         //LoggerSimple.Put(GetType().Name+"::OnPropertyChanged: " + ev.PropertyName);
         if (pn == nameof(Image.Image)) {
            // ! notify parent container
            var ev2 = ev as PropertyChangedExEventArgs<ImageSource>;
            if (ev2 == null)
               OnSelfPropertyChanged(nameof(this.Image));
            else
               OnSelfPropertyChanged(new PropertyChangedExEventArgs<ImageSource>(ev2.NewValue, ev2.OldValue, nameof(this.Image)));
         }
      }

      protected override void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
         base.OnSelfPropertyChanged(ev);
         switch(ev.PropertyName) {
         case nameof(this.UniqueId):
            var ev2 = ev as PropertyChangedExEventArgs<EMosaic>;
            if (ev2 == null)
               OnSelfPropertyChanged(nameof(this.MosaicType));
            else
               OnSelfPropertyChanged(new PropertyChangedExEventArgs<EMosaic>(ev2.NewValue, ev2.OldValue, nameof(this.MosaicType)));
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
