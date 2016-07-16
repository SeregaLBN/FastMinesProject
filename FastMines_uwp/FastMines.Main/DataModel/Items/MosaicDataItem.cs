using System.ComponentModel;
using Windows.UI.Xaml.Media;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;
using fmg.data.controller.types;
using MosaicsImg = fmg.uwp.draw.img.win2d.MosaicsImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic item for data model </summary>
   public class MosaicDataItem : BaseData<EMosaic> {
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
               MosaicImage.SizeField = MosaicType.SizeTileField(value);
            }
         }
      }

      public override ImageSource Image => MosaicImage.Image;
      private MosaicsImg _mosaicImg;
      public MosaicsImg MosaicImage {
         get {
            if (_mosaicImg == null) {
               var sizeField = MosaicType.SizeTileField(SkillLevel);
               var tmp = new MosaicsImg(MosaicType, sizeField, CanvasDevice.GetSharedDevice()) {
                  SizeInt = ImageSize * ZoomKoef,
                  PaddingInt = 5 * ZoomKoef,
                  RotateMode = MosaicsImg.ERotateMode.SomeCells,
                  //BackgroundColor = MosaicsImg.DefaultBkColor,
                  BorderWidth = 3*ZoomKoef//,
                  //RotateAngle = 45 * new Random(Guid.NewGuid().GetHashCode()).Next(7)
                  //, OnlySyncDraw = true
               };
               //var bmp = tmp.Image;
               //System.Diagnostics.Debug.Assert(bmp.PixelWidth == ImageSize * ZoomKoef);
               //System.Diagnostics.Debug.Assert(bmp.PixelHeight == ImageSize * ZoomKoef);
               MosaicImage = tmp; // call this setter
            }
            return _mosaicImg;
         }
         private set {
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

      private int _imageSize = 100; // MosaicsImg.DefaultImageSize;
      public override int ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               MosaicImage.Size = new Size(_imageSize * ZoomKoef, _imageSize * ZoomKoef);
            }
         }
      }

      private void OnMosaicImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         //LoggerSimple.Put(GetType().Name+"::OnPropertyChanged: " + ev.PropertyName);
         if (pn == nameof(MosaicImage.Image)) {
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
            MosaicImage.MosaicType = MosaicType;
            MosaicImage.SizeField = MosaicType.SizeTileField(SkillLevel);
            Title = MosaicType.GetDescription(false);
            break;
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         _mosaicImg = null; // call setter
      }

   }

}
