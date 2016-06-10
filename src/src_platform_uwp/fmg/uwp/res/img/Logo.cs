using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.uwp.utils;

namespace fmg.uwp.res.img {

   /// <summary> main logos image </summary>
   public class Logo : fmg.core.img.Logo<WriteableBitmap> {

      public Logo(bool useGradient) :
         base(useGradient)
      {
      }

      protected override WriteableBitmap CreateImage() {
         return BitmapFactory.New((int)Size, (int)Size);
      }

      protected override void DrawImage(WriteableBitmap img) {
         IList<PointDouble> rays, inn, oct;
         GetCoords(out rays, out inn, out oct);

         // paint owner rays
         for (var i = 0; i < 8; i++) {
            img.FillQuad(
               (int)rays[i].X, (int)rays[i].Y,
               (int)oct[i].X, (int)oct[i].Y,
               (int)inn[i].X, (int)inn[i].Y,
               (int)oct[(i + 5) % 8].X, (int)oct[(i + 5) % 8].Y,
               Palette[i].Darker().ToWinColor()
            );
         }

         // paint star perimeter
         for (var i = 0; i < 8; i++) {
            var p1 = rays[(i + 7) % 8];
            var p2 = rays[i];
            img.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, Palette[i].ToWinColor());
         }

         double w = img.PixelWidth;
         double h = img.PixelHeight;
         // paint inner gradient triangles
         for (var i = 0; i < 8; i++) {
            img.FillTriangle(
               (int)inn[(i + 0) % 8].X, (int)inn[(i + 0) % 8].Y,
               (int)inn[(i + 3) % 8].X, (int)inn[(i + 3) % 8].Y,
               (int)(w / 2), (int)(h / 2),
               ((i & 1) == 0)
                  ? Palette[(i + 6) % 8].Brighter().ToWinColor()
                  : Palette[(i + 6) % 8].Darker().ToWinColor());
         }
      }

   }
}
