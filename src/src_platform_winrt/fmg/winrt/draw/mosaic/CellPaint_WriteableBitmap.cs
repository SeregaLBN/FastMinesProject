using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Shapes;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.winrt.draw.mosaic
{
   public class PaintableWriteableBitmap : WriteableBitmap, IPaintable { }

   /// <summary>
   /// Helper class for drawing info
   /// </summary>
   public class CellPaint_
   {
      protected GraphicContext gContext;
      private IDictionary<Color, Brush> _brushCacheMap;
      private IList<FontFamily> _fontFamilies;
      public const string DRAW_BMP_FONT_NAME = "NirmalaUI";
      public const int DRAW_BMP_FONT_SIZE = 30;


      public static async Task RegisterFont() {
         await BitmapFont.RegisterFont(DRAW_BMP_FONT_NAME, DRAW_BMP_FONT_SIZE);
      }

      public CellPaint(GraphicContext gContext)
      {
         //BitmapFont.RegisterFont(DRAW_BMP_FONT_NAME, DRAW_BMP_FONT_SIZE);
         this.gContext = gContext;
         DefaultBackgroundFillColor = new UISettings().UIElementColor(UIElementType.ButtonFace).ToFmColor();
         _brushCacheMap = new Dictionary<Color, Brush>();
         _fontFamilies = new List<FontFamily>(1);
      }

      /// <summary> find cached solid brush. if not exist - create it </summary>
      private Brush FindBrush(Color clr) {
         if (!_brushCacheMap.ContainsKey(clr))
            _brushCacheMap.Add(clr, new SolidColorBrush(clr.ToWinColor()));
         return _brushCacheMap[clr];
      }
      private Brush BrushBorderShadow {
         get { return FindBrush(gContext.PenBorder.ColorShadow); }
      }
      private Brush BrushBorderLight {
         get { return FindBrush(gContext.PenBorder.ColorLight); }
      }

      /// <summary> find cached font. if not exist - create it </summary>
      private FontFamily FindFontFamily(string fontName) {
         var res = _fontFamilies.FirstOrDefault(x => x.Source == fontName);
         if (res == null)
            _fontFamilies.Add(res = new FontFamily(fontName));
         return res;
      }

      public void Paint(BaseCell cell, Tuple<Polygon, TextBlock, Image> binder) {
         // all paint
         PaintComponent(cell, binder);
         PaintBorder(cell, binder);
      }

      public void Paint(BaseCell cell, WriteableBitmap bmp)
      {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         // all paint
         PaintComponent(cell, bmp);
         PaintBorder(cell, bmp);
      }

      public void PaintBorder(BaseCell cell, WriteableBitmap bmp) {
         // TODO set pen width
         //... = gContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, bmp);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      public void PaintBorder(BaseCell cell, Tuple<Polygon, TextBlock, Image> binder) {
         // TODO set pen width
         //... = gContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, binder);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      public void PaintBorderLines(BaseCell cell, WriteableBitmap bmp) {
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         if (gContext.IconicMode) {
            bmp.DrawPolyline(cell.getRegion().RegionAsXyxyxySequence(gContext.Bound, true), (down ? gContext.PenBorder.ColorLight : gContext.PenBorder.ColorShadow).ToWinColor());
         } else {
            var color = down ? gContext.PenBorder.ColorLight : gContext.PenBorder.ColorShadow;
            var s = cell.getShiftPointBorderIndex();
            var v = cell.Attr.getVertexNumber(cell.getDirection());
            for (var i=0; i < v; i++) {
               var p1 = cell.getRegion().getPoint(i);
               p1.Move(gContext.Bound);
               var p2 = (i != (v - 1)) ? cell.getRegion().getPoint(i + 1) : cell.getRegion().getPoint(0);
               p2.Move(gContext.Bound);
               if (i == s)
                  color = down ? gContext.PenBorder.ColorShadow : gContext.PenBorder.ColorLight;
               bmp.DrawLine(p1.x, p1.y, p2.x, p2.y, color.ToWinColor());
            }
         }
      }

      /// <summary> draw border lines </summary>
      public void PaintBorderLines(BaseCell cell, Tuple<Polygon, TextBlock, Image> binder)
      {
         var poly = binder.Item1;
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
               point.Move(gContext.Bound);
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
               point.Move(gContext.Bound);
               points.Add(point.ToWinPoint());
            }
         }
         poly.Points = points;
#endif
         var open = (cell.State.Status == EState._Open);
         var down = cell.State.Down || open;
         poly.StrokeThickness = open ? (gContext.PenBorder.Width * 2) : gContext.PenBorder.Width;
         poly.Stroke = down ? BrushBorderLight : BrushBorderShadow;
         Canvas.SetZIndex(poly, open ? 1 : down ? 3 : 2);
         // TODO граница региона должна быть двухцветной...
      }

      private void PaintComponent(BaseCell cell, WriteableBitmap bmp)
      {
         PaintComponentBackground(cell, bmp);

         var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         rcInner.moveXY(gContext.Bound);

         WriteableBitmap srcImg = null;
         if ((gContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            srcImg = gContext.ImgFlag;
         }
         else if ((gContext.ImgMine != null) &&
                  (cell.State.Status == EState._Open) &&
                  (cell.State.Open == EOpen._Mine))
         {
            srcImg = gContext.ImgMine;
         }

         // output Pictures
         if (srcImg != null) {
            var destRc = rcInner.ToWinRect();
            var srcRc = new Windows.Foundation.Rect(0, 0, srcImg.PixelWidth, srcImg.PixelHeight);
            bmp.Blit(destRc, srcImg, srcRc);
         } else
         // output text
         {
            string szCaption;
            Color txtColor;
            if (cell.State.Status == EState._Close)
            {
               txtColor = gContext.ColorText.GetColorClose((int) cell.State.Close.Ordinal());
               szCaption = cell.State.Close.ToCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            }
            else
            {
               txtColor = gContext.ColorText.GetColorOpen((int) cell.State.Open.Ordinal());
               szCaption = cell.State.Open.ToCaption();
            }
            if (!string.IsNullOrWhiteSpace(szCaption)) {
               if (cell.State.Down)
                  rcInner.moveXY(1, 1);
//#if DEBUG
//               { // debug
//                  var rnd = Windows.Security.Cryptography.CryptographicBuffer.GenerateRandomNumber();
//                  switch (rnd % 7) {
//                  case 0: txtColor = Color.BLUE; break;
//                  case 1: txtColor = Color.MAGENTA; break;
//                  case 2: txtColor = Color.MAROON; break;
//                  case 3: txtColor = Color.NAVY; break;
//                  case 4: txtColor = Color.OLIVE; break;
//                  case 5: txtColor = Color.TEAL; break;
//                  case 6: txtColor = Color.AQUA; break;
//                  }
//               }
//#endif
               bmp.DrawString(szCaption, rcInner.ToWinRect(), DRAW_BMP_FONT_NAME, DRAW_BMP_FONT_SIZE, txtColor.ToWinColor());
               //bmp.DrawRectangle(rcInner.left(), rcInner.top(), rcInner.right(), rcInner.bottom(), Color.RED.ToWinColor()); // debug
            }
         }
      }

      private void PaintComponent(BaseCell cell, Tuple<Polygon, TextBlock, Image> binder) {
         PaintComponentBackground(cell, binder);

         var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         rcInner.moveXY(gContext.Bound);
         var txt = binder.Item2;
         var image = binder.Item3;

         ImageSource srcImg = null;
         if ((gContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            srcImg = gContext.ImgFlag;
         } else if ((gContext.ImgMine != null) &&
                    (cell.State.Status == EState._Open) &&
                    (cell.State.Open == EOpen._Mine)) {
            srcImg = gContext.ImgMine;
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
               txtColor = gContext.ColorText.GetColorClose((int)cell.State.Close.Ordinal());
               szCaption = cell.State.Close.ToCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            } else {
               txtColor = gContext.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
               szCaption = cell.State.Open.ToCaption();
            }
            if (string.IsNullOrWhiteSpace(szCaption)) {
               txt.Visibility = Visibility.Collapsed;
            } else {
               txt.Visibility = Visibility.Visible;

               if (cell.State.Down)
                  rcInner.moveXY(gContext.PenBorder.Width, gContext.PenBorder.Width);
               txt.Text = szCaption;
               txt.TextAlignment = TextAlignment.Center;
               txt.FontFamily = FindFontFamily(gContext.FontFamilyName);
               txt.FontStyle = gContext.FontStyle;
               txt.FontSize = gContext.FontSize;
               txt.Foreground = FindBrush(txtColor);
               Canvas.SetLeft(txt, rcInner.left());
               Canvas.SetTop(txt, rcInner.top());
               txt.Width = rcInner.width;
               txt.Height = rcInner.height;
               Canvas.SetZIndex(txt, 4);
            }
         }
      }

      /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера </summary>
      private Color DefaultBackgroundFillColor { get; set; }

      /// <summary> залить ячейку нужным цветом </summary>
      protected void PaintComponentBackground(BaseCell cell, WriteableBitmap bmp)
      {
         //if (gContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
         //   return;
         var color = cell.getBackgroundFillColor(
            gContext.BkFill.Mode,
            DefaultBackgroundFillColor,
            gContext.BkFill.GetColor
            );
         bmp.FillPolygon(cell.getRegion().RegionAsXyxyxySequence(gContext.Bound, true), color.ToWinColor());
      }

      /// <summary> залить ячейку нужным цветом </summary>
      protected void PaintComponentBackground(BaseCell cell, Tuple<Polygon, TextBlock, Image> binder) {
         Color clr;
         if (gContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
            clr = DefaultBackgroundFillColor; // TODO ??? мож прозрачное..
         else
            clr = cell.getBackgroundFillColor(
               gContext.BkFill.Mode,
               DefaultBackgroundFillColor,
               gContext.BkFill.GetColor
               );
         binder.Item1.Fill = FindBrush(clr);
      }
   }
}