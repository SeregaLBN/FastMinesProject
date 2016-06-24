using Windows.UI.Xaml.Media.Imaging;

namespace fmg.uwp.res.img {

   /// <summary> mine image </summary>
   public class Mine {
      private readonly WriteableBitmap _bmp;

      public Mine() {
         var img = new Logo {
            UseGradient = false,
            SizeInt = 150,
            PaddingInt = 10
         };
         for (var i = 0; i<img.Palette.Length; i++)
            img.Palette[i].v = 75;
         _bmp = img.Image;
      }

      public WriteableBitmap Image {
         get { return _bmp; }
      }
   }
}