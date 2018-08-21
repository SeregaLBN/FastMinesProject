using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;
using fmg.uwp.utils.win2d;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.img.win2d {

   /// <summary> Main logo image. Win2D implementation </summary>
   public static class Logo {

      /// <summary> Main logo image. Base view Win2D implementation </summary>
      /// <typeparam name="TImage">Win2D specific image: <see cref="CanvasBitmap"/> or <see cref="CanvasImageSource"/></typeparam>
      public abstract class CommonImpl<TImage> : ImageView<TImage, LogoModel>
         where TImage : DependencyObject, ICanvasResourceCreator
      {

         protected readonly ICanvasResourceCreator _rc;

         protected CommonImpl(ICanvasResourceCreator resourceCreator)
            : base(new LogoModel())
         {
            _rc = resourceCreator;
         }

         static CommonImpl() {
            StaticInitializer.Init();
         }

         protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
            ICanvasResourceCreator rc = Image;
            LogoModel lm = Model;

            if (fillBk)
               ds.Clear(lm.BackgroundColor.ToWinColor());

            IList<PointDouble> rays = lm.Rays;
            IList<PointDouble> inn = lm.Inn;
            IList<PointDouble> oct = lm.Oct;

            var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);

            // paint owner rays
            for (var i = 0; i < 8; i++) {
               using (var geom = rc.BuildLines(rays[i], oct[i], inn[i], oct[(i + 5) % 8])) {
                  if (lm.UseGradient) {
                     // linear gragient
                     using (var br = rc.CreateGradientPaintBrush(oct[(i + 5) % 8], lm.Palette[(i + 0) % 8].ToColor(), oct[i], lm.Palette[(i + 3) % 8].ToColor())) {
                        ds.FillGeometry(geom, br);
                     }

                     // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                     var clr = lm.Palette[(i + 6) % 8].ToColor();
                     clr.A = 0;
                     using (var br = rc.CreateGradientPaintBrush(center, clr, inn[(i + 6) % 8], lm.Palette[(i + 3) % 8].ToColor())) {
                        using (var geom2 = rc.BuildLines(rays[i], oct[i], inn[i])) {
                           ds.FillGeometry(geom2, br);
                        }
                     }
                     using (var br = rc.CreateGradientPaintBrush(center, clr, inn[(i + 2) % 8], lm.Palette[(i + 0) % 8].ToColor())) {
                        using (var geom2 = rc.BuildLines(rays[i], oct[(i + 5) % 8], inn[i])) {
                           ds.FillGeometry(geom2, br);
                        }
                     }
                  } else {
                     ds.FillGeometry(geom, lm.Palette[i].ToColor().Darker().ToWinColor());
                  }
               }
            }

            // paint star perimeter
            var zoomAverage = (lm.ZoomX + lm.ZoomY) / 2;
            var penWidth = Model.BorderWidth*zoomAverage;
            if (penWidth > 0.1) {
               // TODO  g.setStroke(new BasicStroke((float)penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
               for (var i = 0; i < 8; i++) {
                  var p1 = rays[(i + 7) % 8];
                  var p2 = rays[i];
                  ds.DrawLine(p1.ToVector2(), p2.ToVector2(), lm.Palette[i].ToColor().ToWinColor(), (float)penWidth);
               }
            }

            // paint inner gradient triangles
            for (var i = 0; i < 8; i++) {
               using (var geom = rc.BuildLines(inn[(i + 0) % 8], inn[(i + 3) % 8], center)) {
                  if (lm.UseGradient) {
                     using (var br = rc.CreateGradientPaintBrush(inn[i], lm.Palette[(i + 6) % 8].ToColor(), center, ((i & 1) == 0) ? Color.Black : Color.White)) {
                        ds.FillGeometry(geom, br);
                     }
                  } else {
                     ds.FillGeometry(geom, ((i & 1) == 0)
                           ? lm.Palette[(i + 6) % 8].ToColor().Brighter().ToWinColor()
                           : lm.Palette[(i + 6) % 8].ToColor().Darker().ToWinColor());
                  }
               }
            }
         }

         protected override void Disposing() {
            Model.Dispose();
            base.Disposing();
         }

      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Logo image view implementation over <see cref="CanvasBitmap"/> </summary>
      public class CanvasBmp : CommonImpl<CanvasBitmap> {

         public CanvasBmp(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(resourceCreator)
         { }

         protected override CanvasBitmap CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var s = Model.Size;
            return new CanvasRenderTarget(_rc, (float)s.Width, (float)s.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               DrawBody(ds, true);
            }
         }

      }

      /// <summary> Logo image view implementation over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
      public class CanvasImgSrc : CommonImpl<CanvasImageSource> {

         public CanvasImgSrc(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(resourceCreator)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var s = Model.Size;
            return new CanvasImageSource(_rc, (float)s.Width, (float)s.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(Model.BackgroundColor.ToWinColor())) {
               DrawBody(ds, false);
            }
         }

      }

      /** Logo image controller implementation for <see cref="Logo.CanvasBmp"/> */
      public class ControllerBitmap : LogoController<CanvasBitmap, Logo.CanvasBmp> {

         public ControllerBitmap(ICanvasResourceCreator resourceCreator)
            : base(new Logo.CanvasBmp(resourceCreator))
         { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

      /** Logo image controller implementation for <see cref="Logo.CanvasImgSrc"/> */
      public class ControllerImgSrc : LogoController<CanvasImageSource, Logo.CanvasImgSrc> {

         public ControllerImgSrc(ICanvasResourceCreator resourceCreator)
            : base(new Logo.CanvasImgSrc(resourceCreator))
         { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

      ////////////// TEST //////////////
      public static IEnumerable<ControllerBitmap> GetTestData1(ICanvasResourceCreator resourceCreator) {
         return new ControllerBitmap[] { new Logo.ControllerBitmap(resourceCreator)
                                       , new Logo.ControllerBitmap(resourceCreator)
                                       , new Logo.ControllerBitmap(resourceCreator)
                                       , new Logo.ControllerBitmap(resourceCreator)
         };
      }
      public static IEnumerable<ControllerImgSrc> GetTestData2(ICanvasResourceCreator resourceCreator) {
         return new ControllerImgSrc[] { new Logo.ControllerImgSrc(resourceCreator)
                                       , new Logo.ControllerImgSrc(resourceCreator)
                                       , new Logo.ControllerImgSrc(resourceCreator)
                                       , new Logo.ControllerImgSrc(resourceCreator)
         };
      }
      //////////////////////////////////

   }

}
