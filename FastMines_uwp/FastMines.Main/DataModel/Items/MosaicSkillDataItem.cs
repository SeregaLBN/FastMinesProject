using System;
using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.data.controller.types;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic skill level item for data model </summary>
   public class MosaicSkillDataItem : BaseData<ESkillLevel?, MosaicsSkillCanvasBmp> {

      public MosaicSkillDataItem(ESkillLevel? eSkill)
         : base(eSkill) 
      {
         Title = eSkill?.GetDescription();
      }

      public ESkillLevel? SkillLevel => UniqueId;

      [Obsolete]
      public string UnicodeChar => SkillLevel?.UnicodeChar().ToString();

      public override int Zoom() { return 2; }

      private MosaicsSkillCanvasBmp _mosaicSkillImg;
      public override MosaicsSkillCanvasBmp Image {
         get {
            if (_mosaicSkillImg == null) {
               // call this setter
               Image = new MosaicsSkillCanvasBmp(SkillLevel, CanvasDevice.GetSharedDevice()) {
                  BorderWidth = 2,
                  RotateAngle = ThreadLocalRandom.Current.Next(90)
               };
            }
            return _mosaicSkillImg;
         }
         protected set {
            var old = _mosaicSkillImg;
            if (SetProperty(ref _mosaicSkillImg, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnImagePropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnImagePropertyChanged;
               }
               OnSelfPropertyChanged(nameof(this.Image));
            }
         }
      }

      public Bound ImagePaddingBurgerMenu {
         get {
            var pad = Image.PaddingBurgerMenu;
            var zoom = Zoom();
            return new Bound(pad.Left / zoom, pad.Top / zoom, pad.Right / zoom, pad.Bottom / zoom);
         }
         set {
            var zoom = Zoom();
            Image.PaddingBurgerMenu = new Bound(value.Left * zoom, value.Top * zoom, value.Right * zoom, value.Bottom * zoom);
         }
      }

      protected override void OnImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnImagePropertyChanged(sender, ev);
         switch (ev.PropertyName) {
         case nameof(MosaicsSkillCanvasBmp.PaddingBurgerMenu):
            OnSelfPropertyChanged<Bound>(ev, nameof(this.ImagePaddingBurgerMenu));
            break;
         }
      }

   }
}
