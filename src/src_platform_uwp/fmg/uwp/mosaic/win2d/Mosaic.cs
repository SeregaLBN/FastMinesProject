using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.core.types.click;
using fmg.data.view.draw;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;
using fmg.uwp.utils;
using FlagCanvasBmp = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using MineCanvasBmp = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;

namespace fmg.uwp.mosaic.win2d {

   public class Mosaic : MosaicBase<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> {

      private PaintUwpContext<CanvasBitmap> _paintContext;
      private CellPaintWin2D _cellPaint;
      private readonly CanvasVirtualControl _container;
      private MineCanvasBmp _mineImage;
      private FlagCanvasBmp _flagImage;

      public Mosaic(CanvasVirtualControl container) {
         _container = container;
      }

      public Mosaic(CanvasVirtualControl container, Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) :
         base(sizeField, mosaicType, minesCount, area)
      {
         _container = container;
      }

      private MineCanvasBmp MineImg {
         get {
            if (_mineImage == null) {
               var device = CanvasDevice.GetSharedDevice();
               //var device = _container.Device;
               _mineImage = new MineCanvasBmp(device);
            }
            return _mineImage;
         }
      }

      private FlagCanvasBmp FlagImg {
         get {
            if (_flagImage == null) {
               var device = CanvasDevice.GetSharedDevice();
               //var device = _container.Device;
               _flagImage = new FlagCanvasBmp(device);
            }
            return _flagImage;
         }
      }

      protected override void OnError(string msg) {
#if DEBUG
         System.Diagnostics.Debug.Assert(false, msg);
#else
         base.OnError(msg);
#endif
      }

      public PaintUwpContext<CanvasBitmap> PaintContext {
         get {
            if (_paintContext == null) {
               _paintContext = new PaintUwpContext<CanvasBitmap>(false);
               _paintContext.ImgMine = MineImg.Image;
               _paintContext.ImgFlag = FlagImg.Image;
               _paintContext.PropertyChanged += OnPaintContextPropertyChanged; // изменение контекста -> перерисовка мозаики
            }
            return _paintContext;
         }
      }

      public override ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> CellPaint => CellPaintFigures;
      protected CellPaintWin2D CellPaintFigures => _cellPaint ?? (_cellPaint = new CellPaintWin2D());

      const bool ASYNC_PAINT = !true;
      public void Repaint() { Repaint((IList<BaseCell>)null); }
      protected override void Repaint(IList<BaseCell> needRepaint) {
         if (_container == null)
            return;

         if (_alreadyPainted) {
            LoggerSimple.Put("  Already painted! {0}={1}", nameof(needRepaint), (needRepaint == null) ? "null" : needRepaint.ToString());
            if ((needRepaint != null) || _toRepaint.Any()){
               System.Diagnostics.Debug.Assert(false, "Review this..");
               //throw new Exception("Bad algorithm... (");
            }
            return;
         }

         if (needRepaint == null) {
            System.Diagnostics.Debug.Assert(!_toRepaint.Any(), "How so? Review this..");
            _toRepaint.Clear();
         } else {
            foreach (var c in needRepaint)
               _toRepaint.Add(c);
         }

         if (ASYNC_PAINT)
            AsyncRunner.InvokeFromUiLater(MarkAllToRepaint, Windows.UI.Core.CoreDispatcherPriority.High);
         else
            MarkAllToRepaint();
      }

      private bool _alreadyPainted;
      private readonly ISet<BaseCell> _toRepaint = new HashSet<BaseCell>();
      /// <summary> Invaliadate all needed </summary>
      protected void MarkAllToRepaint() {
         if (_alreadyPainted) {
            System.Diagnostics.Debug.Assert(false, "Review this..");
            //throw new Exception("Bad algorithm... (");
            return;
         }

         _alreadyPainted = true;
         try {
            bool fullRepaint = !_toRepaint.Any();
            if (fullRepaint) {
               // redraw all of mosaic
               //var size = WindowSize;
               //_container.Invalidate(new Windows.Foundation.Rect(0, 0, size.Width, size.Height));
               _container.Invalidate();
            } else {
#if DEBUG
               var size = new SizeDouble(_container.Width, _container.Height); // double values
             //var size = _container.Size;                                     // int values
               var tmp = new Windows.Foundation.Rect(0, 0, size.Width, size.Height);
#endif
               foreach (var cell in _toRepaint) {
                  var rc = cell.getRcOuter();
#if DEBUG
                  var containsLT = tmp.Contains(rc.PointLT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left())  && tmp.Top.HasMinDiff(rc.Top()));
                  var containsLB = tmp.Contains(rc.PointLB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left())  && tmp.Top.HasMinDiff(rc.Bottom()));
                  var containsRT = tmp.Contains(rc.PointRT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Top()));
                  var containsRB = tmp.Contains(rc.PointRB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Bottom()));
                  bool intersect = (tmp != Windows.Foundation.Rect.Empty);
                //LoggerSimple.Put($"intersect={intersect}; containsLT={containsLT}; containsLB={containsLB}; containsRT={containsRT}; containsRB={containsRB}");
                  System.Diagnostics.Debug.Assert(intersect && containsLT && containsRT && containsLB && containsRB);
#endif
                  _container.Invalidate(rc.ToWinRect());
               }
            }
         } finally {
            if (!USE_HEURISTICS_REPAINT)
               _toRepaint.Clear();
            _alreadyPainted = false;
         }
      }

      const bool USE_HEURISTICS_REPAINT = !true;
      public void Repaint(CanvasDrawingSession ds, Windows.Foundation.Rect region) {
         if (_alreadyPainted) {
            System.Diagnostics.Debug.Assert(false, "Review this..");
            return;
         }

         try {
            _alreadyPainted = true;

            var p = new PaintableWin2D(ds);
            var pc = PaintContext;
            // paint all cells
            if (USE_HEURISTICS_REPAINT) {
               bool fullRepaint = !_toRepaint.Any();
               foreach (var cell in (fullRepaint ? (ICollection<BaseCell>)Matrix : (ICollection<BaseCell>)_toRepaint))
                  CellPaint.Paint(cell, p, pc);
            } else {
               var sizeMosaic = SizeField;
               for (var i = 0; i < sizeMosaic.m; i++)
                  for (var j = 0; j < sizeMosaic.n; j++) {
                     var cell = base.getCell(i, j);
                     var tmp = new Windows.Foundation.Rect(region.X, region.Y, region.Width, region.Height);
                     tmp.Intersect(cell.getRcOuter().ToWinRect());
                     var intersected = (tmp != Windows.Foundation.Rect.Empty);
                     if (intersected)
                        CellPaint.Paint(cell, p, pc);
                  }
            }
         } finally {
            if (USE_HEURISTICS_REPAINT)
               _toRepaint.Clear();
            _alreadyPainted = false;
         }
      }

      public override bool GameNew() {
         var mode = 1 + new Random(Guid.NewGuid().GetHashCode()).Next(MosaicHelper.CreateAttributeInstance(MosaicType, Area).getMaxBackgroundFillModeValue());
         //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
         PaintContext.BkFill.Mode = mode;
         var res = base.GameNew();
         if (!res)
            Repaint(null);
         return res;
      }

      protected override void GameBegin(BaseCell firstClickCell) {
         PaintContext.BkFill.Mode = 0;
         base.GameBegin(firstClickCell);
      }

      /// <summary> преобразовать экранные координаты в ячейку поля мозаики </summary>
      private BaseCell CursorPointToCell(PointDouble point) {
         return Matrix.FirstOrDefault(cell =>
            //cell.getRcOuter().Contains(point) && // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            cell.PointInRegion(point));
      }

      public ClickResult MousePressed(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("Mosaic.MousePressed", "clickPoint" + clickPoint + "; isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? OnLeftButtonDown(CursorPointToCell(clickPoint))
               : OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("Mosaic::MouseReleased", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? OnLeftButtonUp(CursorPointToCell(clickPoint))
               : OnRightButtonUp(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseFocusLost() {
         if (CellDown == null)
            return null;
         bool isLeft = CellDown.State.Down; // hint: State.Down used only for the left click
         using (new Tracer("Mosaic::MouseFocusLost", string.Format("CellDown.Coord={0}; isLeft={1}", CellDown.getCoord(), isLeft))) {
            return isLeft
               ? OnLeftButtonUp(null)
               : OnRightButtonUp(null);
         }
      }

      protected override void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
         base.OnSelfPropertyChanged(ev);
         switch (ev.PropertyName) {
         case nameof(this.MosaicType):
            ChangeFontSize();
            break;
         case nameof(this.Area):
            ChangeFontSize(PaintContext.PenBorder);
            break;
         case nameof(this.Matrix):
            Repaint(null);
            break;
         }
      }

      private void OnPaintContextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is PaintContext<CanvasBitmap>);

         switch (ev.PropertyName) {
         case nameof(PaintContext.PenBorder):
            var evex = ev as PropertyChangedExEventArgs<PenBorder>;
            var penBorder = evex?.NewValue ?? PaintContext.PenBorder;
            ChangeFontSize(penBorder);
            break;
         //case "Font":
         //case "BackgroundFill":
         //   //Repaint(null);
         //   break;
         }
         Repaint(null);
         OnSelfPropertyChanged(nameof(PaintContext));
         OnSelfPropertyChanged(nameof(PaintContext) + "." + ev.PropertyName);
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      public void ChangeFontSize() { ChangeFontSize(PaintContext.PenBorder); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize(PenBorder penBorder) {
         PaintContext.FontInfo.Size = (int)CellAttr.GetSq(penBorder.Width);
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            MineImg.Dispose();
          //FlagImg.Dispose();
            PaintContext.Dispose();
         }
      }

   }

}
