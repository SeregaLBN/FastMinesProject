using System;
using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.draw.mosaic.wbmp {

   /// <summary> Class for drawing cell into (ower <see cref="WriteableBitmap"/>) </summary>
   public class CellPaintWBmp : CellPaint<PaintableWBmp, WriteableBitmap> {

      public override void Paint(BaseCell cell, PaintableWBmp paint, PaintUwpContext<WriteableBitmap> paintContext)
      {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         // all paint
         PaintComponent(cell, paint, paintContext);
         PaintBorder(cell, paint, paintContext);
      }

      protected override void PaintBorder(BaseCell cell, PaintableWBmp paint, PaintUwpContext<WriteableBitmap> paintContext) {
         // TODO set pen width
         //... = PaintContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, paint, paintContext);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         //paint.Bmp.DrawRectangle(rcInner.ToWinRect(), Color.Magenta.ToWinColor());
      }

      /// <summary> draw border lines </summary>
      protected override void PaintBorderLines(BaseCell cell, PaintableWBmp paint, PaintUwpContext<WriteableBitmap> paintContext) {
         var region = cell.getRegion();
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         var color = (down ? paintContext.PenBorder.ColorLight : paintContext.PenBorder.ColorShadow).ToWinColor();
         var borderWidth = paintContext.PenBorder.Width;
         if (paintContext.IconicMode) {

            var points = region.RegionDoubleAsXyxyxySequence(paintContext.Padding, true).ToArray();
            if (borderWidth == 1)
               paint.Bmp.DrawPolyline(points, color);
            else
               for (var i = 0; i < points.Length - 2; i += 2)
                  try {
                     paint.Bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], color, borderWidth);
                  } catch(IndexOutOfRangeException ex) {
                     System.Diagnostics.Debug.WriteLine("WTF! " + ex);
                     paint.Bmp.DrawLine(points[i], points[i + 1], points[i + 2], points[i + 3], color);
                  }
         } else {
            var s = cell.getShiftPointBorderIndex();
            var v = cell.Attr.getVertexNumber(cell.getDirection());
            for (var i=0; i < v; i++) {
               var p1 = region.GetPoint(i);
               p1.Move(paintContext.Padding.Left, paintContext.Padding.Top);
               var p2 = (i != (v - 1)) ? region.GetPoint(i + 1) : region.GetPoint(0);
               p2.Move(paintContext.Padding.Left, paintContext.Padding.Top);
               if (i == s)
                  color = (down ? paintContext.PenBorder.ColorShadow : paintContext.PenBorder.ColorLight).ToWinColor();
               try {
                  paint.Bmp.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, color, borderWidth);
               } catch (IndexOutOfRangeException ex) {
                  System.Diagnostics.Debug.WriteLine("WTF! " + ex);
                  paint.Bmp.DrawLine((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, color);
               }
            }
         }
      }

      protected override void PaintComponent(BaseCell cell, PaintableWBmp paint, PaintUwpContext<WriteableBitmap> paintContext)
      {
         PaintComponentBackground(cell, paint, paintContext);

         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);

         // output Pictures
         if ((paintContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            PaintImage(cell, paint, paintContext, paintContext.ImgFlag);
         }
         else if ((paintContext.ImgMine != null) &&
                  (cell.State.Status == EState._Open) &&
                  (cell.State.Open == EOpen._Mine))
         {
            PaintImage(cell, paint, paintContext, paintContext.ImgMine);
         } else
         // output text
         {
            string szCaption;
            Color txtColor;
            if (cell.State.Status == EState._Close)
            {
               txtColor = paintContext.ColorText.GetColorClose((int) cell.State.Close.Ordinal());
               szCaption = cell.State.Close.ToCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            }
            else
            {
               txtColor = paintContext.ColorText.GetColorOpen((int) cell.State.Open.Ordinal());
               szCaption = cell.State.Open.ToCaption();
            }
            if (!string.IsNullOrWhiteSpace(szCaption)) {
               if (cell.State.Down)
                  rcInner.MoveXY(1, 1);
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
               paint.Bmp.DrawString(szCaption, rcInner.ToWinRect(), paintContext.FontInfo.Name, paintContext.FontInfo.Size, txtColor.ToWinColor());
               //paint.Bmp.DrawRectangle(rcInner.left(), rcInner.top(), rcInner.right(), rcInner.bottom(), Color.RED.ToWinColor()); // debug
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      protected override void PaintComponentBackground(BaseCell cell, PaintableWBmp paint, PaintUwpContext<WriteableBitmap> paintContext)
      {
         //if (PaintContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
         //   return;
         var color = cell.getBackgroundFillColor(
            paintContext.BkFill.Mode,
            paintContext.BackgroundColor,
            paintContext.BkFill.GetColor
            );
         paint.Bmp.FillPolygon(cell.getRegion().RegionDoubleAsXyxyxySequence(paintContext.Padding, true).ToArray(), color.ToWinColor());
      }

      protected override void PaintImage(BaseCell cell, PaintableWBmp paint, PaintUwpContext<WriteableBitmap> paintContext, WriteableBitmap img) {
         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);
         var destRc = rcInner.ToWinRect();
         var srcRc = new Windows.Foundation.Rect(0, 0, img.PixelWidth, img.PixelHeight);
         paint.Bmp.Blit(destRc, img, srcRc);
      }

   }
}