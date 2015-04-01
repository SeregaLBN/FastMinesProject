using Windows.UI.Xaml.Media.Imaging;
using fmg.common;

namespace fmg.winrt.view.res.img {

   /// <summary> mine image </summary>
   public class Mine {
      private readonly WriteableBitmap _bmp;

      public Mine() {
#if true
         var img = new Logo {
            //BkColor = new Color(0x00000000).ToWinColor(),
            Margin = 10,
            ZoomX = 0.7,
            ZoomY = 0.7
         };
         for (var i = 0; i<img.Palette.Length; i++)
            img.Palette[i] = img.Palette[i].Darker(0.5);
         //img.MixLoopColor(mixLoop);
         _bmp = img.Image; // иначе - своя картинка из кода
#else
         const int w = 25;
         const int h = 25;
         var pixels = new uint[w*h];

         // fill background to transparent color
         for (var i = 0; i < pixels.Length; i++)
            pixels[i] = 0x00000000; // aarrggbb

         // paint image

         // центральное тело
         for (var x = 7; x < 18; x++)
            for (var y = 7; y < 18; y++)
               pixels[y*w + x] = 0xFF000000;

         // белая точка
         for (var x = 9; x < 11; x++)
            for (var y = 9; y < 11; y++)
               pixels[y*w + x] = 0xFFFFFFFF;

         // лучи
         for (var x = 11; x < 14; x++)
            for (var y = 3; y < 7; y++)
               pixels[y*w + x] = 0xFF000000;
         for (var x = 18; x < 22; x++)
            for (var y = 11; y < 14; y++)
               pixels[y*w + x] = 0xFF000000;
         for (var x = 11; x < 14; x++)
            for (var y = 18; y < 22; y++)
               pixels[y*w + x] = 0xFF000000;
         for (var x = 3; x < 7; x++)
            for (var y = 11; y < 14; y++)
               pixels[y*w + x] = 0xFF000000;

         // кончики лучей
         pixels[1*w + 11] = 0x56000000;
         pixels[2*w + 11] = 0x56000000;
         pixels[0*w + 12] = 0x56000000;
         pixels[1*w + 12] = 0x8F000000;
         pixels[2*w + 12] = 0x8F000000;
         pixels[1*w + 13] = 0x56000000;
         pixels[2*w + 13] = 0x56000000;

         pixels[22*w + 11] = 0x56000000;
         pixels[23*w + 11] = 0x56000000;
         pixels[22*w + 12] = 0x8F000000;
         pixels[23*w + 12] = 0x8F000000;
         pixels[24*w + 12] = 0x56000000;
         pixels[22*w + 13] = 0x56000000;
         pixels[23*w + 13] = 0x56000000;

         pixels[11*w + 22] = 0x56000000;
         pixels[11*w + 23] = 0x56000000;
         pixels[12*w + 22] = 0x8F000000;
         pixels[12*w + 23] = 0x8F000000;
         pixels[12*w + 24] = 0x56000000;
         pixels[13*w + 22] = 0x56000000;
         pixels[13*w + 23] = 0x56000000;

         pixels[11*w + 1] = 0x56000000;
         pixels[11*w + 2] = 0x56000000;
         pixels[12*w + 0] = 0x56000000;
         pixels[12*w + 1] = 0x8F000000;
         pixels[12*w + 2] = 0x8F000000;
         pixels[13*w + 1] = 0x56000000;
         pixels[13*w + 2] = 0x56000000;

         // точки по диагонали
         for (var x = 5; x < 7; x++)
            for (var y = 5; y < 7; y++)
               pixels[y*w + x] = 0xBB000000;
         for (var x = 18; x < 20; x++)
            for (var y = 5; y < 7; y++)
               pixels[y*w + x] = 0xBB000000;
         for (var x = 5; x < 7; x++)
            for (var y = 18; y < 20; y++)
               pixels[y*w + x] = 0xBB000000;
         for (var x = 18; x < 20; x++)
            for (var y = 18; y < 20; y++)
               pixels[y*w + x] = 0xBB000000;

         // точки 'под' лучами
         for (var x = 9; x < 11; x++)
            for (var y = 5; y < 7; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 14; x < 16; x++)
            for (var y = 5; y < 7; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 18; x < 20; x++)
            for (var y = 9; y < 11; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 18; x < 20; x++)
            for (var y = 14; y < 16; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 14; x < 16; x++)
            for (var y = 18; y < 20; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 9; x < 11; x++)
            for (var y = 18; y < 20; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 5; x < 7; x++)
            for (var y = 14; y < 16; y++)
               pixels[y*w + x] = 0x56000000;
         for (var x = 5; x < 7; x++)
            for (var y = 9; y < 11; y++)
               pixels[y*w + x] = 0x56000000;

         var pix2 = new byte[pixels.Length*4];
         for (var i = 0; i < pixels.Length; i++) {
            var p = pixels[i];
            pix2[i*4 + 0] = (byte) ((p & 0x000000FF) >> 0);
            pix2[i*4 + 1] = (byte) ((p & 0x0000FF00) >> 8);
            pix2[i*4 + 2] = (byte) ((p & 0x00FF0000) >> 16);
            pix2[i*4 + 3] = (byte) ((p & 0xFF000000) >> 24);
         }
         _bmp = new WriteableBitmap(w, h).FromByteArray(pix2);
#endif
      }

      public WriteableBitmap Image {
         get { return _bmp; }
      }
   }
}