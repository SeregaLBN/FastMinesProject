using System.Linq;
using Windows.UI.Core;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.core.img;
using fmg.data.controller.types;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.res.img.win2d {

   /// <summary> Representable <see cref="ESkillLevel"/> as image.
   /// <br/>
   /// Win2D impl
   /// </summary>
   public abstract class MosaicsSkillImg<TImage> : AMosaicsSkillImg<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {

      static MosaicsSkillImg() {
         if (StaticImgConsts.DeferrInvoker == null)
            StaticImgConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (RotatedImgConst.TimerCreator == null)
            RotatedImgConst.TimerCreator = () => new ui.Timer();
      }

      protected MosaicsSkillImg(ESkillLevel group)
         : base(group)
      {
         SyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
      }

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         ICanvasResourceCreator rc = Image;

         if (fillBk)
            ds.Clear(BackgroundColor.ToWinColor());

         var stars = GetCoords();
         foreach (var coords in stars) {
            var points = coords.ToArray();
            using (var geom = rc.BuildGeom(points)) {
               ds.FillGeometry(geom, ForegroundColorAttenuate.ToWinColor());
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
                     var p2 = (i < points.Length - 1) ? points[i + 1] : points[0];
                     ds.DrawLine(p1.ToVector2(), p2.ToVector2(), clrWin, bw, css);
                  }
               }
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="ESkillLevel"/> as image.
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : MosaicsSkillImg<CanvasBitmap> {

         private readonly ICanvasResourceCreator _rc;

         public CanvasBmp(ESkillLevel group, ICanvasResourceCreator resourceCreator)
            : base(group)
         {
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

      /// <summary> Representable <see cref="ESkillLevel"/> as image.
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : MosaicsSkillImg<CanvasImageSource> {

         public CanvasImgSrc(ESkillLevel group)
            : base(group)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var device = CanvasDevice.GetSharedDevice();
            return new CanvasImageSource(device, Width, Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(BackgroundColor.ToWinColor())) {
               DrawBody(ds, false);
            }
         }
      }

   }

}
