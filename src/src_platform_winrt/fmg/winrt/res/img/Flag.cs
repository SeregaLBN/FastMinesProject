using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;

namespace fmg.winrt.res.img {

   /// <summary> flag image </summary>
   public class Flag {
      public const uint DefaultWidht = 100;
      public const uint DefaultHeight = 100;

      private WriteableBitmap _bmp;
      public double Zoom { get; set; }

      public Flag() {
         Zoom = 0.7;
      }

      public WriteableBitmap Image {
         get {
            if (_bmp != null)
               return _bmp;

#if true
            var size = new Size((int) (DefaultWidht*Zoom), (int) (DefaultHeight*Zoom));
            var bmp = BitmapFactory.New(size.width, size.height);

            var p = new[] {
               new Windows.Foundation.Point(13.5*Zoom, 90*Zoom),
               new Windows.Foundation.Point(17.44*Zoom, 51*Zoom),
               new Windows.Foundation.Point(21*Zoom, 16*Zoom),
               new Windows.Foundation.Point(85*Zoom, 15*Zoom),
               new Windows.Foundation.Point(81.45*Zoom, 50*Zoom)
            };

            //bmp.DrawLineAa((int) p[0].X, (int) p[0].Y, (int) p[2].X, (int) p[2].Y, Color.BLACK.ToWinColor());
            bmp.DrawLineAa((int) p[0].X, (int) p[0].Y, (int) p[1].X, (int) p[1].Y, Color.Black.ToWinColor());

            const float tension = 0.5f;
            var clrCurve = Color.Red.ToWinColor();
            bmp.DrawCurve(
               RegionExt.PointsAsXyxyxySequence(new[] {
                  p[3],
                  new Windows.Foundation.Point(95*Zoom, 0*Zoom),
                  new Windows.Foundation.Point(19.3*Zoom, 32*Zoom),
                  p[2]
               }, false), tension, clrCurve);

   #if false
            bmp.DrawCurve(
               RegionExt.PointsAsXyxyxySequence(new[] {
                  p[1],
                  new Windows.Foundation.Point(55.5*Zoom, 15*Zoom),
                  new Windows.Foundation.Point(45*Zoom, 62.5*Zoom),
                  p[3]
               }, false), tension, clrCurve);
   #else
            bmp.DrawCurve(
               RegionExt.PointsAsXyxyxySequence(new[] {
                  p[1],
                  new Windows.Foundation.Point(91.45*Zoom, 35*Zoom),
                  new Windows.Foundation.Point(15.83*Zoom, 67*Zoom),
                  p[4]
               }, false), tension, clrCurve);
            bmp.DrawCurve(
               RegionExt.PointsAsXyxyxySequence(new[] {
                  p[3],
                  new Windows.Foundation.Point(77.8*Zoom, 32.89*Zoom),
                  new Windows.Foundation.Point(88.05*Zoom, 22.73*Zoom),
                  p[4]
               }, false), tension, clrCurve);
   #endif
            bmp.DrawLineAa((int) p[1].X, (int) p[1].Y, (int) p[2].X, (int) p[2].Y, clrCurve);
            _bmp = bmp;
            return _bmp;
#else
            const int w = 11, h = 11;
            var pixels = new uint[w*h];

            // fill background to transparent color
            for (var i = 0; i < pixels.Length; i++)
               pixels[i] = 0x00000000; // aarrggbb

            // paint image

            // центральная стойка
            for (var y = 5; y < 10; y++)
               pixels[y*w + 6] = 0xFF000000;

            // поддон
            for (var x = 4; x < 9; x++)
               pixels[10*w + x] = 0xFF000000;
            pixels[10*w + 3] =
               pixels[9*w + 5] =
                  pixels[9*w + 7] =
                     pixels[10*w + 9] = 0x7F000000;

            // флаг
            var mX = 6;
            for (var y = 1; y < 5; y++, mX--)
               for (var x = 2; x < mX; x++)
                  pixels[y*w + x] = 0xFFFF0000;
            mX = 6;
            for (var y = 1; y < 5; y++, mX--)
               for (var x = mX; x < 7; x++)
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
#endif
         }
      }
   }
}