using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using MosaicsGroupCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasBmp;

namespace fmg.DataModel.Items {

   /// <summary> Mosaic group item for data model </summary>
   public class MosaicGroupDataItem : BaseData<EMosaicGroup?, MosaicsGroupCanvasBmp> {

      public MosaicGroupDataItem(EMosaicGroup? eMosaicGroup)
         : base(eMosaicGroup)
      {
         Title = eMosaicGroup?.GetDescription();
      }

      public EMosaicGroup? MosaicGroup => UniqueId;

      public override int Zoom() { return 2; }

      private MosaicsGroupCanvasBmp _mosaicGroupImg;
      public override MosaicsGroupCanvasBmp Image {
         get {
            if (_mosaicGroupImg == null) {
               // call this setter
               Image = new MosaicsGroupCanvasBmp(MosaicGroup, CanvasDevice.GetSharedDevice()) {
                  BorderWidth = 3,
                  RotateAngle = ThreadLocalRandom.Current.Next(90)
               };
            }
            return _mosaicGroupImg;
         }
         protected set {
            var old = _mosaicGroupImg;
            if (SetProperty(ref _mosaicGroupImg, value)) {
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
         case nameof(MosaicsGroupCanvasBmp.PaddingBurgerMenu):
            OnSelfPropertyChanged<Bound>(ev, nameof(this.ImagePaddingBurgerMenu));
            break;
         }
      }

   }
}
