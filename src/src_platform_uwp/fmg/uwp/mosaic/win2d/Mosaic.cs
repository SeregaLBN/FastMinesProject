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
               Mosaic = new MosaicIntenal(); // call setter
            return _mosaic;
         }
         private set {
            if (_mosaic != null) {
               _mosaic.Dispose();
            }
            _mosaic = value;
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
            if (_view != null) {
               var mosaic = Mosaic;
               _view.Mosaic = mosaic;
               (mosaic as MosaicIntenal).View = _view;
            }
         }
      }

      private class MosaicIntenal : MosaicBase {

         public MosaicView View { get; set; }

         protected override bool CheckNeedRestoreLastGame() {
            // TODO: override
            return base.CheckNeedRestoreLastGame();
         }

         public override bool GameNew() {
            var mode = 1 + new Random(Guid.NewGuid().GetHashCode()).Next(MosaicHelper.CreateAttributeInstance(MosaicType).getMaxBackgroundFillModeValue());
            //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
            View.PaintContext.BkFill.Mode = mode;
            var res = base.GameNew();
            if (!res)
               View.InvalidateCells();
            return res;
         }

         public override void GameBegin(BaseCell firstClickCell) {
            View.PaintContext.BkFill.Mode = 0;
            base.GameBegin(firstClickCell);
         }
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

      private MosaicBase _mosaic;
      private CanvasVirtualControl _control;
      private PaintUwpContext<CanvasBitmap> _paintContext;
      private CellPaintWin2D _cellPaint;
      private MineCanvasBmp _mineImage;
      private FlagCanvasBmp _flagImage;

      public CanvasVirtualControl Control {
         get { return _control; }
         set { _control = value; }
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

      public PaintUwpContext<CanvasBitmap> PaintContext {
         get {
            if (_paintContext == null)
               PaintContext = new PaintUwpContext<CanvasBitmap>(); // call setter
            return _paintContext;
         }
         private set {
            if (_paintContext != null) {
               _paintContext.PropertyChanged -= OnPaintContextPropertyChanged;
               _paintContext.Dispose();
            }
            _paintContext = value;
            if (_paintContext != null) {
               _paintContext.PropertyChanged += OnPaintContextPropertyChanged; // изменение контекста -> перерисовка мозаики
               ChangeSizeImagesMineFlag();
            }
         }
      }

      public ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> CellPaint => CellPaintFigures;
      protected CellPaintWin2D CellPaintFigures => _cellPaint ?? (_cellPaint = new CellPaintWin2D());


      public void InvalidateCells(IEnumerable<BaseCell> modifiedCells = null) {
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


      private void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Mosaic));
         switch (ev.PropertyName) {
         case nameof(MosaicBase.MosaicType):
            ChangeFontSize();
            break;
         case nameof(MosaicBase.Area):
            ChangeFontSize(PaintContext.PenBorder);
            ChangeSizeImagesMineFlag();
            break;
         case nameof(MosaicBase.Matrix):
            InvalidateCells();
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
         //this.InvalidateCells();
         //OnSelfPropertyChanged(nameof(PaintContext));
         //OnSelfPropertyChanged(nameof(PaintContext) + "." + ev.PropertyName);
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize() { ChangeFontSize(PaintContext.PenBorder); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize(PenBorder penBorder) {
         PaintContext.FontInfo.Size = (int)Mosaic.CellAttr.GetSq(penBorder.Width);
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      private void ChangeSizeImagesMineFlag() {
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
            PaintContext = null; // call setter - unsubscribe & dispose
         }
      }

   }

}
