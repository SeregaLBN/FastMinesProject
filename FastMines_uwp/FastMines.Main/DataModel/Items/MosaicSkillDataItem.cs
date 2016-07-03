using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.data.controller.types;
using MosaicsSkillImg = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic skill level item for data model </summary>
   public class MosaicSkillDataItem : BaseData<ESkillLevel> {
      private const int ZoomKoef = 2;

      public MosaicSkillDataItem(ESkillLevel eSkill) : base(eSkill) {
         Title = eSkill.GetDescription();
      }

      public ESkillLevel SkillLevel => UniqueId;

      [Obsolete]
      public string UnicodeChar => SkillLevel.UnicodeChar().ToString();

      public override ImageSource Image => MosaicSkillImage.Image;
      private MosaicsSkillImg _mosaicSkillImg;
      public MosaicsSkillImg MosaicSkillImage {
         get {
            if (_mosaicSkillImg == null) {
               var tmp = new MosaicsSkillImg(SkillLevel, CanvasDevice.GetSharedDevice()) {
                  SizeInt = ImageSize * ZoomKoef,
                  BorderWidth = 2,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
                  //, OnlySyncDraw = true
               };
               System.Diagnostics.Debug.Assert(tmp.Width == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Height == ImageSize * ZoomKoef);
               MosaicSkillImage = tmp; // call this setter
            }
            return _mosaicSkillImg;
         }
         private set {
            var old = _mosaicSkillImg;
            if (SetProperty(ref _mosaicSkillImg, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnMosaicsSkillImgPropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnMosaicsSkillImgPropertyChanged;
               }
               OnPropertyChanged(nameof(this.Image));
            }
         }
      }

      private int _imageSize = MosaicsSkillImg.DefaultImageSize;
      public override int ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               MosaicSkillImage.Size = new Size(_imageSize * ZoomKoef, _imageSize * ZoomKoef);
            }
         }
      }

      private void OnMosaicsSkillImgPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         if (pn == nameof(MosaicsSkillImg.Image)) {
            OnPropertyChanged(this, ev); // ! notify parent container
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         MosaicSkillImage = null; // call setter
      }

   }
}
