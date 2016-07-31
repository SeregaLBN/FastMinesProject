using System.Linq;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.core.types;
using fmg.core.img;
using fmg.uwp.utils;
using fmg.uwp.utils.win2d;
using fmg.uwp.draw.mosaic.win2d;
using fmg.common.geom;
using System.Collections.Generic;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// Win2D impl
   /// </summary>
   public abstract class MosaicsGroupImg<TImage> : AMosaicsGroupImg<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {
      static MosaicsGroupImg() {
         StaticRotateImgConsts.Init();
      }

      protected readonly ICanvasResourceCreator _rc;

      protected MosaicsGroupImg(EMosaicGroup group, ICanvasResourceCreator resourceCreator)
         : base(group)
      {
         _rc = resourceCreator;
      }

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         if (fillBk)
            ds.Clear(BackgroundColor.ToWinColor());

         if (MosaicGroup != EMosaicGroup.eOthers) {
            DrawBody(ds, GetCoords(), 0);
         } else {
            var coords = GetDoubleCoords();
            DrawBody(ds, coords.Item1, 0);
            DrawBody(ds, coords.Item2, 180);
         }
#if DEBUG
         //// test
         //using (var ctf = new Microsoft.Graphics.Canvas.Text.CanvasTextFormat { FontSize = 25 }) {
         //   ds.DrawText(string.Format($"{RotateAngle:0.##}"), 0f, 0f, Color.Black.ToWinColor(), ctf);
         //}
#endif
      }

      private void DrawBody(CanvasDrawingSession ds, IEnumerable<PointDouble> coords, double addonToRotateColor) {
         ICanvasResourceCreator rc = Image;

         var points = coords.ToArray();
         using (var geom = rc.BuildLines(points)) {
            if (addonToRotateColor.HasMinDiff(0))
               ds.FillGeometry(geom, ForegroundColor.ToWinColor());
            else {
               var hsv = new HSV(ForegroundColor);
               hsv.h += addonToRotateColor;
               ds.FillGeometry(geom, hsv.ToColor().ToWinColor());
            }
         }

         // draw perimeter border
         var clr = BorderColor;
         if (clr.A != Color.Transparent.A) {
            var clrWin = clr.ToWinColor();
            var bw = BorderWidth;

            using (var css = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Triangle,
               EndCap = CanvasCapStyle.Triangle
            }) {
               for (var i = 0; i < points.Length; ++i) {
                  var p1 = points[i];
                  var p2 = (i < points.Length-1) ? points[i+1] : points[0];
                  ds.DrawLine(p1.ToVector2(), p2.ToVector2(), clrWin, bw, css);
               }
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="EMosaicGroup"/> as image.
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : MosaicsGroupImg<CanvasBitmap> {

         public CanvasBmp(EMosaicGroup group, ICanvasResourceCreator resourceCreator)
            : base(group, resourceCreator)
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

      /// <summary> Representable <see cref="EMosaicGroup"/> as image.
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : MosaicsGroupImg<CanvasImageSource> {

         public CanvasImgSrc(EMosaicGroup group, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(group, resourceCreator)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasImageSource(_rc, Width, Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(BackgroundColor.ToWinColor())) {
               DrawBody(ds, false);
            }
         }
      }

   }

}
