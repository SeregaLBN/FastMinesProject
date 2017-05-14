using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.uwp.draw.mosaic;
using FlagCanvasBmp = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using MineCanvasBmp = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;

namespace fmg.uwp.mosaic.win2d {

   /// summary> MVC: view. UWP Win2D implementation. Base implementation View located into control <see cref="Windows.UI.Xaml.FrameworkElement"/> */
   public abstract class AMosaicViewInControl<TControl> : AMosaicViewWin2D
      where TControl : FrameworkElement
   {
      protected TControl _control;
      private MineCanvasBmp _mineImage;
      private FlagCanvasBmp _flagImage;

      public virtual TControl Control {
         get { return _control; }
         set { _control = value; }
      }

      protected virtual CanvasDevice GetCanvasDevice() {
         return CanvasDevice.GetSharedDevice();
      }

      private MineCanvasBmp MineImg {
         get {
            if (_mineImage == null)
               _mineImage = new MineCanvasBmp(GetCanvasDevice());
            return _mineImage;
         }
      }

      private FlagCanvasBmp FlagImg {
         get {
            if (_flagImage == null)
               _flagImage = new FlagCanvasBmp(GetCanvasDevice());
            return _flagImage;
         }
      }

      public override SizeDouble Size {
         get {
            // TODO: return getController().WindowSize
            return new SizeDouble(Control?.Width ?? 0, Control?.Height ?? 0);
         }
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      protected override void ChangeSizeImagesMineFlag() {
         // PS: картинки не зависят от размера ячейки...
         PaintUwpContext<CanvasBitmap> pc = PaintContext;
         int sq = (int)Mosaic.CellAttr.GetSq(pc.PenBorder.Width);
         if (sq <= 0) {
            System.Diagnostics.Debug.Assert(false, "Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
            sq = 3; // ат балды...
         }
         //MineImg = null;
         //FlagImg = null;

         if (sq >= 50) { // ignore small sizes
            MineImg.Size = new Size(sq, sq);
         }
         pc.ImgMine = MineImg.Image;
         pc.ImgFlag = FlagImg.Image;
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            MineImg.Dispose();
            FlagImg.Dispose();
            _mineImage = null;
            _flagImage = null;

            Control = null;
         }
      }

   }

}
