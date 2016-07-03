using Microsoft.Graphics.Canvas;
using Logo = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> mine image </summary>
   public class Mine {

      private readonly CanvasBitmap _bmp;

      public Mine() {
         var img = new Logo {
            UseGradient = false,
            SizeInt = 150,
            PaddingInt = 10
         };
         for (var i = 0; i < img.Palette.Length; ++i)
            //img.Palette[i].v = 75;
            img.Palette[i].Grayscale();
         _bmp = img.Image;
      }

      public CanvasBitmap Image {
         get { return _bmp; }
      }

   }

}
