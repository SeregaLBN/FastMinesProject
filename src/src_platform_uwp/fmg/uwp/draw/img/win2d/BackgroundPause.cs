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
      const int Width = 1000, Height = 1000;
      protected readonly ICanvasResourceCreator _rc;
      private TImage _img;

      protected BackgroundPause(ICanvasResourceCreator resourceCreator) {
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
         // fill background (only transparent color)
         if (fillBk) {
            //ds.FillRectangle(5, 5, Width - 10, Height - 10, Color.Transparent.ToWinColor());
         }

         // тело смайла
         ds.FillEllipse(5, 5, Width - 10, Height - 10, new Color(0x00FFE600).ToWinColor());

         // глаза
         var clr = new Color(0x00000000).ToWinColor();
         ds.FillEllipse(330, 150, 98, 296, clr);
         ds.FillEllipse(570, 150, 98, 296, clr);

         // smile
         using (var css = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
         }) {
            using (var builder = new CanvasPathBuilder(_rc)) {
               //g.drawArc(103, -133, 795, 1003, 207, 126);
               builder.BeginFigure(103, -133);
               float radiusX = 795 / 2.0f;
               float radiusY = 1003 / 2.0f;
               builder.AddArc(new Vector2(103 + radiusX, -133 + radiusY), radiusX, radiusY, 207, 126);
               builder.EndFigure(CanvasFigureLoop.Open);

               ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clr, 14, css);
            }

            // ямочки на щеках
            using (var builder = new CanvasPathBuilder(_rc)) {
               //g.drawArc(90, 580, 180, 180, 85, 57);
               builder.BeginFigure(90, 580);
               float radiusX = 180 / 2.0f;
               float radiusY = 180 / 2.0f;
               builder.AddArc(new Vector2(90 + radiusX, 580 + radiusY), radiusX, radiusY, 85, 57);
               builder.EndFigure(CanvasFigureLoop.Open);

               ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clr, 14, css);
            }
            using (var builder = new CanvasPathBuilder(_rc)) {
               //g.drawArc(730, 580, 180, 180, 38, 57);
               builder.BeginFigure(730, 580);
               float radiusX = 180 / 2.0f;
               float radiusY = 180 / 2.0f;
               builder.AddArc(new Vector2(730 + radiusX, 580 + radiusY), radiusX, radiusY, 38, 57);
               builder.EndFigure(CanvasFigureLoop.Open);

               ds.DrawGeometry(CanvasGeometry.CreatePath(builder), clr, 14, css);
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
