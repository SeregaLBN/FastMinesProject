using System.Collections.Generic;
using System.Numerics;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.Brushes;
using fmg.common;
using fmg.common.geom;
using fmg.uwp.utils;

namespace fmg.uwp.res.img.win2d {

   /// <summary> main logos image </summary>
   public class Logo : fmg.core.img.Logo<CanvasBitmap> {

      //private CanvasDevice device;
      private readonly ICanvasResourceCreator _resourceCreator;

      public Logo(bool useGradient, ICanvasResourceCreator resourceCreator) :
         base(useGradient)
      {
         _resourceCreator = resourceCreator;
      }

      protected override CanvasBitmap CreateImage() {
         var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
         return new CanvasRenderTarget(_resourceCreator, (float)Size, (float)Size, dpi);
      }

      protected override void DrawImage(CanvasBitmap img) {
         using (var ds = ((CanvasRenderTarget)img).CreateDrawingSession()) {
            IList<PointDouble> rays, inn, oct;
            GetCoords(out rays, out inn, out oct);

            var center = new PointDouble(Size/2, Size/2);

            // paint owner rays
            for (var i = 0; i < 8; i++) {
               using (var geom = BuildGeom(rays[i], oct[i], inn[i], oct[(i + 5) % 8])) {
                  if (UseGradient) {
                     // rectangle gragient
                     using (var br = CreateGradientPaintBrush(oct[(i + 5) % 8], Palette[(i + 0) % 8], oct[i], Palette[(i + 3) % 8])) {
                        ds.FillGeometry(geom, br);
                     }

                     // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                     var clr = Palette[(i + 6) % 8];
                     clr.A = 0;
                     using (var br = CreateGradientPaintBrush(center, clr, inn[(i + 6) % 8], Palette[(i + 3) % 8])) {
                        using (var geom2 = BuildGeom(rays[i], oct[i], inn[i])) {
                           ds.FillGeometry(geom2, br);
                        }
                     }
                     using (var br = CreateGradientPaintBrush(center, clr, inn[(i + 2) % 8], Palette[(i + 0) % 8])) {
                        using (var geom2 = BuildGeom(rays[i], oct[(i + 5) % 8], inn[i])) {
                           ds.FillGeometry(geom2, br);
                        }
                     }
                  } else {
                     ds.FillGeometry(geom, Palette[i].Darker().ToWinColor());
                  }
               }
            }

            // paint star perimeter
            var penWidth = (float)(2*Zoom);
            for (var i = 0; i < 8; i++) {
               var p1 = rays[(i + 7) % 8];
               var p2 = rays[i];
               ds.DrawLine((float)p1.X, (float)p1.Y, (float)p2.X, (float)p2.Y, Palette[i].ToWinColor(), penWidth);
            }

            // paint inner gradient triangles
            for (var i = 0; i < 8; i++) {
               using (var geom = BuildGeom(inn[(i + 0) % 8], inn[(i + 3) % 8], center)) {
                  if (UseGradient) {
                     using (var br = CreateGradientPaintBrush(inn[i], Palette[(i + 6) % 8], center, ((i & 1) == 0) ? Color.Black : Color.White)) {
                        ds.FillGeometry(geom, br);
                     }
                  } else {
                     ds.FillGeometry(geom, ((i & 1) == 0)
                           ? Palette[(i + 6) % 8].Brighter().ToWinColor()
                           : Palette[(i + 6) % 8].Darker().ToWinColor());
                  }
               }
            }
         }
      }

      private CanvasGeometry BuildGeom(params PointDouble[] p) {
         using (var builder = new CanvasPathBuilder(_resourceCreator)) {
            builder.BeginFigure((float)p[0].X, (float)p[0].Y);
            for (var i = 1; i < p.Length; ++i) {
               builder.AddLine((float)p[i].X, (float)p[i].Y);
            }
            builder.EndFigure(CanvasFigureLoop.Closed);
            return CanvasGeometry.CreatePath(builder);
         }
      }

      private CanvasLinearGradientBrush CreateGradientPaintBrush(PointDouble pt1, Color color1, PointDouble pt2, Color color2) {
         return new CanvasLinearGradientBrush(_resourceCreator, color1.ToWinColor(), color2.ToWinColor()) {
            StartPoint = pt1.ToWinPoint().ToVector2(),
            EndPoint = pt2.ToWinPoint().ToVector2()
         };
      }
   }

}
