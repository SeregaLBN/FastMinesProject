using Windows.UI.Xaml.Media.Imaging;

namespace ua.ksn.fmg.view.win_rt.res.img {

   /// <summary> flag image </summary>
   public class Flag {
      private readonly WriteableBitmap _bmp;

      public Flag() {

         const int w = 11, h = 11;
         var pixels = new uint[w*h];

         // fill background to transparent color
         for (int i = 0; i < pixels.Length; i++)
            pixels[i] = 0x00000000; // aarrggbb

         // paint image

         // центральная стойка
         for (int y = 5; y < 10; y++)
            pixels[y*w + 6] = 0xFF000000;

         // поддон
         for (int x = 4; x < 9; x++)
            pixels[10*w + x] = 0xFF000000;
         pixels[10*w + 3] =
            pixels[9*w + 5] =
               pixels[9*w + 7] =
                  pixels[10*w + 9] = 0x7F000000;

         // флаг
         int mX = 6;
         for (int y = 1; y < 5; y++, mX--)
            for (int x = 2; x < mX; x++)
               pixels[y*w + x] = 0xFFFF0000;
         mX = 6;
         for (int y = 1; y < 5; y++, mX--)
            for (int x = mX; x < 7; x++)
               pixels[y*w + x] = 0xFF800000;

         var pix2 = new byte[pixels.Length*4];
         for (var i = 0; i < pixels.Length; i++) {
            var p = pixels[i];
            pix2[i*4 + 0] = (byte)((p & 0x000000FF) >> 0);
            pix2[i*4 + 1] = (byte)((p & 0x0000FF00) >> 8);
            pix2[i*4 + 2] = (byte)((p & 0x00FF0000) >> 16);
            pix2[i*4 + 3] = (byte)((p & 0xFF000000) >> 24);
         }
         _bmp = new WriteableBitmap(w, h).FromByteArray(pix2);
      }

      public WriteableBitmap Image {
         get { return _bmp; }
      }
   }
}