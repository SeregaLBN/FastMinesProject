using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {

   /// <summary> main logo image. View implementation over UWP <see cref="WriteableBitmap"/> </summary>
   public class Logo : ImageView<WriteableBitmap, LogoModel> {

      private WriteableBitmap _bmp;

      protected Logo() 
         : base(new LogoModel())
      { }

      static Logo() {
         StaticInitializer.Init();
      }

      protected override WriteableBitmap CreateImage() {
         var s = Model.Size;
         _bmp = new WriteableBitmap((int)s.Width, (int)s.Height);
         return _bmp;
      }

      protected override void DrawBody() {
         var img = Image;
         LogoModel lm = Model;

         {
            var bkClr = lm.BackgroundColor;
            if (!bkClr.IsTransparent)
               img.Clear(bkClr.ToWinColor());
         }

         IList<PointDouble> rays = lm.Rays;
         IList<PointDouble> inn  = lm.Inn;
         IList<PointDouble> oct  = lm.Oct;

         // paint owner rays
         for (var i = 0; i < 8; i++) {
            img.FillQuad(
               (int)rays[i].X, (int)rays[i].Y,
               (int)oct[i].X, (int)oct[i].Y,
               (int)inn[i].X, (int)inn[i].Y,
               (int)oct[(i + 5) % 8].X, (int)oct[(i + 5) % 8].Y,
               lm.Palette[i].ToColor().Darker().ToWinColor()
            );
         }

         // paint star perimeter
         for (var i = 0; i < 8; i++) {
            var p1 = rays[(i + 7) % 8];
            var p2 = rays[i];
            // TODO need usage:
            //var zoomAverage = (ZoomX + ZoomY) / 2;
            //var penWidth = Model.BorderWidth*zoomAverage;
            img.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, lm.Palette[i].ToColor().ToWinColor());
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
                  ? lm.Palette[(i + 6) % 8].ToColor().Brighter().ToWinColor()
                  : lm.Palette[(i + 6) % 8].ToColor().Darker().ToWinColor());
         }
      }


      protected override void Disposing() {
         Model.Dispose();
         base.Disposing();
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary>
      /// Logo image controller implementation for <see cref="Logo"/>
      /// </summary>
      public class Controller : LogoController<WriteableBitmap, Logo> {

         public Controller()
            : base(new Logo()) { }

         protected override void Disposing() {
            View.Disposing();
            base.Disposing();
         }

      }
   }

}
