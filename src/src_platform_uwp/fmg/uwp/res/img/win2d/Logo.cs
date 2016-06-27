using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic.win2d;
using fmg.core.img;

namespace fmg.uwp.res.img.win2d {

   /// <summary> Main logos image
   /// <br/>
   /// Win2D impl
   /// </summary>
   public abstract class Logo<TImage> : ALogo<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {

      static Logo() {
         if (StaticImgConsts.DeferrInvoker == null)
            StaticImgConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (RotatedImgConst.TimerCreator == null)
            RotatedImgConst.TimerCreator = () => new ui.Timer();
      }

      protected Logo() {
         SyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
      }

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         ICanvasResourceCreator rc = Image;

         if (fillBk) {
            var bkClr = BackgroundColor;
            if (bkClr.A != Color.Transparent.A)
               ds.Clear(bkClr.ToWinColor());
         }

         IList<PointDouble> rays = new List<PointDouble>(), inn = new List<PointDouble>(), oct = new List<PointDouble>();
         GetCoords(rays, inn, oct);

         var center = new PointDouble(Width / 2.0, Height / 2.0);

         // paint owner rays
         for (var i = 0; i < 8; i++) {
            using (var geom = rc.BuildGeom(rays[i], oct[i], inn[i], oct[(i + 5) % 8])) {
               if (UseGradient) {
                  // rectangle gragient
                  using (var br = rc.CreateGradientPaintBrush(oct[(i + 5) % 8], Palette[(i + 0) % 8].ToColor(), oct[i], Palette[(i + 3) % 8].ToColor())) {
                     ds.FillGeometry(geom, br);
                  }

                  // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                  var clr = Palette[(i + 6) % 8].ToColor();
                  clr.A = 0;
                  using (var br = rc.CreateGradientPaintBrush(center, clr, inn[(i + 6) % 8], Palette[(i + 3) % 8].ToColor())) {
                     using (var geom2 = rc.BuildGeom(rays[i], oct[i], inn[i])) {
                        ds.FillGeometry(geom2, br);
                     }
                  }
                  using (var br = rc.CreateGradientPaintBrush(center, clr, inn[(i + 2) % 8], Palette[(i + 0) % 8].ToColor())) {
                     using (var geom2 = rc.BuildGeom(rays[i], oct[(i + 5) % 8], inn[i])) {
                        ds.FillGeometry(geom2, br);
                     }
                  }
               } else {
                  ds.FillGeometry(geom, Palette[i].ToColor().Darker().ToWinColor());
               }
            }
         }

         // paint star perimeter
         var zoomAverage = (ZoomX + ZoomY) / 2;
         var penWidth = (float)(2*zoomAverage);
         for (var i = 0; i < 8; i++) {
            var p1 = rays[(i + 7) % 8];
            var p2 = rays[i];
            ds.DrawLine(p1.ToVector2(), p2.ToVector2(), Palette[i].ToColor().ToWinColor(), penWidth);
         }

         // paint inner gradient triangles
         for (var i = 0; i < 8; i++) {
            using (var geom = rc.BuildGeom(inn[(i + 0) % 8], inn[(i + 3) % 8], center)) {
               if (UseGradient) {
                  using (var br = rc.CreateGradientPaintBrush(inn[i], Palette[(i + 6) % 8].ToColor(), center, ((i & 1) == 0) ? Color.Black : Color.White)) {
                     ds.FillGeometry(geom, br);
                  }
               } else {
                  ds.FillGeometry(geom, ((i & 1) == 0)
                        ? Palette[(i + 6) % 8].ToColor().Brighter().ToWinColor()
                        : Palette[(i + 6) % 8].ToColor().Darker().ToWinColor());
               }
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Main logos image
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : Logo<CanvasBitmap> {

         private readonly ICanvasResourceCreator _rc;

         public CanvasBmp(ICanvasResourceCreator resourceCreator) {
            _rc = resourceCreator;
         }

         protected override CanvasBitmap CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasRenderTarget(_rc, Width, Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               DrawBody(ds, true);
            }
         }
      }

   }

}
