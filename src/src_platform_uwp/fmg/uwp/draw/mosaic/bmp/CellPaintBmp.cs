using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.uwp.draw.mosaic.bmp
{
   /// <summary>
   /// Helper class for drawing info (ower WriteableBitmap)
   /// </summary>
   public class CellPaintBmp : CellPaint<PaintableBmp>
   {
      public override void Paint(BaseCell cell, PaintableBmp paint)
      {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         // all paint
         PaintComponent(cell, paint);
         PaintBorder(cell, paint);
      }

      public override void PaintBorder(BaseCell cell, PaintableBmp paint) {
         // TODO set pen width
         //... = gContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, paint);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      public override void PaintBorderLines(BaseCell cell, PaintableBmp paint) {
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         if (GContext.IconicMode) {
            paint.Bmp.DrawPolyline(cell.getRegion().RegionAsXyxyxySequence(GContext.Bound, true), (down ? GContext.PenBorder.ColorLight : GContext.PenBorder.ColorShadow).ToWinColor());
         } else {
            var color = down ? GContext.PenBorder.ColorLight : GContext.PenBorder.ColorShadow;
            var s = cell.getShiftPointBorderIndex();
            var v = cell.Attr.getVertexNumber(cell.getDirection());
            for (var i=0; i < v; i++) {
               var p1 = cell.getRegion().getPoint(i);
               p1.Move(GContext.Bound);
               var p2 = (i != (v - 1)) ? cell.getRegion().getPoint(i + 1) : cell.getRegion().getPoint(0);
               p2.Move(GContext.Bound);
               if (i == s)
                  color = down ? GContext.PenBorder.ColorShadow : GContext.PenBorder.ColorLight;
               paint.Bmp.DrawLine(p1.x, p1.y, p2.x, p2.y, color.ToWinColor());
            }
         }
      }

      public override void PaintComponent(BaseCell cell, PaintableBmp paint)
      {
         PaintComponentBackground(cell, paint);

         var rcInner = cell.getRcInner(GContext.PenBorder.Width);
         rcInner.moveXY(GContext.Bound);

         WriteableBitmap srcImg = null;
         if ((GContext.ImgFlag != null) &&
             (cell.State.Status == EState._Close) &&
             (cell.State.Close == EClose._Flag))
         {
            srcImg = GContext.ImgFlag;
         }
         else if ((GContext.ImgMine != null) &&
                  (cell.State.Status == EState._Open) &&
                  (cell.State.Open == EOpen._Mine))
         {
            srcImg = GContext.ImgMine;
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
               txtColor = GContext.ColorText.GetColorClose((int) cell.State.Close.Ordinal());
               szCaption = cell.State.Close.ToCaption();
               //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
               //szCaption = ""+cell.getDirection(); // debug
            }
            else
            {
               txtColor = GContext.ColorText.GetColorOpen((int) cell.State.Open.Ordinal());
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
               paint.Bmp.DrawString(szCaption, rcInner.ToWinRect(), GContext.FontFamily.Source, GContext.FontSize, txtColor.ToWinColor());
               //paint.Bmp.DrawRectangle(rcInner.left(), rcInner.top(), rcInner.right(), rcInner.bottom(), Color.RED.ToWinColor()); // debug
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      public override void PaintComponentBackground(BaseCell cell, PaintableBmp paint)
      {
         //if (gContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
         //   return;
         var color = cell.getBackgroundFillColor(
            GContext.BkFill.Mode,
            DefaultBackgroundFillColor,
            GContext.BkFill.GetColor
            );
         paint.Bmp.FillPolygon(cell.getRegion().RegionAsXyxyxySequence(GContext.Bound, true), color.ToWinColor());
      }

   }
}