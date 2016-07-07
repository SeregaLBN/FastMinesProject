using System.Numerics;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.uwp.utils;

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
         ds.FillEllipse(5*w, 5*h, Width - 10*w, Height - 10*h, new Color(0xFFFFE600).ToWinColor());

         // глаза
         var clr = new Color(0xFF000000).ToWinColor();
         ds.FillEllipse(330*w, 150*h, 98*w, 296*h, clr);
         ds.FillEllipse(570*w, 150*h, 98*w, 296*h, clr);

         // smile
         using (var css = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
         }) {
            using (var builder = new CanvasPathBuilder(_rc)) {
               builder.BeginFigure(103*w, -133*h);
               float radiusX = 795*w / 2;
               float radiusY = 1003*h / 2;
               builder.AddArc(new Vector2(103*w + radiusX, -133*h + radiusY), radiusX, radiusY, 207, 126);
               builder.EndFigure(CanvasFigureLoop.Open);

               ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clr, System.Math.Max(1, 14*(w+h)/2), css);
            }

            // ямочки на щеках
            using (var builder = new CanvasPathBuilder(_rc)) {
               builder.BeginFigure(90*w, 580*h);
               float radiusX = 180*w / 2;
               float radiusY = 180*h / 2;
               builder.AddArc(new Vector2(90*w + radiusX, 580*h + radiusY), radiusX, radiusY, 85, 57);
               builder.EndFigure(CanvasFigureLoop.Open);

               ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clr, System.Math.Max(1, 14*(w+h)/2), css);
            }
            using (var builder = new CanvasPathBuilder(_rc)) {
               builder.BeginFigure(730*w, 580*h);
               float radiusX = 180*w / 2;
               float radiusY = 180*h / 2;
               builder.AddArc(new Vector2(730*w + radiusX, 580*h + radiusY), radiusX, radiusY, 38, 57);
               builder.EndFigure(CanvasFigureLoop.Open);

               ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clr, System.Math.Max(1, 14*(w+h)/2), css);
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
