using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.core.mosaic.draw;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.mosaic.win2d {

   /// <summary> MVC view. Abstract UWP Win2D implementation </summary>
   public abstract class AMosaicViewWin2D : AMosaicView<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> {

      private CellPaintWin2D _cellPaint;
      public override ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> CellPaint => _cellPaint ?? (_cellPaint = new CellPaintWin2D());

      public CanvasDrawingSession Paintable { get; set; }

      protected bool _alreadyPainted = false;
      protected void Repaint(IEnumerable<BaseCell> modifiedCells, RectDouble clipRegion) {
         var ds = Paintable;
         if (ds == null)
            return;

         System.Diagnostics.Debug.Assert(!_alreadyPainted);

         _alreadyPainted = true;

         //using (new Tracer())
         {
            var pc = PaintContext;

            if (pc.IsUseBackgroundColor) {
               // background color
               ds.Clear(pc.BackgroundColor.ToWinColor());
               //ds.FillRectangle(clipRegion, pc.BackgroundColor.ToWinColor());
            }

            if (modifiedCells == null)
               modifiedCells = Mosaic.Matrix; // check to redraw all mosaic cells

            var p = new PaintableWin2D(ds);
            // paint all cells
            var sizeMosaic = Mosaic.SizeField;
            var cellPaint = CellPaint;
            foreach (var cell in modifiedCells) {
               var rco = cell.getRcOuter();
               if (rco.Intersects(clipRegion))
                  cellPaint.Paint(cell, p, pc);
            }
         }

         _alreadyPainted = false;
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing)
            _cellPaint = null;
      }

   }

}
