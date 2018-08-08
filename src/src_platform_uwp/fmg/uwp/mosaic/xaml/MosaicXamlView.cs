using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Shapes;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.xaml {

   /// <summary> MVC: view. UWP Xaml shapes implementation </summary>
   public class MosaicXamlView : MosaicView<Panel, ImageSource, MosaicDrawModel<ImageSource>> {

      private Panel _control;
      class CellShapes {
         public Polygon   Poly { get; set; }
         public TextBlock Txt  { get; set; }
         public Image     Img  { get; set; }
      }
      private IDictionary<BaseCell, CellShapes> _xamlBinder;
      private IDictionary<BaseCell, CellShapes> XamlBinder => _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, CellShapes>());
      private readonly IDictionary<Color, Brush> _brushCacheMap = new Dictionary<Color, Brush>();

      public MosaicXamlView()
         : base(new MosaicDrawModel<ImageSource>())
      {
         _notifier.DeferredNotifications = true;
         ChangeSizeImagesMineFlag();
      }

      static MosaicXamlView() {
         StaticInitializer.Init();
      }

      protected override Panel CreateImage() {
         return Control;
      }

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
         foreach (var cell in Model.Matrix) {
            var shape = new Polygon();
            var txt = new TextBlock();
            var img = new Image();
            xamlBinder.Add(cell, new CellShapes{Poly=shape, Txt=txt, Img=img));
            container.Children.Add(shape);
            container.Children.Add(txt);
            container.Children.Add(img);
         }
      }

      /// <summary> find cached solid brush. if not exist - create it </summary>
      protected Brush FindBrush(Color clr) {
         if (!_brushCacheMap.ContainsKey(clr))
            _brushCacheMap.Add(clr, new SolidColorBrush(clr.ToWinColor()));
         return _brushCacheMap[clr];
      }

      protected override void DrawModified(IEnumerable<BaseCell> requiredCells) {
         var container = Control;

         //System.Diagnostics.Debug.Assert(container != null);
         if (container == null)
            return;

         var m = Model;
         { // paint background
            var bkb = container.Background as SolidColorBrush;
            var bkc = m.BackgroundColor.ToWinColor();
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

      public override void Paint(BaseCell cell, PaintableShapes binder, PaintUwpContext<ImageSource> paintContext) {
         // all paint
         PaintComponent(cell, binder, paintContext);
         PaintBorder(cell, binder, paintContext);
      }

      protected override void PaintBorder(BaseCell cell, PaintableShapes binder, PaintUwpContext<ImageSource> paintContext) {
         // TODO set pen width
         //... = paintContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, binder, paintContext);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      protected override void PaintBorderLines(BaseCell cell, PaintableShapes binder, PaintUwpContext<ImageSource> paintContext) {
         var poly = binder.Poly;
#if true
         if (poly.Points == null)
            poly.Points = new PointCollection();
         { //  check vertex
            var cnt = cell.getRegion().CountPoints;
            var d = (poly.Points.Count != cnt);
            if (d)
               poly.Points.Clear();
            for (var p = 0; p < cnt; p++) {
               var point = cell.getRegion().GetPoint(p);
               point.Move(paintContext.Padding.Left, paintContext.Padding.Top);
               if (d)
                  poly.Points.Add(point.ToWinPoint());
               else
                  poly.Points[p] = point.ToWinPoint();
            }
         }
#else
         var points = new PointCollection();
         { //  check vertex
            var region = cell.getRegion();
            for (var p = 0; p < region.CountPoints; p++) {
               var point = region.getPoint(p);
               point.Move(paintContext.Bound);
               points.Add(point.ToWinPoint());
            }
         }
         poly.Points = points;
#endif
         var open = (cell.State.Status == EState._Open);
         var down = cell.State.Down || open;
         poly.StrokeThickness = open ? (paintContext.PenBorder.Width * 2) : paintContext.PenBorder.Width;
         poly.Stroke = FindBrush(down ? paintContext.PenBorder.ColorLight : paintContext.PenBorder.ColorShadow);
         Canvas.SetZIndex(poly, open ? 1 : down ? 3 : 2);
         // TODO граница региона должна быть двухцветной...
      }

      protected override void PaintComponent(BaseCell cell, PaintableShapes binder, PaintUwpContext<ImageSource> paintContext) {
         PaintComponentBackground(cell, binder, paintContext);

         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);
         var txt = binder.Txt;
         var image = binder.Img;

         // output Pictures
         if ((paintContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag)) {
            PaintImage(cell, binder, paintContext, paintContext.ImgFlag);
         } else if ((paintContext.ImgMine != null) &&
                    (cell.State.Status == EState._Open) &&
                    (cell.State.Open == EOpen._Mine)) {
            PaintImage(cell, binder, paintContext, paintContext.ImgMine);
         } else
         // output text
         {
            image.Visibility = Visibility.Collapsed;
            string szCaption;
            Color txtColor;
            if (cell.State.Status == EState._Close) {
               txtColor = paintContext.ColorText.GetColorClose((int)cell.State.Close.Ordinal());
               szCaption = cell.State.Close.ToCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            } else {
               txtColor = paintContext.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
               szCaption = cell.State.Open.ToCaption();
            }
            if (string.IsNullOrWhiteSpace(szCaption)) {
               txt.Visibility = Visibility.Collapsed;
            } else {
               txt.Visibility = Visibility.Visible;

               if (cell.State.Down)
                  rcInner.MoveXY(paintContext.PenBorder.Width, paintContext.PenBorder.Width);
               txt.Text = szCaption;
               txt.TextAlignment = TextAlignment.Center;
               txt.FontFamily = new FontFamily(paintContext.FontInfo.Name);
               txt.FontStyle = FontStyle.Normal;
               txt.FontWeight = paintContext.FontInfo.Bold ? FontWeights.SemiBold : FontWeights.Normal;
               txt.FontSize = paintContext.FontInfo.Size;
               txt.Foreground = FindBrush(txtColor);
               Canvas.SetLeft(txt, rcInner.Left());
               Canvas.SetTop(txt, rcInner.Top());
               txt.Width = rcInner.Width;
               txt.Height = rcInner.Height;
               Canvas.SetZIndex(txt, 4);
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      protected override void PaintComponentBackground(BaseCell cell, PaintableShapes binder, PaintUwpContext<ImageSource> paintContext) {
         Color clr;
         if (paintContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
            clr = paintContext.BackgroundColor;
         else
            clr = cell.getBackgroundFillColor(
               paintContext.BkFill.Mode,
               paintContext.BackgroundColor,
               paintContext.BkFill.GetColor
               );
         binder.Poly.Fill = FindBrush(clr);
      }

      protected override void PaintImage(BaseCell cell, PaintableShapes binder, PaintUwpContext<ImageSource> paintContext, ImageSource img) {
         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);
         var txt = binder.Txt;
         var image = binder.Img;

         image.Source = img;
         image.Stretch = Stretch.UniformToFill;
         image.Width = rcInner.Width;
         image.Height = rcInner.Height;
         Canvas.SetLeft(image, rcInner.Left());
         Canvas.SetTop(image, rcInner.Top());
         Canvas.SetZIndex(image, 5);
         image.Visibility = Visibility.Visible;
         txt.Visibility = Visibility.Collapsed;
      }

      protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Model));
         switch (ev.PropertyName) {
         case nameof(Model.MosaicType):
         case nameof(Model.Matrix):
            UnbindXaml();
            BindXamlToMosaic();
            break;
         }
         base.OnPropertyModelChanged(sender, ev);
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      protected void ChangeSizeImagesMineFlag() {
         // none
      }

      protected override void Disposing() {
         UnbindXaml();
         base.Dispose();
      }

   }

}
