using Windows.UI.Xaml.Media.Imaging;
using fmg.common;

namespace fmg.winrt.view.res.img {

   /// <summary> картинка для фоновой паузы </summary>
   public class BackgroundPause {
      private readonly WriteableBitmap _bmp;

      public BackgroundPause() {
         const int iconWidth = 1000, iconHeight = 1000;
         _bmp = new WriteableBitmap(iconWidth, iconHeight);

         // fill background (only transparent color)
         //_bmp.FillRectangle(5, 5, IconWidth - 10, IconHeight - 10, (Windows.UI.Color)new Color(0x00123456);

         // тело смайла
         _bmp.FillEllipse(5, 5, iconWidth - 10, iconHeight - 10, new Color(0x00FFE600).ToWinColor());

         // глаза
         var clr = new Color(0x00000000);
         _bmp.FillEllipse(330, 150, 98, 296, clr.ToWinColor());
         _bmp.FillEllipse(570, 150, 98, 296, clr.ToWinColor());

         // smile
         //g2d.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
         //_bmp.DrawCurve(103, -133, 795, 1003, 207, 126);

         // ямочки на щеках
         //g.drawArc(90, 580, 180, 180, 85, 57);
         //g.drawArc(730, 580, 180, 180, 38, 57);
      }

      public WriteableBitmap Image {
         get { return _bmp; }
      }
   }
}