using System;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.geom;

namespace ua.ksn.fmg.view.win_rt.draw.mosaics
{

   /// <summary>
   /// Helper class for drawing info
   /// </summary>
   public class CellPaint
   {
      protected GraphicContext gContext;

      public CellPaint(GraphicContext gContext)
      {
         this.gContext = gContext;
         DefaultBackgroundFillColor = new UISettings().UIElementColor(UIElementType.ButtonFace).Cast();
      }

      private const string DRAW_FONT_NAME = "NirmalaUI";
      private const int DRAW_FONT_SIZE = 30;

      //static CellPaint()
      private bool _registerFont;
      private void RegisterFont()
      {
         if (_registerFont) return;
         try
         {
            BitmapFont.RegisterFont(DRAW_FONT_NAME, DRAW_FONT_SIZE);
            _registerFont = true;
         }
         catch (Exception ex)
         {
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
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

         // debug - визуально провер€ю верность вписанного квадрата (провер€ть при ширине пера около 21)
         //var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      public void PaintBorderLines(BaseCell cell, WriteableBitmap bmp) {
         if (gContext.IconicMode) {
            bmp.DrawPolyline(RegionAsXyxyxySequence(gContext.Bound, cell.getRegion()), (Windows.UI.Color)(cell.State.Down ? gContext.PenBorder.ColorLight : gContext.PenBorder.ColorShadow));
         } else {
            var color = cell.State.Down ? gContext.PenBorder.ColorLight : gContext.PenBorder.ColorShadow;
            var s = cell.getShiftPointBorderIndex();
            var v = cell.Attr.getVertexNumber(cell.getDirection());
            for (var i=0; i < v; i++) {
               var p1 = cell.getRegion().getPoint(i);
               var p2 = (i != (v - 1)) ? cell.getRegion().getPoint(i + 1) : cell.getRegion().getPoint(0);
               if (i == s)
                  color = cell.State.Down ? gContext.PenBorder.ColorShadow : gContext.PenBorder.ColorLight;
               bmp.DrawLine(p1.x + gContext.Bound.width, p1.y + gContext.Bound.height, p2.x + gContext.Bound.width, p2.y + gContext.Bound.height, (Windows.UI.Color)color);
            }
         }
      }

      private void PaintComponent(BaseCell cell, WriteableBitmap bmp)
      {
         PaintComponentBackground(cell, bmp);

         var rcInner = cell.getRcInner(gContext.PenBorder.Width).moveXY(gContext.Bound);

         // output Pictures
         if ((gContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            var destRc = (Windows.Foundation.Rect) rcInner;
            var srcImg = gContext.ImgFlag;
            var srcRc = new Windows.Foundation.Rect(0, 0, srcImg.PixelWidth, srcImg.PixelHeight);
            bmp.Blit(destRc, srcImg, srcRc);
         }
         else if ((gContext.ImgMine != null) &&
                  (cell.State.Status == EState._Open) &&
                  (cell.State.Open == EOpen._Mine))
         {
            var destRc = (Windows.Foundation.Rect) rcInner;
            var srcImg = gContext.ImgMine;
            var srcRc = new Windows.Foundation.Rect(0, 0, srcImg.PixelWidth, srcImg.PixelHeight);
            bmp.Blit(destRc, srcImg, srcRc);
         }
         else
         // output text
         {
            string szCaption;
            Color txtColor;
            if (cell.State.Status == EState._Close)
            {
               txtColor = gContext.ColorText.getColorClose((int) cell.State.Close.Ordinal());
               szCaption = cell.State.Close.toCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            }
            else
            {
               txtColor = gContext.ColorText.getColorOpen((int) cell.State.Open.Ordinal());
               szCaption = cell.State.Open.toCaption();
            }
            if (!string.IsNullOrEmpty(szCaption))
            {
               if (cell.State.Down)
                  rcInner.moveXY(1, 1);
               RegisterFont();
               bmp.DrawString(szCaption, rcInner.left(), rcInner.top(), DRAW_FONT_NAME, DRAW_FONT_SIZE,
                  (Windows.UI.Color) txtColor);
            }
         }
      }

      /// <summary> ÷вет заливки €чейки по-умолчанию. «ависит от текущего UI манагера </summary>
      private Color DefaultBackgroundFillColor { get; set; }

      /// <summary> залить €чейку нужным цветом </summary>
      protected void PaintComponentBackground(BaseCell cell, WriteableBitmap bmp)
      {
         //if (gContext.IconicMode) // когда русуетс€ иконка, а не игровое поле, - делаю попроще...
         //   return;
         var color = cell.getBackgroundFillColor(
            gContext.BkFill.Mode,
            DefaultBackgroundFillColor,
            gContext.BkFill.getColor
            );
         bmp.FillPolygon(RegionAsXyxyxySequence(gContext.Bound, cell.getRegion()), (Windows.UI.Color) color);
      }

      private static int[] RegionAsXyxyxySequence(Size bound, Region region)
      {
         var points = new int[region.CountPoints * 2 + 2];
         int i;
         for (i=0; i < region.CountPoints; i++) {
            var point = region.getPoint(i);
            points[i * 2] = point.x + bound.width;
            points[i * 2 + 1] = point.y + bound.height;
         }
         { // Add the first point also at the end of the array if the line should be closed.
            var point = region.getPoint(0);
            points[i * 2 + 0] = point.x + bound.width;
            points[i * 2 + 1] = point.y + bound.height;
         }
         return points;
      }
   }
}