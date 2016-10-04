using System;
using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.data.controller.types;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic skill level item for data model </summary>
   public class MosaicSkillDataItem : BaseData<ESkillLevel?, MosaicsSkillCanvasBmp> {
      private const int ZoomKoef = 2;

      public MosaicSkillDataItem(ESkillLevel? eSkill) : base(eSkill) {
         Title = eSkill?.GetDescription();
      }

      public ESkillLevel? SkillLevel => UniqueId;

      [Obsolete]
      public string UnicodeChar => SkillLevel?.UnicodeChar().ToString();

      private MosaicsSkillCanvasBmp _mosaicSkillImg;
      public override MosaicsSkillCanvasBmp Image {
         get {
            if (_mosaicSkillImg == null) {
               // call this setter
               Image = new MosaicsSkillCanvasBmp(SkillLevel, CanvasDevice.GetSharedDevice()) {
                  BorderWidth = 2,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
               };
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

      public override Size ImageSize {
         get {
            var size = Image.Size;
            return new Size(size.Width / ZoomKoef, size.Height / ZoomKoef);
         }
         set {
            Image.Size = new Size(value.Width * ZoomKoef, value.Height * ZoomKoef);
         }
      }

      public Bound ImagePadding {
         get {
            var pad = Image.Padding;
            return new Bound(pad.Left / ZoomKoef, pad.Top / ZoomKoef, pad.Right / ZoomKoef, pad.Bottom / ZoomKoef);
         }
         set {
            Image.Padding = new Bound(value.Left * ZoomKoef, value.Top * ZoomKoef, value.Right * ZoomKoef, value.Bottom * ZoomKoef);
         }
      }

      public Bound ImagePaddingBurgerMenu {
         get {
            var pad = Image.PaddingBurgerMenu;
            return new Bound(pad.Left / ZoomKoef, pad.Top / ZoomKoef, pad.Right / ZoomKoef, pad.Bottom / ZoomKoef);
         }
         set {
            Image.PaddingBurgerMenu = new Bound(value.Left * ZoomKoef, value.Top * ZoomKoef, value.Right * ZoomKoef, value.Bottom * ZoomKoef);
         }
      }

      private void OnMosaicsSkillImgPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is MosaicsSkillCanvasBmp);
         switch (ev.PropertyName) {
         case nameof(MosaicsSkillCanvasBmp.Size):
            OnSelfPropertyChanged<Size>(ev, nameof(this.ImageSize));
            break;
         case nameof(MosaicsSkillCanvasBmp.Image):
            OnSelfPropertyChanged<MosaicsSkillCanvasBmp>(ev, nameof(this.Image));
            break;
         case nameof(MosaicsSkillCanvasBmp.Padding):
            OnSelfPropertyChanged<Bound>(ev, nameof(this.ImagePadding));
            break;
         case nameof(MosaicsSkillCanvasBmp.PaddingBurgerMenu):
            OnSelfPropertyChanged<Bound>(ev, nameof(this.ImagePaddingBurgerMenu));
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
