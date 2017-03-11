using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.core.types.click;
using fmg.data.view.draw;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;
using FlagCanvasBmp = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using MineCanvasBmp = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;

namespace fmg.uwp.mosaic.win2d {

   /// <summary> MVC: controller </summary>
   public class MosaicController : Disposable {

      /// <summary> MVC: model </summary>
      private MosaicBase _mosaic;
      /// <summary> MVC: view </summary>
      private MosaicView _view;

      /// <summary> MVC: model </summary>
      public MosaicBase Mosaic {
         get {
            if (_mosaic == null)
               Mosaic = new MosaicBase(); // call setter
            return _mosaic;
         }
         private set {
            if (_mosaic != null) {
               _mosaic.Dispose();
            }
            _mosaic = value;
            if (_mosaic != null)
               View.Mosaic = _mosaic;
         }
      }

      /// <summary> MVC: view </summary>
      public MosaicView View {
         get {
            if (_view == null)
               View = new MosaicView(); // call setter
            return _view;
         }
         private set {
            if (_view != null)
               _view.Dispose();
            _view = value;
         }
      }


      public void GameNew() {
         var mosaic = Mosaic;
         var mode = 1 + new Random(Guid.NewGuid().GetHashCode()).Next(MosaicHelper.CreateAttributeInstance(mosaic.MosaicType, mosaic.Area).getMaxBackgroundFillModeValue());
         //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
         View.PaintContext.BkFill.Mode = mode;
         var res = mosaic.GameNew();
         if (!res)
            View.InvalidateCells(mosaic.Matrix);
      }

      protected void GameBegin(BaseCell firstClickCell) {
         View.PaintContext.BkFill.Mode = 0;
         Mosaic.GameBegin(firstClickCell);
      }

      /// <summary> преобразовать экранные координаты в ячейку поля мозаики </summary>
      private BaseCell CursorPointToCell(PointDouble point) {
         return Mosaic.Matrix.FirstOrDefault(cell =>
            //cell.getRcOuter().Contains(point) && // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            cell.PointInRegion(point));
      }

      public ClickResult MousePressed(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("Mosaic.MousePressed", "clickPoint" + clickPoint + "; isLeftMouseButton=" + isLeftMouseButton)) {
            return isLeftMouseButton
               ? Mosaic.OnLeftButtonDown(CursorPointToCell(clickPoint))
               : Mosaic.OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("Mosaic::MouseReleased", "isLeftMouseButton=" + isLeftMouseButton)) {
            return isLeftMouseButton
               ? Mosaic.OnLeftButtonUp(CursorPointToCell(clickPoint))
               : Mosaic.OnRightButtonUp(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseFocusLost() {
         if (Mosaic.CellDown == null)
            return null;
         bool isLeft = Mosaic.CellDown.State.Down; // hint: State.Down used only for the left click
         using (new Tracer("Mosaic::MouseFocusLost", string.Format("CellDown.Coord={0}; isLeft={1}", Mosaic.CellDown.getCoord(), isLeft))) {
            return isLeft
               ? Mosaic.OnLeftButtonUp(null)
               : Mosaic.OnRightButtonUp(null);
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            Mosaic = null; // call setter - unsubscribe & dispose
            View = null; // call setter - unsubscribe & dispose
         }
      }

      //static string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) { return "MosaicContrllr::" + callerName; }

   }

   public class MosaicView : Disposable {

      private CanvasVirtualControl _canvasVirtualControl;
      private PaintUwpContext<CanvasBitmap> _paintContext;
      private CellPaintWin2D _cellPaint;
      private MineCanvasBmp _mineImage;
      private FlagCanvasBmp _flagImage;
      private MosaicBase _mosaic;

      public CanvasVirtualControl CanvasVirtualControl {
         get { return _canvasVirtualControl; }
         set { _canvasVirtualControl = value; }
      }

      public MosaicBase Mosaic {
         get { return _mosaic; }
         set {
            if (_mosaic != null) {
               _mosaic.PropertyChanged -= OnMosaicPropertyChanged;
               _mosaic.Dispose();
            }
            _mosaic = value;
            if (_mosaic != null)
               _mosaic.PropertyChanged += OnMosaicPropertyChanged;
         }
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

      public PaintUwpContext<CanvasBitmap> PaintContext {
         get {
            if (_paintContext == null)
               PaintContext = new PaintUwpContext<CanvasBitmap>(false); // call setter
            return _paintContext;
         }
         private set {
            if (_paintContext != null) {
               _paintContext.PropertyChanged -= OnPaintContextPropertyChanged;
               _paintContext.Dispose();
            }
            _paintContext = value;
            if (_paintContext != null) {
               _paintContext.ImgMine = MineImg.Image;
               _paintContext.ImgFlag = FlagImg.Image;
               _paintContext.PropertyChanged += OnPaintContextPropertyChanged; // изменение контекста -> перерисовка мозаики
            }
         }
      }

      public ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> CellPaint => CellPaintFigures;
      protected CellPaintWin2D CellPaintFigures => _cellPaint ?? (_cellPaint = new CellPaintWin2D());

      protected void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Mosaic));
         switch (ev.PropertyName) {
         case nameof(Mosaic.MosaicType):
            ChangeFontSize();
            break;
         case nameof(Mosaic.Area):
            ChangeFontSize(PaintContext.PenBorder);
            break;
         case nameof(Mosaic.Matrix):
            InvalidateCells(Mosaic.Matrix);
            break;
         case MosaicBase.PROPERTY_MODIFIED_CELLS:
            InvalidateCells((ev as IPropertyChangedExEventArgs<IEnumerable<BaseCell>>).NewValue);
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
         }
         //this.InvalidateCells(Mosaic.Matrix);
         //OnSelfPropertyChanged(nameof(PaintContext));
         //OnSelfPropertyChanged(nameof(PaintContext) + "." + ev.PropertyName);
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      public void ChangeFontSize() { ChangeFontSize(PaintContext.PenBorder); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      public void ChangeFontSize(PenBorder penBorder) {
         PaintContext.FontInfo.Size = (int)Mosaic.CellAttr.GetSq(penBorder.Width);
      }


      public void InvalidateCells(IEnumerable<BaseCell> modifiedCells) {
         using (new Tracer()) {
            if (_canvasVirtualControl == null)
               return;
            if (double.IsNaN(_canvasVirtualControl.Width) || double.IsNaN(_canvasVirtualControl.Height))
               return;
            //if ((_canvasVirtualControl.Size.Width == 0) || (_canvasVirtualControl.Size.Height == 0))
            //   return;
            if (!modifiedCells.Any())
               return;

            if (_alreadyPainted && ReferenceEquals(Mosaic.Matrix, modifiedCells)) {
               return;
            } else {
               System.Diagnostics.Debug.Assert(!_alreadyPainted);
            }

            _toRepaint.UnionWith(modifiedCells);

#if DEBUG
            var size = new SizeDouble(_canvasVirtualControl.Width, _canvasVirtualControl.Height); // double values
                                                                                                  //var size = _canvasVirtualControl.Size;                                                // int values
            var tmp = new Windows.Foundation.Rect(0, 0, size.Width, size.Height);
#endif
            foreach (var cell in modifiedCells) {
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
               _canvasVirtualControl.Invalidate(rc.ToWinRect());
            }
         }
      }

      ISet<BaseCell> _toRepaint = new HashSet<BaseCell>();
      bool _alreadyPainted = false;
      private void OnRegionsInvalidated(CanvasVirtualControl sender, CanvasRegionsInvalidatedEventArgs ev) {
         using (new Tracer()) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _canvasVirtualControl));
            if (!_toRepaint.Any())
               return;

            _alreadyPainted = true;
            var invalidatedRegions = ev.InvalidatedRegions;
            foreach (var region in invalidatedRegions) {
               using (var ds = sender.CreateDrawingSession(region)) {
                  Repaint(ds, region);
               }
            }
            _alreadyPainted = false;
            System.Diagnostics.Debug.Assert(!_toRepaint.Any());
         }
      }

      private void Repaint(CanvasDrawingSession ds, Windows.Foundation.Rect region) {
         using (new Tracer()) {
            var p = new PaintableWin2D(ds);
            var pc = PaintContext;
            // paint all cells
            var sizeMosaic = Mosaic.SizeField;
            var cellPaint = CellPaint;
#if true
            var toRepaintAfter = new HashSet<BaseCell>();
            foreach (var cell in _toRepaint) {
               var tmp = new Windows.Foundation.Rect(region.X, region.Y, region.Width, region.Height);
               tmp.Intersect(cell.getRcOuter().ToWinRect());
               var intersected = (tmp != Windows.Foundation.Rect.Empty);
               if (intersected) {
                  cellPaint.Paint(cell, p, pc);
               } else {
                  toRepaintAfter.Add(cell);
               }
            }
            _toRepaint.Clear();
            _toRepaint = toRepaintAfter;
#else
            foreach (var cell in Mosaic.Matrix) {
               var cell = Mosaic.getCell(i, j);
               var tmp = new Windows.Foundation.Rect(region.X, region.Y, region.Width, region.Height);
               tmp.Intersect(cell.getRcOuter().ToWinRect());
               var intersected = (tmp != Windows.Foundation.Rect.Empty);
               if (intersected)
                  cellPaint.Paint(cell, p, pc);
            }
#endif
         }
      }


      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            MineImg.Dispose();
            //FlagImg.Dispose();
            PaintContext = null; // call setter - unsubscribe & dispose
         }
      }

   }

}
