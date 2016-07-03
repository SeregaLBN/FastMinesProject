using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.types;
using MosaicsGroupImg = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic group item for data model </summary>
   public class MosaicGroupDataItem : BaseData<EMosaicGroup> {
      private const int ZoomKoef = 2;

      public MosaicGroupDataItem(EMosaicGroup eMosaicGroup) : base(eMosaicGroup) {
         Title = eMosaicGroup.GetDescription();
      }

      public EMosaicGroup MosaicGroup => UniqueId;

      public override ImageSource Image => MosaicGroupImage.Image;
      private MosaicsGroupImg _mosaicGroupImg;
      public MosaicsGroupImg MosaicGroupImage
      {
         get
         {
            if (_mosaicGroupImg == null) {
               var tmp = new MosaicsGroupImg(MosaicGroup, CanvasDevice.GetSharedDevice()) {
                  SizeInt = ImageSize * ZoomKoef,
                  BorderWidth = 3,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
                  //, OnlySyncDraw = true
               };
               System.Diagnostics.Debug.Assert(tmp.Width == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Height == ImageSize * ZoomKoef);
               MosaicGroupImage = tmp; // call this setter
            }
            return _mosaicGroupImg;
         }
         private set
         {
            var old = _mosaicGroupImg;
            if (SetProperty(ref _mosaicGroupImg, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnImagePropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnImagePropertyChanged;
               }
               OnPropertyChanged("Image");
            }
         }
      }

      private int _imageSize = MosaicsGroupImg.DefaultImageSize;
      public override int ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               MosaicGroupImage.Size = new Size(_imageSize * ZoomKoef, _imageSize * ZoomKoef);
            }
         }
      }

      private void OnImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         if (pn == "Image") {
            OnPropertyChanged(this, ev); // ! notify parent container
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         MosaicGroupImage = null; // call setter
      }

   }
}
