using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using System.Threading.Tasks;
using Windows.UI.Popups;
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
using fmg.winrt.draw;
using fmg.winrt.draw.mosaic;
using fmg.winrt.draw.mosaic.xaml;

namespace fmg.winrt.mosaic {
   public class MosaicExt : Mosaic {
      private IDictionary<BaseCell, PaintableShapes> _xamlBinder;
      private MosaicGraphicContext _gContext;
      private ICellPaint<PaintableShapes> _cellPaint;
      private Panel _container;

      public MosaicExt() {}

      public MosaicExt(Size sizeField, EMosaic mosaicType, int minesCount, int area) :
         base(sizeField, mosaicType, minesCount, area)
      {}

      private void UnbindXaml() {
         Container.Children.Clear();
         XamlBinder.Clear();
      }

      private void BindXamlToMosaic() {
         //UnbindXaml();
         var sizeMosaic = SizeField;
         for (var i = 0; i < sizeMosaic.width; i++)
            for (var j = 0; j < sizeMosaic.height; j++) {
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

      public override async Task SetParams(Size? newSizeField, EMosaic? newMosaicType, int? newMinesCount) {
         if (this._mosaicType != newMosaicType)
            _cellPaint = null;

         var rebind = (this.SizeField != newSizeField) ||
                      (this.MosaicType != newMosaicType) ||
                      (this.MinesCount != newMinesCount);
         if (rebind)
            UnbindXaml();
         await base.SetParams(newSizeField, newMosaicType, newMinesCount);
         if (rebind)
            BindXamlToMosaic();

         Repaint();
         //Container.InvalidateArrange(); // Revalidate();
      }

      public MosaicGraphicContext GraphicContext {
         get {
            if (_gContext == null) {
               _gContext = new MosaicGraphicContext();
               //changeFontSize(_gContext.PenBorder, Area);
               _gContext.PropertyChanged += OnPropertyChange; // изменение контекста -> перерисовка мозаики
            }
            return _gContext;
         }
      }

      public ICellPaint<PaintableShapes> CellPaint {
         get { return _cellPaint ?? (_cellPaint = new CellPaintShapes(GraphicContext)); }
      }

      private IDictionary<BaseCell, PaintableShapes> XamlBinder {
         get { return _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, PaintableShapes>()); }
      }

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
            for (var i = 0; i < sizeMosaic.width; i++)
               for (var j = 0; j < sizeMosaic.height; j++) {
                  var cell = base.getCell(i, j);
                  CellPaint.Paint(cell, XamlBinder[cell]);
               }
         } finally {
            _alreadyPainted = false;
         }
      }

      public override async Task GameNew() {
         var mode = 1 + new Random().Next(CellFactory.CreateAttributeInstance(MosaicType, Area).getMaxBackgroundFillModeValue());
         //System.Diagnostics.Debug.WriteLine("GameNew: new bkFill mode " + mode);
         GraphicContext.BkFill.Mode = (int)mode;
         await base.GameNew();
         Repaint();
      }

      protected override void GameBegin(BaseCell firstClickCell) {
         GraphicContext.BkFill.Mode = 0;
         base.GameBegin(firstClickCell);
      }

      /// <summary> преобразовать экранные координаты в ячейку поля мозаики </summary>
      private BaseCell CursorPointToCell(Point point) {
         foreach (var cell in _matrix)
            //if (cell.getRcOuter().Contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
               if (cell.PointInRegion(point))
                  return cell;
         return null;
      }

      protected override async Task<bool> RequestToUser_RestoreLastGame() {
         var dlg = new MessageDialog("Restore last game?", "Question");
         const string okLabel = "Ok";
         dlg.Commands.Add(new UICommand(okLabel));
         dlg.Commands.Add(new UICommand("Cancel"));
         dlg.DefaultCommandIndex = 0;
         dlg.CancelCommandIndex = 1;

         var cmd = await dlg.ShowAsync();
         return okLabel.Equals(cmd.Label);
      }

      public override int Area {
         //get { return base.Area; }
         set {
            var oldVal = Area;
            base.Area = value;
            var newVal = Area;
            if (oldVal != newVal) {
               // см. комент - сноску 1
               ChangeFontSize(GraphicContext.PenBorder, value);
               Repaint();
            }
         }
      }

      public async Task<bool> MousePressed(Point clickPoint, bool isLeftMouseButton) {
         using (new Tracer("MosaicExt::MousePressed", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? await OnLeftButtonDown(CursorPointToCell(clickPoint))
               : await OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public bool MouseReleased(Point clickPoint, bool isLeftMouseButton) {
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
            if (CellDown.State.Down)
               return OnLeftButtonUp(null);
            else
               return OnRightButtonUp();
         }
      }

      private void OnPropertyChange(object sender, PropertyChangedEventArgs e) {
         if ((sender is GraphicContext) && "PenBorder".Equals(e.PropertyName)) {
            // см. комент - сноску 1
            var gc = sender as GraphicContext;
            ChangeFontSize(gc.PenBorder, Area);
         }

         if (sender is GraphicContext)
            Repaint();
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      public void ChangeFontSize() { ChangeFontSize(GraphicContext.PenBorder, Area); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize(PenBorder penBorder, int area) {
         GraphicContext.FontSize = (int)CellAttr.CalcSq(area, penBorder.Width);
      }
   }
}