using System.Linq;
using Windows.Graphics.Display;
using Windows.UI.Core;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.draw.mosaic.win2d;
using fmg.uwp.utils;

namespace fmg.uwp.res.img.win2d {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// CanvasBitmap impl
   /// </summary>
   public class MosaicsGroupImg : core.img.MosaicsGroupImg<CanvasBitmap> {

      static MosaicsGroupImg() {
         if (StaticImgConsts.DeferrInvoker == null)
            StaticImgConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (RotatedImgConst.TimerCreator == null)
            RotatedImgConst.TimerCreator = () => new ui.Timer();
      }

      private readonly ICanvasResourceCreator _rc;

      public MosaicsGroupImg(EMosaicGroup group, ICanvasResourceCreator resourceCreator, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding)
      {
         OnlySyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
         _rc = resourceCreator;
      }

      protected override CanvasBitmap CreateImage() {
         var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
         return new CanvasRenderTarget(_rc, Width, Height, dpi);
      }

      protected override void DrawBody() {
         var img = Image;
         using (var ds = ((CanvasRenderTarget)img).CreateDrawingSession()) {

            ds.Clear(BackgroundColor.ToWinColor());

            var points = GetCoords().ToArray();
            using (var geom = _rc.BuildGeom(points)) {
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
                     var p2 = (i < points.Length-1) ? points[i+1] : points[0];
                     ds.DrawLine(p1.ToVector2(), p2.ToVector2(), clrWin, bw, css);
                  }
               }
            }
         }
      }

   }

}
