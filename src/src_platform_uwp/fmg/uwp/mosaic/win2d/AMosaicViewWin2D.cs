using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.mosaic.win2d;

namespace fmg.uwp.mosaic.win2d {
#if false

    /// <summary> MVC view. Abstract UWP Win2D implementation </summary>
    public abstract class AMosaicViewWin2D : AMosaicView<PaintableWin2D, CanvasBitmap, PaintWin2DContext> {

        private CellPaintWin2D _cellPaint;
        public override ICellPaint<PaintableWin2D, CanvasBitmap, PaintWin2DContext> CellPaint => _cellPaint ?? (_cellPaint = new CellPaintWin2D());

        public CanvasDrawingSession Paintable { get; set; }

        protected bool _alreadyPainted = false;
        public override void Repaint(IEnumerable<BaseCell> modifiedCells, RectDouble? clipRegion) {
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
                    if (clipRegion.HasValue)
                        ds.FillRectangle(clipRegion.Value.ToWinRect(), pc.BackgroundColor.ToWinColor());
                    else
                        //ds.FillRectangle(new Windows.Foundation.Rect(0, 0, Size.Width, Size.Height), pc.BackgroundColor.ToWinColor());
                        ds.Clear(pc.BackgroundColor.ToWinColor());
                }

                if (modifiedCells == null)
                    modifiedCells = Mosaic.Matrix; // check to redraw all mosaic cells

                var p = new PaintableWin2D(ds);
                // paint all cells
                var sizeMosaic = Mosaic.SizeField;
                var cellPaint = CellPaint;
                double padX = pc.Padding.Left, padY = pc.Padding.Top;
                foreach (var cell in modifiedCells) {
                    if (!clipRegion.HasValue || cell.getRcOuter().MoveXY(padX, padY).Intersection(clipRegion.Value))
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

#endif
}
