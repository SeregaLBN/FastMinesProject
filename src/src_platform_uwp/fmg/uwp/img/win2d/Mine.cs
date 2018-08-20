using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Windows.UI.Xaml;
#if false
using LogoBmp = fmg.uwp.img.win2d.Logo.CanvasBmp;
using LogoImg = fmg.uwp.img.win2d.Logo.CanvasImgSrc;
#endif

namespace fmg.uwp.img.win2d {
#if false

   /// <summary> mine image </summary>
   public static class Mine {

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
         where TLogoImage: Logo.CommonImpl<TImage>
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         img.UseGradient = false;
         img.Size = new common.geom.Size(150, 150);
         img.PaddingInt = 10;
         for (var i = 0; i < img.Palette.Length; ++i)
            //img.Palette[i].v = 75;
            img.Palette[i].Grayscale();
      }

   }

#endif
}
