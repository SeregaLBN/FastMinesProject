using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.data.controller.types;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic skill level item for data model </summary>
   public class MosaicSkillDataItem : BaseData<ESkillLevel, MosaicsSkillCanvasBmp> {
      private const int ZoomKoef = 2;

      public MosaicSkillDataItem(ESkillLevel eSkill) : base(eSkill) {
         Title = eSkill.GetDescription();
      }

      public ESkillLevel SkillLevel => UniqueId;

      [Obsolete]
      public string UnicodeChar => SkillLevel.UnicodeChar().ToString();

      private MosaicsSkillCanvasBmp _mosaicSkillImg;
      public override MosaicsSkillCanvasBmp Image {
         get {
            if (_mosaicSkillImg == null) {
               var tmp = new MosaicsSkillCanvasBmp(SkillLevel, CanvasDevice.GetSharedDevice()) {
                  Size = new Size(ImageSize.Width * ZoomKoef, ImageSize.Height * ZoomKoef),
                  BorderWidth = 2,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
                  //, OnlySyncDraw = true
               };
               System.Diagnostics.Debug.Assert(tmp.Size.Width == ImageSize.Width * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Size.Height == ImageSize.Height * ZoomKoef);
               Image = tmp; // call this setter
            }
            return _mosaicSkillImg;
         }
         protected set {
            var old = _mosaicSkillImg;
            if (SetProperty(ref _mosaicSkillImg, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnMosaicsSkillImgPropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnMosaicsSkillImgPropertyChanged;
               }
               OnSelfPropertyChanged(nameof(this.Image));
            }
         }
      }

      private Size _imageSize = new Size(MosaicsSkillCanvasBmp.DefaultImageSize, MosaicsSkillCanvasBmp.DefaultImageSize);
      public override Size ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               Image.Size = new Size(_imageSize.Height * ZoomKoef, _imageSize.Width * ZoomKoef);
            }
         }
      }

      private void OnMosaicsSkillImgPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         if (pn == nameof(MosaicsSkillCanvasBmp.Image)) {
            // ! notify parent container
            var ev2 = ev as PropertyChangedExEventArgs<ImageSource>;
            if (ev2 == null)
               OnSelfPropertyChanged(nameof(this.Image));
            else
               OnSelfPropertyChanged(new PropertyChangedExEventArgs<ImageSource>(ev2.NewValue, ev2.OldValue, nameof(this.Image)));
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
