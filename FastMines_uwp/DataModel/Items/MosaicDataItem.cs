using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using fmg.common.geom;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.res.img;

namespace FastMines.DataModel.Items {

   /// <summary> Mosaic item for data model </summary>
   class MosaicDataItem : BaseData<EMosaic>, IDisposable
   {
      private const int ZoomKoef = 2;

      public MosaicDataItem(EMosaic eMosaic) : base(eMosaic) {
         Title = eMosaic.GetDescription(false);
      }

      public EMosaic MosaicType => UniqueId;

      private MosaicsImg _mosaicImg;
      public MosaicsImg MosaicImage
      {
         get
         {
            if (_mosaicImg == null) {
               var tmp = MosaicImage = new MosaicsImg {
                  MosaicType = MosaicType,
                  SizeField = MosaicType.SizeTileField(ESkillLevel.eAmateur), // TODO - как свойство: MosaicDataItem.SkillLevel
                  Area = 456, // TODO - вычслять в зависимости от размера картинки == ImageSize * ZoomKoef
                  BackgroundColor = StaticImg<object, object>.DefaultBkColor,
                  Padding = new Bound(5,5,5,5),
                  //BorderWidth = 3,
                  //RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
               };
               tmp.GContext.PenBorder.Width = 3; // eq tmp.BorderWidth
               //System.Diagnostics.Debug.Assert(tmp.Size == ImageSize * ZoomKoef);
               //System.Diagnostics.Debug.Assert(tmp.Width == ImageSize * ZoomKoef);
               //System.Diagnostics.Debug.Assert(tmp.Height == ImageSize * ZoomKoef);
            }
            return _mosaicImg;
         }
         private set
         {
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

      public override ImageSource Image => MosaicImage.Image;

      private int _imageSize = MosaicsImg.DefaultImageSize;
      public override int ImageSize
      {
         get { return _imageSize; }
         set
         {
            if (SetProperty(ref _imageSize, value)) {
               MosaicImage.Size = ImageSize * ZoomKoef;
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
