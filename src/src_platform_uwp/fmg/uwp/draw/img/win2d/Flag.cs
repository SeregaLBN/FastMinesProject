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
      protected readonly ICanvasResourceCreator _rc;
      private TImage _img;
      private int _width = 100;
      private int _height = 100;

      protected Flag(ICanvasResourceCreator resourceCreator) {
         _rc = resourceCreator;
      }

      public TImage Image {
         get {
            if (_img == null) {
               _img = CreateImage();
               DrawBody();
            }
            return _img;
         }
      }

      public int Width {
         get { return _width; }
         set { _width = value; _img = null; }
      }
      public int Height {
         get { return _height; }
         set { _height = value; _img = null; }
      }

      protected abstract TImage CreateImage();

      protected abstract void DrawBody();

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         var w = _width / 100.0f;
         var h = _height / 100.0f;

         ds.DrawRectangle(0, 0, Width, Height, Windows.UI.Colors.Red, 1); // test

         var p = new[] {
            new Vector2(13.50f * w, 90 * h),
            new Vector2(17.44f * w, 51 * h),
            new Vector2(21.00f * w, 16 * h),
            new Vector2(85.00f * w, 15 * h),
            new Vector2(81.45f * w, 50 * h)
         };

         using (var cssLine = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Flat,
            EndCap = CanvasCapStyle.Flat
         }) {
            ds.DrawLine(p[0], p[1], Color.Black.ToWinColor(), System.Math.Max(1, 12*(w+h)/2), cssLine);

            var clrRed = Color.Red.ToWinColor();
            using (var cssCurve = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Triangle,
               EndCap = CanvasCapStyle.Triangle
            }) {
               using (var builder = new CanvasPathBuilder(_rc)) {
                  builder.BeginFigure(p[2]);
                  builder.AddCubicBezier(
                     new Vector2(95.0f * w,  0 * h),
                     new Vector2(19.3f * w, 32 * h),
                     p[3]);
                  builder.AddCubicBezier(
                     new Vector2(77.80f * w, 32.89f * h),
                     new Vector2(88.05f * w, 22.73f * h),
                     p[4]);
                  builder.AddCubicBezier(
                     new Vector2(15.83f * w, 67 * h),
                     new Vector2(91.45f * w, 35 * h),
                     p[1]);
                  builder.AddLine(p[2]);
                  builder.EndFigure(CanvasFigureLoop.Closed);

                  ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clrRed, System.Math.Max(1, 12 * (w+h)/2), cssCurve);
               }
            }
            //ds.DrawLine(p[1], p[2], clrRed, 15, cssLine);
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
