using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Shapes;
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
using fmg.uwp.draw.mosaic.xaml;
using fmg.uwp.draw.img.wbmp;

namespace fmg.uwp.mosaic.xaml {

   public class Mosaic : MosaicBase<PaintableShapes, ImageSource, PaintUwpContext<ImageSource>> {

      private PaintUwpContext<ImageSource> _paintContext;
      private CellPaintShapes _cellPaint;

      public Mosaic() {}

      public Mosaic(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) :
         base(sizeField, mosaicType, minesCount, area)
      {}

      protected override void OnError(string msg) {
#if DEBUG
         System.Diagnostics.Debug.Assert(false, msg);
#else
         base.OnError(msg);
#endif
      }

      public PaintUwpContext<ImageSource> PaintContext {
         get
         {
            if (_paintContext == null) {
               _paintContext = new PaintUwpContext<ImageSource>(false);
               _paintContext.ImgMine = new Mine().Image;
             //_paintContext.ImgFlag = new Flag().Image;
               _paintContext.PropertyChanged += OnPaintContextPropertyChanged; // изменение контекста -> перерисовка мозаики
            }
            return _paintContext;
         }
      }

      public override ICellPaint<PaintableShapes, ImageSource, PaintUwpContext<ImageSource>> CellPaint => CellPaintFigures;
      protected CellPaintShapes CellPaintFigures => _cellPaint ?? (_cellPaint = new CellPaintShapes());

      /*
      protected override void Repaint(IList<BaseCell> needRepaint) {
         if (needRepaint == null)
            Repaint();
         else
            foreach (var cell in needRepaint)
               CellPaint.Paint(cell, XamlBinder[cell], PaintContext);
      }

      private bool _alreadyPainted;
      public void Repaint() {
         if (!XamlBinder.Any())
            return;

         if (_alreadyPainted)
            return;

         try {
            _alreadyPainted = true;

            { // paint background
               var bkb = Container.Background as SolidColorBrush;
               var bkc = PaintContext.BackgroundColor.ToWinColor();
               if ((bkb == null) || (bkb.Color != bkc))
                  Container.Background = new SolidColorBrush(bkc);
            }

            // paint all cells
            var sizeMosaic = SizeField;
            for (var i = 0; i < sizeMosaic.m; i++)
               for (var j = 0; j < sizeMosaic.n; j++) {
                  var cell = base.getCell(i, j);
                  CellPaint.Paint(cell, XamlBinder[cell], PaintContext);
               }
         } finally {
            _alreadyPainted = false;
         }
      }
      */

      public override bool GameNew() {
         var mode = 1 + new Random(Guid.NewGuid().GetHashCode()).Next(MosaicHelper.CreateAttributeInstance(MosaicType, Area).getMaxBackgroundFillModeValue());
         //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
         PaintContext.BkFill.Mode = (int)mode;
         var res = base.GameNew();
         if (!res)
            OnSelfModifiedCellsPropertyChanged(this.Matrix);
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
         using (new Tracer("MosaicExt::MousePressed", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? OnLeftButtonDown(CursorPointToCell(clickPoint))
               : OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("MosaicExt::MouseReleased", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? OnLeftButtonUp(CursorPointToCell(clickPoint))
               : OnRightButtonUp(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseFocusLost() {
         using (new Tracer("MosaicExt::MouseFocusLost")) {
            if (CellDown == null)
               return null;
            return CellDown.State.Down
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
            OnSelfPropertyChanged(null, this.Matrix, PROPERTY_MODIFIED_CELLS);
            break;
         }
      }

      private void OnPaintContextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is PaintContext<ImageSource>);

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
         OnSelfPropertyChanged(null, this.Matrix, PROPERTY_MODIFIED_CELLS);
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
            PaintContext.Dispose();
         }
      }

   }

}
