using System.ComponentModel;
using Windows.UI.Xaml.Media;
using fmg.core.types;
using fmg.uwp.res.img;

namespace FastMines.DataModel.Items {

   /// <summary> Mosaic group item for data model </summary>
   public class MosaicGroupDataItem : BaseData<EMosaicGroup> {
      private const int ZoomKoef = 2;

      public MosaicGroupDataItem(EMosaicGroup eMosaicGroup) : base(eMosaicGroup) {
         Title = eMosaicGroup.GetDescription();
      }

      public EMosaicGroup MosaicGroup => UniqueId;

      private MosaicsGroupImg _mosaicGroupImg;
      public MosaicsGroupImg MosaicGroupImage {
         get {
            if (_mosaicGroupImg == null) {
               var tmp = MosaicGroupImage = new MosaicsGroupImg(MosaicGroup, ImageSize * ZoomKoef) {
                  BorderWidth = 3,
                  PolarLights = true
               };
               System.Diagnostics.Debug.Assert(tmp.Size == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Width == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Height == ImageSize * ZoomKoef);
            }
            return _mosaicGroupImg;
         }
         private set {
            var old = this._mosaicGroupImg;
            if (SetProperty(ref _mosaicGroupImg, value)) {
               if (old != null)
               {
                  old.PropertyChanged -= OnMosaicGroupImagePropertyChanged;
                  old.Dispose();
               }
               if (value != null)
               {
                  value.PropertyChanged += OnMosaicGroupImagePropertyChanged;
               }
               OnPropertyChanged("Image");
            }
         }
      }

      public override ImageSource Image => MosaicGroupImage.Image;

      private int _imageSize = MosaicsGroupImg.DefaultImageSize;
      public override int ImageSize
      {
         get { return _imageSize; }
         set
         {
            if (SetProperty(ref _imageSize, value)) {
               MosaicGroupImage = null;
            }
         }
      }

      private void OnMosaicGroupImagePropertyChanged(object sender, PropertyChangedEventArgs ev)
      {
         var pn = ev.PropertyName;
         if (pn == "Image")
         {
            OnPropertyChanged(this, ev);
         }
      }

   }
}