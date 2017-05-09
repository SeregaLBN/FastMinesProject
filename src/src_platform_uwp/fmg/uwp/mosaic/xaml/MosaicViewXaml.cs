using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Shapes;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.xaml;

namespace fmg.uwp.mosaic.xaml {

   /// <summary> MVC view. UWP Xaml shapes implementation </summary>
   public class MosaicViewXaml : AMosaicView<PaintableShapes, ImageSource, PaintUwpContext<ImageSource>> {

      private Panel _control;
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

      public override ICellPaint<PaintableShapes, ImageSource, PaintUwpContext<ImageSource>> CellPaint => _cellPaint ?? (_cellPaint = new CellPaintShapes());

      public override SizeDouble Size {
         get {
            return new SizeDouble(Control?.Width ?? 0, Control?.Height ?? 0);
         }
      }

      public override void Invalidate(IEnumerable<BaseCell> modifiedCells = null) {
         AsyncRunner.InvokeFromUiLater(() => Repaint(modifiedCells, null));
      }

      public override void Repaint(IEnumerable<BaseCell> modifiedCells = null, common.geom.RectDouble? __ignore__ = null) {
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

      protected override void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Mosaic));
         switch (ev.PropertyName) {
         case nameof(Mosaic.MosaicType):
         case nameof(Mosaic.Matrix):
            UnbindXaml();
            BindXamlToMosaic();
            break;
         }
         base.OnMosaicPropertyChanged(sender, ev);
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      protected override void ChangeSizeImagesMineFlag() {
         // none
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            UnbindXaml();
         }
      }

   }

}
