using System;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.img.wbmp;
using fmg.uwp.utils.wbmp;

namespace fmg.uwp.mosaic.wbmp {

   /// <summary> MVC: view. UWP WriteableBitmap implementation over control <see cref="Canvas"/> </summary>
   public class MosaicCanvasView : MosaicView<Canvas, WriteableBitmap, MosaicDrawModel<WriteableBitmap>> {

      class InnerView : MosaicViewWBmp<WriteableBitmap, MosaicDrawModel<WriteableBitmap>> {

         private readonly MosaicCanvasView _owner;

         public InnerView(MosaicCanvasView self)
            : base(self.Model) {
            _owner = self;
         }

         public override void Draw(IEnumerable<BaseCell> modifiedCells) {
            //Draw(modifiedCells, null, true);
            throw new InvalidOperationException();
         }

         public void Draw(IEnumerable<BaseCell> modifiedCells, RectDouble clipRegion, bool drawBk) {
            base.Draw(modifiedCells, clipRegion, drawBk);
         }

      }

      private InnerView _innerView;
      private Canvas _control;
      private Flag.Controller _imgFlag = new Flag.Controller();
      private Mine.Controller _imgMine = new Mine.Controller();

      public MosaicCanvasView()
         : base(new MosaicDrawModel<WriteableBitmap>())
      {
         _innerView = new InnerView(this);
         ChangeSizeImagesMineFlag();
      }

      protected override Canvas CreateImage() {
         // will return once created window
         return GetControl();
      }

      public Canvas GetControl() {
         if (_control == null) {
            _control = new Canvas();

            var imgControl = new Image {
               Stretch = Stretch.None
            };
            imgControl.SetBinding(Windows.UI.Xaml.Controls.Image.SourceProperty, new Binding {
               Source = Image,
               Path = new PropertyPath(nameof(Image)),
               Mode = BindingMode.OneWay
            });

            _control.Children.Add(imgControl);
         }
         return _control;
      }

      public override void Draw(IEnumerable<BaseCell> modifiedCells) {
         Canvas control = GetControl();
         RectDouble rc = new RectDouble(0,0, control.Width, control.Height);
         _innerView.Draw(modifiedCells, rc, true);
      }

      public override void Invalidate() {
         base.Invalidate();
         //AsyncRunner.InvokeFromUiLater(() => {
         //      var img = Image; // implicit call Draw() -> DrawBegin() -> this.Draw(...)
         //}, CoreDispatcherPriority.High);
      }

      protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnPropertyModelChanged(sender, ev);
         switch (ev.PropertyName) {
         case nameof(MosaicGameModel.MosaicType):
         case nameof(MosaicGameModel.Area):
            ChangeSizeImagesMineFlag();
            break;
         }
      }

      /** переустанавливаю заного размер мины/флага для мозаики */
      protected void ChangeSizeImagesMineFlag() {
         MosaicDrawModel<WriteableBitmap> model = Model;
         int sq = (int)model.CellAttr.GetSq(model.PenBorder.Width);
         if (sq <= 0) {
            System.Diagnostics.Debug.WriteLine("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
         }

         const int max = 30;
         if (sq > max) {
            _imgFlag.Model.SetSize(sq);
            _imgMine.Model.SetSize(sq);
            model.ImgFlag = _imgFlag.Image;
            model.ImgMine = _imgMine.Image;
         } else {
            _imgFlag.Model.SetSize(max);
            model.ImgFlag = ImgUtils.Zoom(_imgFlag.Image, sq, sq);
            _imgMine.Model.SetSize(max);
            model.ImgMine = ImgUtils.Zoom(_imgMine.Image, sq, sq);
         }
      }

      protected override void Disposing() {
         _innerView.Dispose();
         Model.Dispose();
         base.Disposing();
         _control = null;
         _imgFlag.Dispose();
         _imgMine.Dispose();
      }

   }

}
