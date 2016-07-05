using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Windows.UI.Xaml;
using LogoBmp = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using LogoImg = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> mine image </summary>
   public sealed class Mine {

      public class CanvasBmp : LogoBmp {

         public CanvasBmp(ICanvasResourceCreator resourceCreator)
            : base(resourceCreator)
         {
            Mine.Update<LogoBmp, CanvasBitmap>(this);
         }

      }

      public class CanvasImgSrc : LogoImg {

         public CanvasImgSrc(ICanvasResourceCreator resourceCreator)
            : base(resourceCreator)
         {
            Mine.Update<LogoImg, CanvasImageSource>(this);
         }

      }

      private static void Update<TLogoImage, TImage>(TLogoImage img)
         where TLogoImage: Logo<TImage>
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         img.UseGradient = false;
         img.SizeInt = 150;
         img.PaddingInt = 10;
         for (var i = 0; i < img.Palette.Length; ++i)
            //img.Palette[i].v = 75;
            img.Palette[i].Grayscale();
      }

   }

}
