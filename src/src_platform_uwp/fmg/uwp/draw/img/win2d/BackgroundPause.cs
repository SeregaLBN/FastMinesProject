using System;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> картинка для фоновой паузы </summary>
   public abstract class BackgroundPause<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {
      protected readonly ICanvasResourceCreator _rc;
      private TImage _img;
      private int _width = 100;
      private int _height = 100;

      protected BackgroundPause(ICanvasResourceCreator resourceCreator) {
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
         var w = _width / 1000.0f;
         var h = _height / 1000.0f;

         ds.DrawRectangle(0, 0, Width, Height, Windows.UI.Colors.Red, 1); // test

         // fill background (only transparent color)
         if (fillBk) {
            //ds.FillRectangle(5, 5, Width - 10, Height - 10, Color.Transparent.ToWinColor());
         }

         // тело смайла
         ds.FillEllipse(Width / 2f, Height / 2f, Width / 2f - 5 * w, Height / 2f - 5 * h, new Color(0xFFFFE600).ToWinColor());

         // глаза
         var clr = new Color(0xFF000000).ToWinColor();
         ds.FillEllipse((330 + 98 / 2f) * w, (150 + 296 / 2f) * h, 98 / 2f * w, 296 / 2f * h, clr);
         ds.FillEllipse((570 + 98 / 2f) * w, (150 + 296 / 2f) * h, 98 / 2f * w, 296 / 2f * h, clr);

         // smile
         using (var css = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
         }) {
            using (var g = _rc.BuildArc(103 * w, -133 * h, 795 * w, 1003 * h, 207, 126, false)) {
               ds.DrawGeometry(g, clr, Math.Max(1, 14 * (w + h) / 2), css);
            }

            // ямочки на щеках
            using (var g = _rc.BuildArc(90 * w, 580 * h, 180 * w, 180 * h, 90, 45, false)) {
               ds.DrawGeometry(g, clr, Math.Max(1, 14 * (w + h) / 2), css);
            }
            using (var g = _rc.BuildArc(730 * w, 580 * h, 180 * w, 180 * h, 45, 45, false)) {
               ds.DrawGeometry(g, clr, Math.Max(1, 14 * (w + h) / 2), css);
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Main pause image
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : BackgroundPause<CanvasBitmap> {

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

      /// <summary> Main pause image
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : BackgroundPause<CanvasImageSource> {

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
