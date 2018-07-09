using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {
#if false
   /// <summary> main logos image </summary>
   public class Logo : ALogo<WriteableBitmap> {

      static Logo() {
         StaticInitializer.Init();
      }

      protected override WriteableBitmap CreateImage() {
         return new WriteableBitmap(Size.Width, Size.Height);
      }

      protected override void DrawBody() {
         var img = Image;

         {
            var bkClr = BackgroundColor;
            if (!bkClr.IsTransparent)
               img.Clear(bkClr.ToWinColor());
         }

         IList<PointDouble> rays = new List<PointDouble>(), inn = new List<PointDouble>(), oct = new List<PointDouble>();
         GetCoords(rays, inn, oct);

         // paint owner rays
         for (var i = 0; i < 8; i++) {
            img.FillQuad(
               (int)rays[i].X, (int)rays[i].Y,
               (int)oct[i].X, (int)oct[i].Y,
               (int)inn[i].X, (int)inn[i].Y,
               (int)oct[(i + 5) % 8].X, (int)oct[(i + 5) % 8].Y,
               Palette[i].ToColor().Darker().ToWinColor()
            );
         }

         // paint star perimeter
         for (var i = 0; i < 8; i++) {
            var p1 = rays[(i + 7) % 8];
            var p2 = rays[i];
            // TODO need usage:
            //var zoomAverage = (ZoomX + ZoomY) / 2;
            //var penWidth = Model.BorderWidth*zoomAverage;
            img.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, Palette[i].ToColor().ToWinColor());
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
                  ? Palette[(i + 6) % 8].ToColor().Brighter().ToWinColor()
                  : Palette[(i + 6) % 8].ToColor().Darker().ToWinColor());
         }
      }

   }
#endif
}
