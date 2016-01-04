using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.data.controller.types;
using fmg.uwp.res.img;

namespace FastMines.DataModel.Items {

   /// <summary> Mosaic item for data model </summary>
   class MosaicDataItem : BaseData<EMosaic>, IDisposable {
      private const int ZoomKoef = 2;

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
               var padIn = new Bound(5, 5, 5, 5);
               var sizeImageIn = new Size(ImageSize - padIn.Left - padIn.Right, ImageSize - padIn.Top - padIn.Bottom);
               var sizeImageOut = new Size(sizeImageIn);
               int area = MosaicHelper.FindAreaBySize(MosaicType, sizeField, ref sizeImageOut);
               System.Diagnostics.Debug.Assert(ImageSize >= (sizeImageOut.width + padIn.Left + padIn.Right));
               System.Diagnostics.Debug.Assert(ImageSize >= (sizeImageOut.height + padIn.Top + padIn.Bottom));
               var paddingOut = new Bound(
                  (ImageSize - sizeImageOut.width) / 2,
                  (ImageSize - sizeImageOut.height) / 2,
                  (ImageSize - sizeImageOut.width) / 2 + (ImageSize - sizeImageOut.width) % 2,
                  (ImageSize - sizeImageOut.height) / 2 + (ImageSize - sizeImageOut.height) % 2);
               System.Diagnostics.Debug.Assert(ImageSize == sizeImageOut.width + paddingOut.Left + paddingOut.Right);
               System.Diagnostics.Debug.Assert(ImageSize == sizeImageOut.height + paddingOut.Top + paddingOut.Bottom);
               var tmp = new MosaicsImg {
                  MosaicType = MosaicType,
                  SizeField = sizeField,
                  Area = area * ZoomKoef,
                  BackgroundColor = StaticImg<object, object>.DefaultBkColor,
                  Padding = new Bound(paddingOut.Left * ZoomKoef, paddingOut.Top * ZoomKoef, paddingOut.Right * ZoomKoef, paddingOut.Bottom * ZoomKoef),
                  BorderWidth = 3,
                  //RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
               };
               var bmp = tmp.Image;
               //System.Diagnostics.Debug.Assert(tmp.Size == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(bmp.PixelWidth == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(bmp.PixelHeight == ImageSize * ZoomKoef);
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
         if (pn == "Image") {
            OnPropertyChanged(this, ev); // ! notify parent container
         }
      }

      public void Dispose() {
         _mosaicImg = null; // call setter
      }

   }

}
