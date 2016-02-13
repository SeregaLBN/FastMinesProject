using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Shapes;
using FastMines.Common;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.data.view.draw;
using fmg.uwp.draw;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.xaml;

namespace fmg.uwp.mosaic {
   public class Mosaic : MosaicBase<PaintableShapes> {
      private IDictionary<BaseCell, PaintableShapes> _xamlBinder;
      private CellPaintShapes _cellPaint;
      private Panel _container;

      public Mosaic() {}

      public Mosaic(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) :
         base(sizeField, mosaicType, minesCount, area)
      {}

      private void UnbindXaml() {
         Container.Children.Clear();
         XamlBinder.Clear();
      }

      private void BindXamlToMosaic() {
         //UnbindXaml();
         var sizeMosaic = SizeField;
         for (var i = 0; i < sizeMosaic.m; i++)
            for (var j = 0; j < sizeMosaic.n; j++) {
               var cell = base.getCell(i, j);
               var shape = new Polygon();
               var txt = new TextBlock();
               var img = new Image();
               XamlBinder.Add(cell, new PaintableShapes(shape, txt, img));
               Container.Children.Add(shape);
               Container.Children.Add(txt);
               Container.Children.Add(img);
            }
      }

      public Panel Container {
         get { return _container ?? (_container = new Canvas()); }
      }

      protected override void OnError(string msg) {
#if DEBUG
         System.Diagnostics.Debug.Assert(false, msg);
#else
			base.OnError(msg);
#endif
      }

      public override void SetParams(Matrisize? newSizeField, EMosaic? newMosaicType, int? newMinesCount) {
         if (this._mosaicType != newMosaicType)
            _cellPaint = null;

         var rebind = (this.SizeField != newSizeField) ||
                      (this.MosaicType != newMosaicType) ||
                      (this.MinesCount != newMinesCount);
         if (rebind)
            UnbindXaml();
         base.SetParams(newSizeField, newMosaicType, newMinesCount);
         if (rebind)
            BindXamlToMosaic();

         Repaint();
         //Container.InvalidateArrange(); // Revalidate();
      }

      public MosaicGraphicContext GraphicContext {
         get
         {
            var gContext = CellPaintFigures.GContext as MosaicGraphicContext;
            if (gContext == null) {
               CellPaintFigures.GContext = gContext = new MosaicGraphicContext();
               //changeFontSize(gContext.PenBorder, Area);
               gContext.PropertyChanged += OnPropertyChange; // изменение контекста -> перерисовка мозаики
            }
            return gContext;
         }
      }

      public override ICellPaint<PaintableShapes> CellPaint => CellPaintFigures;
      protected CellPaintShapes CellPaintFigures => _cellPaint ?? (_cellPaint = new CellPaintShapes());

      private IDictionary<BaseCell, PaintableShapes> XamlBinder => _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, PaintableShapes>());

      protected override void Repaint(BaseCell cell) {
         if (cell == null)
            Repaint();
         else
            CellPaint.Paint(cell, XamlBinder[cell]);
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
               var bkc = GraphicContext.ColorBk.ToWinColor();
               if ((bkb == null) || (bkb.Color != bkc))
                  Container.Background = new SolidColorBrush(bkc);
            }

            // paint all cells
            var sizeMosaic = SizeField;
            for (var i = 0; i < sizeMosaic.m; i++)
               for (var j = 0; j < sizeMosaic.n; j++) {
                  var cell = base.getCell(i, j);
                  CellPaint.Paint(cell, XamlBinder[cell]);
               }
         } finally {
            _alreadyPainted = false;
         }
      }

      public override void GameNew() {
         var mode = 1 + new Random(Guid.NewGuid().GetHashCode()).Next(MosaicHelper.CreateAttributeInstance(MosaicType, Area).getMaxBackgroundFillModeValue());
         //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
         GraphicContext.BkFill.Mode = (int)mode;
         base.GameNew();
         Repaint();
      }

      protected override void GameBegin(BaseCell firstClickCell) {
         GraphicContext.BkFill.Mode = 0;
         base.GameBegin(firstClickCell);
      }

      /// <summary> преобразовать экранные координаты в ячейку поля мозаики </summary>
      private BaseCell CursorPointToCell(PointDouble point) {
         return Matrix.AsParallel().FirstOrDefault(cell =>
            //cell.getRcOuter().Contains(point) && // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            cell.PointInRegion(point));
      }

      public override double Area {
         //get { return base.Area; }
         set {
            var oldVal = Area;
            base.Area = value;
            var newVal = Area;
            if (!oldVal.HasMinDiff(newVal)) {
               // см. комент - сноску 1
               ChangeFontSize(GraphicContext.PenBorder);
               Repaint();
            }
         }
      }

      public bool MousePressed(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("MosaicExt::MousePressed", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? OnLeftButtonDown(CursorPointToCell(clickPoint))
               : OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public bool MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("MosaicExt::MouseReleased", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? OnLeftButtonUp(CursorPointToCell(clickPoint))
               : OnRightButtonUp(/*CursorPointToCell(clickPoint)*/);
         }
      }

      public bool MouseFocusLost() {
         using (new Tracer("MosaicExt::MouseFocusLost")) {
            if (CellDown == null)
               return false;
            return CellDown.State.Down
               ? OnLeftButtonUp(null)
               : OnRightButtonUp();
         }
      }

      private void OnPropertyChange(object sender, PropertyChangedEventArgs e) {
         if ((sender is GraphicContext) && "PenBorder".Equals(e.PropertyName)) {
            // см. комент - сноску 1
            var gc = sender as GraphicContext;
            ChangeFontSize(gc.PenBorder);
         }

         if (sender is GraphicContext)
            Repaint();
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      public void ChangeFontSize() { ChangeFontSize(GraphicContext.PenBorder); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize(PenBorder penBorder) {
         GraphicContext.FontSize = (int)CellAttr.GetSq(penBorder.Width);
      }
   }
}