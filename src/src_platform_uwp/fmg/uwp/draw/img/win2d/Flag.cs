using System.Numerics;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Flag image </summary>
   public abstract class Flag<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {
      const int Width = 100, Height = 100;
      private const double Zoom = 0.7;
      protected readonly ICanvasResourceCreator _rc;
      private TImage _img;

      protected Flag(ICanvasResourceCreator resourceCreator) {
         _rc = resourceCreator;
      }

      public TImage Image {
         get {
            if (_img == null)
               _img = CreateImage();
            return _img;
         }
      }

      protected abstract TImage CreateImage();

      protected abstract void DrawBody();

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         var p = new[] {
            new Vector2((float)(13.50 * Zoom), (float)(90 * Zoom)),
            new Vector2((float)(17.44 * Zoom), (float)(51 * Zoom)),
            new Vector2((float)(21.00 * Zoom), (float)(16 * Zoom)),
            new Vector2((float)(85.00 * Zoom), (float)(15 * Zoom)),
            new Vector2((float)(81.45 * Zoom), (float)(50 * Zoom))
         };

         using (var cssLine = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Square,
            EndCap = CanvasCapStyle.Flat
         }) {
            ds.DrawLine(p[0], p[1], Color.Black.ToWinColor(), 15, cssLine);

            var clrRed = Color.Red.ToWinColor();
            using (var cssCurve = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Round,
               EndCap = CanvasCapStyle.Flat
            }) {
               using (var builder = new CanvasPathBuilder(_rc)) {
                  builder.BeginFigure(p[2]);
                  builder.AddCubicBezier(
                     new Vector2((float)(95.0 * Zoom), (float)( 0 * Zoom)),
                     new Vector2((float)(19.3 * Zoom), (float)(32 * Zoom)),
                     p[3]);
                  builder.AddCubicBezier(
                     new Vector2((float)(77.80 * Zoom), (float)(32.89 * Zoom)),
                     new Vector2((float)(88.05 * Zoom), (float)(22.73 * Zoom)),
                     p[4]);
                  builder.AddCubicBezier(
                     new Vector2((float)(15.83 * Zoom), (float)(67 * Zoom)),
                     new Vector2((float)(91.45 * Zoom), (float)(35 * Zoom)),
                     p[1]);
                  builder.EndFigure(CanvasFigureLoop.Open);

                  ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clrRed, 12, cssCurve);
               }
            }
            ds.DrawLine(p[1], p[2], clrRed, 15, cssLine);
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Flag image
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : Flag<CanvasBitmap> {

         public CanvasBmp(ICanvasResourceCreator resourceCreator)
            : base(resourceCreator)
         { }

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

      /// <summary> Flag image
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : Flag<CanvasImageSource> {

         public CanvasImgSrc(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(resourceCreator)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasImageSource(_rc, Width, Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(Color.Transparent.ToWinColor())) {
               DrawBody(ds, false);
            }
         }

      }

   }

}
