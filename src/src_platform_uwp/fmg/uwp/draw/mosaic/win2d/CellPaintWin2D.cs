using System;
using System.Linq;
using Windows.UI.Text;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.Text;

namespace fmg.uwp.draw.mosaic.win2d {

   /// <summary> Class for drawing cell into (ower <see cref="CanvasBitmap"/>) </summary>
   public class CellPaintWin2D : CellPaint<PaintableWin2D, CanvasBitmap> {

      public override void Paint(BaseCell cell, PaintableWin2D paint, PaintUwpContext<CanvasBitmap> paintContext)
      {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         // all paint
         PaintComponent(cell, paint, paintContext);
         PaintBorder(cell, paint, paintContext);
      }

      public override void PaintBorder(BaseCell cell, PaintableWin2D paint, PaintUwpContext<CanvasBitmap> paintContext) {
         // TODO set pen width
         //... = PaintContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, paint, paintContext);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         //paint.DrawingSession.DrawRectangle(rcInner.ToWinRect(), Color.Magenta.ToWinColor(), 21);
      }

      /// <summary> draw border lines </summary>
      public override void PaintBorderLines(BaseCell cell, PaintableWin2D paint, PaintUwpContext<CanvasBitmap> paintContext) {
         var ds = paint.DrawingSession;
         var region = cell.getRegion();
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         var color = (down ? paintContext.PenBorder.ColorLight : paintContext.PenBorder.ColorShadow).ToWinColor();
         var borderWidth = paintContext.PenBorder.Width;
         if (paintContext.IconicMode) {
            using (var geom = ds.BuildGeom(region)) {
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
                     color =
                        (down ? paintContext.PenBorder.ColorShadow : paintContext.PenBorder.ColorLight).ToWinColor();
                  ds.DrawLine(p1.ToVector2(), p2.ToVector2(), color, borderWidth, css);
               }
            }
         }
      }

      public override void PaintComponent(BaseCell cell, PaintableWin2D paint, PaintUwpContext<CanvasBitmap> paintContext) {
         var ds = paint.DrawingSession;
         PaintComponentBackground(cell, paint, paintContext);

         var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
         rcInner.MoveXY(paintContext.Padding.Left, paintContext.Padding.Top);

         CanvasBitmap srcImg = null;
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
            var srcRc = new Windows.Foundation.Rect(0, 0, srcImg.Size.Width, srcImg.Size.Height);
            ds.DrawImage(srcImg, destRc, srcRc);
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
               using (var ctf = new CanvasTextFormat() {
                  FontSize = paintContext.FontInfo.Size,
                  FontFamily = paintContext.FontInfo.Name,
                  FontStyle = FontStyle.Normal,
                  FontWeight = paintContext.FontInfo.Bold ? FontWeights.Bold : FontWeights.Normal,
                  HorizontalAlignment = CanvasHorizontalAlignment.Center,
                  VerticalAlignment = CanvasVerticalAlignment.Center,
               }) {
                  ds.DrawText(szCaption, rcInner.ToWinRect(), txtColor.ToWinColor(), ctf);
               }
               //ds.DrawRectangle(rcInner.ToWinRect(), Color.Red.ToWinColor()); // debug
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      public override void PaintComponentBackground(BaseCell cell, PaintableWin2D paint, PaintUwpContext<CanvasBitmap> paintContext)
      {
         //if (PaintContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
         //   return;
         var ds = paint.DrawingSession;
         var color = cell.getBackgroundFillColor(
            paintContext.BkFill.Mode,
            paintContext.BackgroundColor,
            paintContext.BkFill.GetColor
            );
         using (var geom = ds.BuildGeom(cell.getRegion())) {
            ds.FillGeometry(geom, paintContext.Padding.LeftTopOffset.ToVector2(), color.ToWinColor());
         }
      }

   }

}
