using System.Collections.Generic;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.common.geom;
using fmg.uwp.utils;

namespace fmg.uwp.res.img.win2d {

   /// <summary> main logos image </summary>
   public class Logo : fmg.core.img.Logo<CanvasRenderTarget> {

      //private CanvasDevice device;
      private readonly ICanvasResourceCreator _resourceCreator;

      public Logo(bool useGradient, ICanvasResourceCreator resourceCreator) :
         base(useGradient)
      {
         _resourceCreator = resourceCreator;
      }

      protected override CanvasRenderTarget CreateImage() {
         var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
         return new CanvasRenderTarget(_resourceCreator, (float)Size, (float)Size, dpi);
      }

      protected override void DrawImage(CanvasRenderTarget img) {
         using (var ds = img.CreateDrawingSession()) {
            IList<PointDouble> rays, inn, oct;
            GetCoords(out rays, out inn, out oct);

            // paint owner rays
            for (var i = 0; i < 8; i++) {
               CanvasGeometry geom;
               using (var builder = new CanvasPathBuilder(_resourceCreator)) {
                  builder.BeginFigure((float)rays[i].X, (float)rays[i].Y);
                  builder.AddLine((float)oct[i].X, (float)oct[i].Y);
                  builder.AddLine((float)inn[i].X, (float)inn[i].Y);
                  builder.AddLine((float)oct[(i + 5) % 8].X, (float)oct[(i + 5) % 8].Y);
                  builder.EndFigure(CanvasFigureLoop.Closed);
                  geom = CanvasGeometry.CreatePath(builder);
               }
               ds.FillGeometry(geom, Palette[i].Darker().ToWinColor());
            }

            // paint star perimeter
            var penWidth = (float)(2*Zoom);
            for (var i = 0; i < 8; i++) {
               var p1 = rays[(i + 7) % 8];
               var p2 = rays[i];
               ds.DrawLine((float)p1.X, (float)p1.Y, (float)p2.X, (float)p2.Y, Palette[i].ToWinColor(), penWidth);
            }

            var w = Size;
            var h = Size;
            // paint inner gradient triangles
            for (var i = 0; i < 8; i++) {
               CanvasGeometry geom;
               using (var builder = new CanvasPathBuilder(_resourceCreator)) {
                  builder.BeginFigure((float)inn[(i + 0) % 8].X, (float)inn[(i + 0) % 8].Y);
                  builder.AddLine((float)inn[(i + 3) % 8].X, (float)inn[(i + 3) % 8].Y);
                  builder.AddLine((float)(w / 2), (float)(h / 2));
                  builder.EndFigure(CanvasFigureLoop.Closed);
                  geom = CanvasGeometry.CreatePath(builder);
               }
               ds.FillGeometry(geom,
                  ((i & 1) == 0)
                     ? Palette[(i + 6) % 8].Brighter().ToWinColor()
                     : Palette[(i + 6) % 8].Darker().ToWinColor());
            }
         }
      }

   }
}
