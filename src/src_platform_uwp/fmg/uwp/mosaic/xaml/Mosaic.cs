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
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.core.types.click;
using fmg.data.view.draw;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.xaml;
using fmg.uwp.draw.img.wbmp;

namespace fmg.uwp.mosaic.xaml {

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
         using (new Tracer("MosaicExt::MousePressed", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? Mosaic.OnLeftButtonDown(CursorPointToCell(clickPoint))
               : Mosaic.OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer("MosaicExt::MouseReleased", "isLeftMouseButton="+isLeftMouseButton)) {
            return isLeftMouseButton
               ? Mosaic.OnLeftButtonUp(CursorPointToCell(clickPoint))
               : Mosaic.OnRightButtonUp(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseFocusLost() {
         using (new Tracer("MosaicExt::MouseFocusLost")) {
            if (Mosaic.CellDown == null)
               return null;
            return Mosaic.CellDown.State.Down
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

   }

   public class MosaicView : Disposable {

      private MosaicBase _mosaic;
      private Panel _control;
      private PaintUwpContext<ImageSource> _paintContext;
      private CellPaintShapes _cellPaint;
      private IDictionary<BaseCell, PaintableShapes> _xamlBinder;
      private IDictionary<BaseCell, PaintableShapes> XamlBinder => _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, PaintableShapes>());

      public Panel Control {
         get { return _control; }
         set {
            if (_control != null)
               UnbindXaml();
            _control = value;
            if (_control != null)
               BindXamlToMosaic();
         }
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

      private void UnbindXaml() {
         Control?.Children.Clear();
         XamlBinder.Clear();
      }

      private void BindXamlToMosaic() {
         var container = Control;

         //System.Diagnostics.Debug.Assert(container != null);
         if (container == null)
            return;

         //UnbindXaml();
         var xamlBinder = XamlBinder;
         foreach (var cell in Mosaic.Matrix) {
            var shape = new Polygon();
            var txt = new TextBlock();
            var img = new Image();
            xamlBinder.Add(cell, new PaintableShapes(shape, txt, img));
            container.Children.Add(shape);
            container.Children.Add(txt);
            container.Children.Add(img);
         }
      }

      public PaintUwpContext<ImageSource> PaintContext {
         get {
            if (_paintContext == null)
               PaintContext = new PaintUwpContext<ImageSource>(); // call setter
            return _paintContext;
         }
         private set {
            if (_paintContext != null) {
               _paintContext.PropertyChanged -= OnPaintContextPropertyChanged;
               _paintContext.Dispose();
            }
            _paintContext = value;
            if (_paintContext != null) {
               _paintContext.ImgMine = new Mine().Image;
             //_paintContext.ImgFlag = new Flag().Image;
               _paintContext.PropertyChanged += OnPaintContextPropertyChanged; // изменение контекста -> перерисовка мозаики
            }
         }
      }

      public ICellPaint<PaintableShapes, ImageSource, PaintUwpContext<ImageSource>> CellPaint => CellPaintFigures;
      protected CellPaintShapes CellPaintFigures => _cellPaint ?? (_cellPaint = new CellPaintShapes());


      public void InvalidateCells(IEnumerable<BaseCell> modifiedCells = null) {
         var container = Control;

         //System.Diagnostics.Debug.Assert(container != null);
         if (container == null)
            return;

         var paintContext = PaintContext;
         { // paint background
            var bkb = container.Background as SolidColorBrush;
            var bkc = paintContext.BackgroundColor.ToWinColor();
            if ((bkb == null) || (bkb.Color != bkc))
               container.Background = new SolidColorBrush(bkc);
         }

         // paint all cells
         var cellPaint = CellPaint;
         var xamlBinder = XamlBinder;
         foreach (var cell in modifiedCells ?? Mosaic.Matrix) {
            cellPaint.Paint(cell, xamlBinder[cell], paintContext);
         }
      }

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

            var paintContext = PaintContext;
            { // paint background
               var bkb = Container.Background as SolidColorBrush;
               var bkc = paintContext.BackgroundColor.ToWinColor();
               if ((bkb == null) || (bkb.Color != bkc))
                  Container.Background = new SolidColorBrush(bkc);
            }

            // paint all cells
            var cellPaint = CellPaint;
            var xamlBinder = XamlBinder;
            foreach (var cell in Mosaic.Matrix) {
               cellPaint.Paint(cell, xamlBinder[cell], paintContext);
            }
         } finally {
            _alreadyPainted = false;
         }
      }
      */

      private void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Mosaic));
         switch (ev.PropertyName) {
         case nameof(Mosaic.MosaicType):
            UnbindXaml();
            BindXamlToMosaic();
            ChangeFontSize();
            break;
         case nameof(Mosaic.Area):
            ChangeFontSize(PaintContext.PenBorder);
            break;
         case nameof(Mosaic.Matrix):
            UnbindXaml();
            BindXamlToMosaic();
            InvalidateCells();
            break;
         case MosaicBase.PROPERTY_MODIFIED_CELLS:
            InvalidateCells((ev as IPropertyChangedExEventArgs<IEnumerable<BaseCell>>).NewValue);
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

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            UnbindXaml();
            PaintContext = null; // call setter - unsubscribe & dispose
         }
      }

   }

}
