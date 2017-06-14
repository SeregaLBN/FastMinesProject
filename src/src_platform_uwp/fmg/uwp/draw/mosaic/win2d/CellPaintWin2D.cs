using Windows.UI.Text;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.Text;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.utils.win2d;

namespace fmg.uwp.draw.mosaic.win2d {

   /// <summary> Class for drawing cell into (ower <see cref="CanvasBitmap"/>) </summary>
   public class CellPaintWin2D : CellPaint<PaintableWin2D, CanvasBitmap, PaintWin2DContext> {

      public override void Paint(BaseCell cell, PaintableWin2D paint, PaintWin2DContext paintContext)
      {
         //LoggerSimple.Put("Paint cell=[{0},{1}]", cell.getCoord().x, cell.getCoord().y);
         // ограничиваю рисование только границами своей фигуры
         var ds = paint.DrawingSession;
         var region = cell.getRegion();
         using (var polygon = ds.CreatePolygon(region, paintContext.Padding)) {
            using (var layer = ds.CreateLayer(1, polygon)) {

               // all paint
               PaintComponent(cell, paint, paintContext);
               PaintBorder(cell, paint, paintContext);

            }
         }
      }

      protected override void PaintBorder(BaseCell cell, PaintableWin2D paint, PaintWin2DContext paintContext) {
         // TODO set pen width
         //... = PaintContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, paint, paintContext);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         //paint.DrawingSession.DrawRectangle(rcInner.ToWinRect(), Color.Magenta.ToWinColor(), 21);
      }

      /// <summary> draw border lines </summary>
      protected override void PaintBorderLines(BaseCell cell, PaintableWin2D paint, PaintWin2DContext paintContext) {
         var ds = paint.DrawingSession;
         var region = cell.getRegion();
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         var color = (down ? paintContext.PenBorder.ColorLight : paintContext.PenBorder.ColorShadow).ToWinColor();
         var borderWidth = paintContext.PenBorder.Width;
         if (paintContext.IconicMode) {
            using (var geom = ds.BuildLines(region)) {
               ds.DrawGeometry(geom, paintContext.Padding.LeftTopOffset.ToVector2(), color, borderWidth);
            }
         } else {
            var s = cell.getShiftPointBorderIndex();
            var v = cell.Attr.getVertexNumber(cell.getDirection());
            using (var css = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Triangle,
               EndCap = CanvasCapStyle.Triangle,
            }) {
               for (var i = 0; i < v; i++) {
                  var p1 = region.GetPoint(i);
                  p1.Move(paintContext.Padding.Left, paintContext.Padding.Top);
                  var p2 = (i != (v - 1)) ? region.GetPoint(i + 1) : region.GetPoint(0);
                  p2.Move(paintContext.Padding.Left, paintContext.Padding.Top);
                  if (i == s)
                     color = (down ? paintContext.PenBorder.ColorShadow : paintContext.PenBorder.ColorLight).ToWinColor();
                  ds.DrawLine(p1.ToVector2(), p2.ToVector2(), color, borderWidth, css);
               }
            }
         }
      }

      protected override void PaintComponent(BaseCell cell, PaintableWin2D paint, PaintWin2DContext paintContext) {
         var ds = paint.DrawingSession;
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
               ds.DrawText(szCaption, rcInner.ToWinRect(), txtColor.ToWinColor(), paintContext.Font);
               //ds.DrawRectangle(rcInner.ToWinRect(), Color.Red.ToWinColor()); // debug
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      protected override void PaintComponentBackground(BaseCell cell, PaintableWin2D paint, PaintWin2DContext paintContext)
      {
         //if (PaintContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
         //   return;
         var ds = paint.DrawingSession;
         var color = cell.getBackgroundFillColor(
            paintContext.BkFill.Mode,
            paintContext.BackgroundColor,
            paintContext.BkFill.GetColor
            );
         using (var geom = ds.BuildLines(cell.getRegion())) {
            ds.FillGeometry(geom, paintContext.Padding.LeftTopOffset.ToVector2(), color.ToWinColor());
         }
      }

      protected override void PaintImage(BaseCell cell, PaintableWin2D paint, PaintWin2DContext paintContext, CanvasBitmap img) {
         var ds = paint.DrawingSession;
         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);
         var destRc = rcInner.ToWinRect();
         var srcRc = new Windows.Foundation.Rect(0, 0, img.Size.Width, img.Size.Height);
         ds.DrawImage(img, destRc, srcRc);
      }

   }

}
