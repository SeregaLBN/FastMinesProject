using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.draw.mosaic.bmp {

   /// <summary> Class for drawing cell into (ower <see cref="WriteableBitmap"/>) </summary>
   public class CellPaintBmp : CellPaint<PaintableBmp, WriteableBitmap> {

      public override void Paint(BaseCell cell, PaintableBmp paint, PaintUwpContext<WriteableBitmap> paintContext)
      {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         // all paint
         PaintComponent(cell, paint, paintContext);
         PaintBorder(cell, paint, paintContext);
      }

      public override void PaintBorder(BaseCell cell, PaintableBmp paint, PaintUwpContext<WriteableBitmap> paintContext) {
         // TODO set pen width
         //... = PaintContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, paint, paintContext);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(PaintContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      public override void PaintBorderLines(BaseCell cell, PaintableBmp paint, PaintUwpContext<WriteableBitmap> paintContext) {
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         if (paintContext.IconicMode) {

            var points = cell.getRegion().RegionDoubleAsXyxyxySequence(paintContext.Padding, true).ToArray();
            var color = (down ? paintContext.PenBorder.ColorLight : paintContext.PenBorder.ColorShadow).ToWinColor();
            var borderWidth = paintContext.PenBorder.Width;
            if (borderWidth == 1)
               paint.Bmp.DrawPolyline(points, color);
            else
               for (var i = 0; i < points.Length - 2; i += 2) {
                  paint.Bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], color, borderWidth);
               }
         } else {
            var color = (down ? paintContext.PenBorder.ColorLight : paintContext.PenBorder.ColorShadow).ToWinColor();
            var s = cell.getShiftPointBorderIndex();
            var v = cell.Attr.getVertexNumber(cell.getDirection());
            var borderWidth = paintContext.PenBorder.Width;
            for (var i=0; i < v; i++) {
               var p1 = cell.getRegion().GetPoint(i);
               p1.Move(paintContext.Padding.Left, paintContext.Padding.Top);
               var p2 = (i != (v - 1)) ? cell.getRegion().GetPoint(i + 1) : cell.getRegion().GetPoint(0);
               p2.Move(paintContext.Padding.Left, paintContext.Padding.Top);
               if (i == s)
                  color = (down ? paintContext.PenBorder.ColorShadow : paintContext.PenBorder.ColorLight).ToWinColor();
               paint.Bmp.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, color, borderWidth);
            }
         }
      }

      public override void PaintComponent(BaseCell cell, PaintableBmp paint, PaintUwpContext<WriteableBitmap> paintContext)
      {
         PaintComponentBackground(cell, paint, paintContext);

         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);

         WriteableBitmap srcImg = null;
         if ((paintContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            srcImg = paintContext.ImgFlag;
         }
         else if ((paintContext.ImgMine != null) &&
                  (cell.State.Status == EState._Open) &&
                  (cell.State.Open == EOpen._Mine))
         {
            srcImg = paintContext.ImgMine;
         }

         // output Pictures
         if (srcImg != null) {
            var destRc = rcInner.ToWinRect();
            var srcRc = new Windows.Foundation.Rect(0, 0, srcImg.PixelWidth, srcImg.PixelHeight);
            paint.Bmp.Blit(destRc, srcImg, srcRc);
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
      public override void PaintComponentBackground(BaseCell cell, PaintableBmp paint, PaintUwpContext<WriteableBitmap> paintContext)
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

   }
}