using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.res.img {

   /// <summary> main logos image </summary>
   public class Logo : fmg.core.img.Logo<WriteableBitmap> {

      static Logo() {
         if (StaticImgConsts.DeferrInvoker == null)
            StaticImgConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (RotatedImgConst.TimerCreator == null)
            RotatedImgConst.TimerCreator = () => new ui.Timer();
      }

      public Logo() {
         SyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
      }

      protected override WriteableBitmap CreateImage() {
         return new WriteableBitmap(Width, Height);
      }

      protected override void DrawBody() {
         var img = Image;

         {
            var bkClr = BackgroundColor;
            if (bkClr.A != Color.Transparent.A)
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
}
