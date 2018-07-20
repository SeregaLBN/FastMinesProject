using System.Linq;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.core.types;
using fmg.core.img;
using fmg.uwp.utils;
using fmg.uwp.utils.win2d;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// Win2D impl
   /// </summary>
   public static class MosaicGroupImg {

      /// <summary> Representable <see cref="EMosaicGroup"/> as image: common implementation part </summary>
      public abstract class CommonImpl<TImage> : AMosaicGroupImg<TImage>
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         static CommonImpl() {
            StaticInitializer.Init();
         }

         protected readonly ICanvasResourceCreator _rc;

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         protected CommonImpl(EMosaicGroup? group, ICanvasResourceCreator resourceCreator)
            : base(group)
         {
            _rc = resourceCreator;
         }

         protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
            ICanvasResourceCreator rc = Image;

            if (fillBk)
               ds.Clear(BackgroundColor.ToWinColor());

            var bw = BorderWidth;
            var needDrawPerimeterBorder = (!BorderColor.IsTransparent && (bw > 0));
            var borderColor = BorderColor.ToWinColor();
            using (var css = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Triangle,
               EndCap = CanvasCapStyle.Triangle
            }) {
               var shapes = GetCoords();
               foreach (var data in shapes) {
                  var points = data.Item2.ToArray();
                  using (var geom = rc.BuildLines(points)) {
                     if (!data.Item1.IsTransparent)
                        ds.FillGeometry(geom, data.Item1.ToWinColor());

                     // draw perimeter border
                     if (needDrawPerimeterBorder)
                        ds.DrawGeometry(geom, borderColor, bw, css);
                  }
               }
            }
            using (var css = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Flat,
               EndCap = CanvasCapStyle.Flat
            }) {
               foreach (var li in GetCoordsBurgerMenu())
                  ds.DrawLine(li.from.ToVector2(), li.to.ToVector2(), li.clr.ToWinColor(), (float)li.penWidht, css);
            }

   #if DEBUG
            //// test
            //using (var ctf = new Microsoft.Graphics.Canvas.Text.CanvasTextFormat { FontSize = 25 }) {
            //   ds.DrawText(string.Format($"{RotateAngle:0.##}"), 0f, 0f, Color.Black.ToWinColor(), ctf);
            //}
   #endif
         }

      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="EMosaicGroup"/> as image.
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : CommonImpl<CanvasBitmap> {

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         public CanvasBmp(EMosaicGroup? group, ICanvasResourceCreator resourceCreator)
            : base(group, resourceCreator)
         { }

         protected override CanvasBitmap CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasRenderTarget(_rc, Size.Width, Size.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               DrawBody(ds, true);
            }
         }
      }

      /// <summary> Representable <see cref="EMosaicGroup"/> as image.
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : CommonImpl<CanvasImageSource> {

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         public CanvasImgSrc(EMosaicGroup? group, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(group, resourceCreator)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasImageSource(_rc, Size.Width, Size.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(BackgroundColor.ToWinColor())) {
               DrawBody(ds, false);
            }
         }
      }

   }

}
