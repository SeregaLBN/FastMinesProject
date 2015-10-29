using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.uwp.draw.mosaic.xaml
{
   /// <summary>
   /// Helper class for drawing info (over xaml shapes)
   /// </summary>
   public class CellPaintShapes : CellPaint<PaintableShapes>
   {
      private readonly IDictionary<Color, Brush> _brushCacheMap;

      public CellPaintShapes()
      {
         _brushCacheMap = new Dictionary<Color, Brush>();
      }

      /// <summary> find cached solid brush. if not exist - create it </summary>
      protected Brush FindBrush(Color clr)
      {
         if (!_brushCacheMap.ContainsKey(clr))
            _brushCacheMap.Add(clr, new SolidColorBrush(clr.ToWinColor()));
         return _brushCacheMap[clr];
      }
      protected Brush BrushBorderShadow => FindBrush(GContext.PenBorder.ColorShadow);
      protected Brush BrushBorderLight => FindBrush(GContext.PenBorder.ColorLight);

      public override void Paint(BaseCell cell, PaintableShapes binder) {
         // all paint
         PaintComponent(cell, binder);
         PaintBorder(cell, binder);
      }

      public override void PaintBorder(BaseCell cell, PaintableShapes binder) {
         // TODO set pen width
         //... = gContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, binder);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(GContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      public override void PaintBorderLines(BaseCell cell, PaintableShapes binder)
      {
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
               var point = cell.getRegion().getPoint(p);
               point.Move(GContext.Padding.Left, GContext.Padding.Top);
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
               point.Move(GContext.Bound);
               points.Add(point.ToWinPoint());
            }
         }
         poly.Points = points;
#endif
         var open = (cell.State.Status == EState._Open);
         var down = cell.State.Down || open;
         poly.StrokeThickness = open ? (GContext.PenBorder.Width * 2) : GContext.PenBorder.Width;
         poly.Stroke = down ? BrushBorderLight : BrushBorderShadow;
         Canvas.SetZIndex(poly, open ? 1 : down ? 3 : 2);
         // TODO граница региона должна быть двухцветной...
      }

      public override void PaintComponent(BaseCell cell, PaintableShapes binder) {
         PaintComponentBackground(cell, binder);

         var rcInner = cell.getRcInner(GContext.PenBorder.Width);
         rcInner.moveXY(GContext.Padding.Left, GContext.Padding.Top);
         var txt = binder.Txt;
         var image = binder.Img;

         ImageSource srcImg = null;
         if ((GContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            srcImg = GContext.ImgFlag;
         } else if ((GContext.ImgMine != null) &&
                    (cell.State.Status == EState._Open) &&
                    (cell.State.Open == EOpen._Mine)) {
            srcImg = GContext.ImgMine;
         }

         // output Pictures
         if (srcImg != null) {
            image.Source = srcImg;
            image.Stretch = Stretch.UniformToFill;
            image.Width = rcInner.width;
            image.Height = rcInner.height;
            Canvas.SetLeft(image, rcInner.left());
            Canvas.SetTop(image, rcInner.top());
            Canvas.SetZIndex(image, 5);
            image.Visibility = Visibility.Visible;
            txt.Visibility = Visibility.Collapsed;
         } else
         // output text
         {
            image.Visibility = Visibility.Collapsed;
            string szCaption;
            Color txtColor;
            if (cell.State.Status == EState._Close) {
               txtColor = GContext.ColorText.GetColorClose((int)cell.State.Close.Ordinal());
               szCaption = cell.State.Close.ToCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            } else {
               txtColor = GContext.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
               szCaption = cell.State.Open.ToCaption();
            }
            if (string.IsNullOrWhiteSpace(szCaption)) {
               txt.Visibility = Visibility.Collapsed;
            } else {
               txt.Visibility = Visibility.Visible;

               if (cell.State.Down)
                  rcInner.moveXY(GContext.PenBorder.Width, GContext.PenBorder.Width);
               txt.Text = szCaption;
               txt.TextAlignment = TextAlignment.Center;
               txt.FontFamily = GContext.FontFamily;
               txt.FontStyle = GContext.FontStyle;
               txt.FontSize = GContext.FontSize;
               txt.Foreground = FindBrush(txtColor);
               Canvas.SetLeft(txt, rcInner.left());
               Canvas.SetTop(txt, rcInner.top());
               txt.Width = rcInner.width;
               txt.Height = rcInner.height;
               Canvas.SetZIndex(txt, 4);
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      public override void PaintComponentBackground(BaseCell cell, PaintableShapes binder) {
         Color clr;
         if (GContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
            clr = GraphicContext.DefaultBackgroundFillColor; // TODO ??? мож прозрачное..
         else
            clr = cell.getBackgroundFillColor(
               GContext.BkFill.Mode,
               GraphicContext.DefaultBackgroundFillColor,
               GContext.BkFill.GetColor
               );
         binder.Poly.Fill = FindBrush(clr);
      }
   }
}