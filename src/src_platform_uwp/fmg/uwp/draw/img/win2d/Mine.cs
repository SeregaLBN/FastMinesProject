using Microsoft.Graphics.Canvas;
using Logo = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> mine image </summary>
   public class Mine {

      private CanvasBitmap _img;
      private readonly ICanvasResourceCreator _rc;

      public Mine(ICanvasResourceCreator resourceCreator) {
         _rc = resourceCreator;
      }

      public CanvasBitmap Image {
         get {
            if (_img == null) {
               var img = new Logo(_rc) {
                  UseGradient = false,
                  SizeInt = 150,
                  PaddingInt = 10
               };
               for (var i = 0; i < img.Palette.Length; ++i)
                  //img.Palette[i].v = 75;
                  img.Palette[i].Grayscale();
               _img = img.Image;
            }
            return _img;
         }
      }

   }

}
