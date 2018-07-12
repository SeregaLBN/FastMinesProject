using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.uwp.utils;
using fmg.core.img;

namespace fmg.uwp.draw.img.wbmp {

   /// <summary> Flag image over UWP WriteableBitmap </summary>
   public class Flag : ImageView<WriteableBitmap, FlagModel> {

      WriteableBitmap _bmp;

      public Flag()
         : base(new FlagModel())
      { }

      static Flag() {
         StaticInitializer.Init();
      }

      protected override WriteableBitmap CreateImage() {
         var s = Model.Size;
         _bmp = BitmapFactory.New((int)s.Width, (int)s.Height);
         return _bmp;
      }

      protected override void DrawBody() {
         var bmp = _bmp;
#if true
         double w = Size.Width  / 100.0;
         double h = Size.Height / 100.0;

         // perimeter figure points
         var p = new[] {
            new PointDouble(13.5 *w, 90*h),
            new PointDouble(17.44*w, 51*h),
            new PointDouble(21   *w, 16*h),
            new PointDouble(85   *w, 15*h),
            new PointDouble(81.45*w, 50*h)
         };

       //bmp.DrawLineAa((int) p[0].x, (int) p[0].y, (int) p[2].x, (int) p[2].y, Color.BLACK.ToWinColor());
         bmp.DrawLineAa((int) p[0].X, (int) p[0].Y, (int) p[1].X, (int) p[1].Y, Color.Black.ToWinColor());

         const float tension = 0.5f;
         var clrCurve = Color.Red.ToWinColor();
         bmp.DrawCurve(new[] {
               p[3],
               new PointDouble(95  *w,  0*h),
               new PointDouble(19.3*w, 32*h),
               p[2]
            }.PointsAsXyxyxySequence(false).ToArray(), tension, clrCurve);

#if false
         bmp.DrawCurve(new[] {
               p[1],
               new PointDouble(55.5*Zoom, 15*Zoom),
               new PointDouble(45*Zoom, 62.5*Zoom),
               p[3]
            }.PointsAsXyxyxySequence(false), tension, clrCurve);
#else
         bmp.DrawCurve(new[] {
               p[1],
               new PointDouble(91.45*w, 35*h),
               new PointDouble(15.83*w, 67*h),
               p[4]
            }.PointsAsXyxyxySequence(false).ToArray(), tension, clrCurve);
         bmp.DrawCurve(new[] {
               p[3],
               new PointDouble(77.8 *w, 32.89*h),
               new PointDouble(88.05*w, 22.73*h),
               p[4]
            }.PointsAsXyxyxySequence(false).ToArray(), tension, clrCurve);
#endif
         bmp.DrawLineAa((int) p[1].X, (int) p[1].Y, (int) p[2].X, (int) p[2].Y, clrCurve);

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

      protected override void Disposing() {
         Model.Dispose();
         base.Disposing();
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /** Flag image controller implementation for {@link Icon} */
      public class Controller : ImageController<WriteableBitmap, Flag, FlagModel> {

         public Controller()
            : base(new Flag())
         { }

         protected override void Disposing() {
            View.Disposing();
            base.Disposing();
         }

      }

   }

}