using System.Linq;
using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;
using FlagCanvasBmp = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using MineCanvasBmp = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;

namespace fmg.uwp.mosaic.win2d {

   /// <summary> MVC view. UWP Win2D implementation </summary>
   public class MosaicViewWin2D : AMosaicView<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> {

      private CanvasVirtualControl _control;
      private CellPaintWin2D _cellPaint;
      private MineCanvasBmp _mineImage;
      private FlagCanvasBmp _flagImage;

      public CanvasVirtualControl Control {
         get { return _control; }
         set { _control = value; }
      }

      private MineCanvasBmp MineImg {
         get {
            if (_mineImage == null) {
               var device = CanvasDevice.GetSharedDevice();
             //var device = _control.Device;
               _mineImage = new MineCanvasBmp(device);
            }
            return _mineImage;
         }
      }

      private FlagCanvasBmp FlagImg {
         get {
            if (_flagImage == null) {
               var device = CanvasDevice.GetSharedDevice();
               //var device = _control.Device;
               _flagImage = new FlagCanvasBmp(device);
            }
            return _flagImage;
         }
      }

      public override ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> CellPaint => _cellPaint ?? (_cellPaint = new CellPaintWin2D());

      public override void Invalidate(IEnumerable<BaseCell> modifiedCells = null) {
         System.Diagnostics.Debug.Assert((modifiedCells == null) || modifiedCells.Any());
         using (new Tracer()) {
            var canvasVirtualControl = Control;
            if (canvasVirtualControl == null)
               return;
            if (double.IsNaN(canvasVirtualControl.Width) || double.IsNaN(canvasVirtualControl.Height))
               return;
            //if ((canvasVirtualControl.Size.Width == 0) || (canvasVirtualControl.Size.Height == 0))
            //   return;

            System.Diagnostics.Debug.Assert(!_alreadyPainted);

            if (modifiedCells == null) {
               canvasVirtualControl.Invalidate(); // redraw all of mosaic
               return;
            }

#if DEBUG
            var size = new SizeDouble(canvasVirtualControl.Width, canvasVirtualControl.Height); // double values
          //var size = canvasVirtualControl.Size;                                               // int values
            var tmp = new Windows.Foundation.Rect(0, 0, size.Width, size.Height);
#endif

            foreach (var cell in modifiedCells ?? Mosaic.Matrix) {
               var rc = cell.getRcOuter();
#if DEBUG
               var containsLT = tmp.Contains(rc.PointLT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left()) && tmp.Top.HasMinDiff(rc.Top()));
               var containsLB = tmp.Contains(rc.PointLB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left()) && tmp.Top.HasMinDiff(rc.Bottom()));
               var containsRT = tmp.Contains(rc.PointRT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Top()));
               var containsRB = tmp.Contains(rc.PointRB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Bottom()));
               bool intersect = (tmp != Windows.Foundation.Rect.Empty);
               //LoggerSimple.Put($"intersect={intersect}; containsLT={containsLT}; containsLB={containsLB}; containsRT={containsRT}; containsRB={containsRB}");
               System.Diagnostics.Debug.Assert(intersect && containsLT && containsRT && containsLB && containsRB);
               if (!(intersect && containsLT && containsRT && containsLB && containsRB))
                  return;
#endif
               canvasVirtualControl.Invalidate(rc.ToWinRect());
            }
         }
      }

      bool _alreadyPainted = false;
      public void OnRegionsInvalidated(CanvasVirtualControl sender, CanvasRegionsInvalidatedEventArgs ev) {
         using (new Tracer()) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _control));

            _alreadyPainted = true;
            foreach (var region in ev.InvalidatedRegions) {
               using (var ds = sender.CreateDrawingSession(region)) {
                  Repaint(ds, region);
               }
            }
            _alreadyPainted = false;
         }
      }

      private void Repaint(CanvasDrawingSession ds, Windows.Foundation.Rect region) {
         using (new Tracer()) {
            var p = new PaintableWin2D(ds);
            var pc = PaintContext;
            // paint all cells
            var sizeMosaic = Mosaic.SizeField;
            var cellPaint = CellPaint;
            foreach (var cell in Mosaic.Matrix) {
               var tmp = new Windows.Foundation.Rect(region.X, region.Y, region.Width, region.Height);
               tmp.Intersect(cell.getRcOuter().ToWinRect());
               var intersected = (tmp != Windows.Foundation.Rect.Empty);
               if (intersected)
                  cellPaint.Paint(cell, p, pc);
            }
         }
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      protected override void ChangeSizeImagesMineFlag() {
         // PS: картинки не зависят от размера ячейки...
         PaintUwpContext<CanvasBitmap> pc = PaintContext;
         //int sq = (int)Mosaic.CellAttr.GetSq(pc.PenBorder.Width);
         //if (sq <= 0) {
         //   System.Diagnostics.Debug.Assert(false, "Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
         //   sq = 3; // ат балды...
         //}
         //MineImg = null;
         //FlagImg = null;
         pc.ImgFlag = FlagImg.Image;
         pc.ImgMine = MineImg.Image;
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            MineImg.Dispose();
          //FlagImg.Dispose();
         }
      }

   }

}
