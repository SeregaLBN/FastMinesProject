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
   public class PaintableWBmp : IPaintable
   {
      public PaintableWBmp (WriteableBitmap bmp) { Bmp = bmp; }
      public WriteableBitmap Bmp { get; private set; }
   }

   /// <summary>
   /// Helper class for drawing info (ower WriteableBitmap)
   /// </summary>
   public class CellPaint_WriteableBitmap : ICellPaint<PaintableWBmp>
   {
      private IList<FontFamily> _fontFamilies;
      public const string DRAW_BMP_FONT_NAME = "NirmalaUI";
      public const int DRAW_BMP_FONT_SIZE = 30;


      public static async Task RegisterFont() {
         await BitmapFont.RegisterFont(DRAW_BMP_FONT_NAME, DRAW_BMP_FONT_SIZE);
      }

      public CellPaint_WriteableBitmap(GraphicContext gContext) :
         base(gContext)
      {
         //BitmapFont.RegisterFont(DRAW_BMP_FONT_NAME, DRAW_BMP_FONT_SIZE);
         _fontFamilies = new List<FontFamily>(1);
      }

      /// <summary> find cached font. if not exist - create it </summary>
      private FontFamily FindFontFamily(string fontName) {
         var res = _fontFamilies.FirstOrDefault(x => x.Source == fontName);
         if (res == null)
            _fontFamilies.Add(res = new FontFamily(fontName));
         return res;
      }

      public override void Paint(BaseCell cell, PaintableWBmp paint)
      {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         // all paint
         PaintComponent(cell, paint);
         PaintBorder(cell, paint);
      }

      public override void PaintBorder(BaseCell cell, PaintableWBmp paint) {
         // TODO set pen width
         //... = gContext.PenBorder.Width;

         // draw lines
         PaintBorderLines(cell, paint);

         // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
         //var rcInner = cell.getRcInner(gContext.PenBorder.Width);
         //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
      }

      /// <summary> draw border lines </summary>
      public override void PaintBorderLines(BaseCell cell, PaintableWBmp paint) {
         var down = cell.State.Down || (cell.State.Status == EState._Open);
         if (gContext.IconicMode) {
            paint.Bmp.DrawPolyline(cell.getRegion().RegionAsXyxyxySequence(gContext.Bound, true), (down ? gContext.PenBorder.ColorLight : gContext.PenBorder.ColorShadow).ToWinColor());
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
               paint.Bmp.DrawLine(p1.x, p1.y, p2.x, p2.y, color.ToWinColor());
            }
         }
      }

      protected override void PaintComponent(BaseCell cell, PaintableWBmp paint)
      {
         PaintComponentBackground(cell, paint);

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
            paint.Bmp.Blit(destRc, srcImg, srcRc);
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
               paint.Bmp.DrawString(szCaption, rcInner.ToWinRect(), DRAW_BMP_FONT_NAME, DRAW_BMP_FONT_SIZE, txtColor.ToWinColor());
               //paint.Bmp.DrawRectangle(rcInner.left(), rcInner.top(), rcInner.right(), rcInner.bottom(), Color.RED.ToWinColor()); // debug
            }
         }
      }

      /// <summary> залить ячейку нужным цветом </summary>
      protected override void PaintComponentBackground(BaseCell cell, PaintableWBmp paint)
      {
         //if (gContext.IconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
         //   return;
         var color = cell.getBackgroundFillColor(
            gContext.BkFill.Mode,
            DefaultBackgroundFillColor,
            gContext.BkFill.GetColor
            );
         paint.Bmp.FillPolygon(cell.getRegion().RegionAsXyxyxySequence(gContext.Bound, true), color.ToWinColor());
      }

   }
}