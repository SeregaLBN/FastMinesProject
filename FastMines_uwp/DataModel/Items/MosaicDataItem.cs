using System.ComponentModel;
using Windows.UI.Xaml.Media;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.res.img;
using FastMines.Common;

namespace FastMines.DataModel.Items {

   /// <summary> Mosaic item for data model </summary>
   public class MosaicDataItem : BaseData<EMosaic> {
      private const int ZoomKoef = 5;

      public MosaicDataItem(EMosaic eMosaic) : base(eMosaic) {
         Title = eMosaic.GetDescription(false);
      }

      public EMosaic MosaicType => UniqueId;

      private ESkillLevel _skillLevel;
      public ESkillLevel SkillLevel {
         get { return _skillLevel; }
         set {
            if (SetProperty(ref _skillLevel, value)) {
               _mosaicImg = null;
            }
         }
      }

      public override ImageSource Image => MosaicImage.Image;
      private MosaicsImg _mosaicImg;
      public MosaicsImg MosaicImage {
         get {
            if (_mosaicImg == null) {
               var sizeField = MosaicType.SizeTileField(SkillLevel);
               var tmp = new MosaicsImg(MosaicType, sizeField, ImageSize * ZoomKoef, 5 * ZoomKoef) {
                  BackgroundColor = StaticImg<object, object>.DefaultBkColor,
                  BorderWidth = 3
                  //RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
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
               OnPropertyChanged("Image");
            }
         }
      }

      private int _imageSize = 100; // MosaicsImg.DefaultImageSize;
      public override int ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               _mosaicImg = null;
            }
         }
      }

      private void OnMosaicImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         //LoggerSimple.Put(GetType().Name+"::OnPropertyChanged: " + ev.PropertyName);
         if (pn == "Image") {
            OnPropertyChanged(this, ev); // ! notify parent container
         }
      }

      public override void Dispose() {
         _mosaicImg = null; // call setter
      }

   }

}
