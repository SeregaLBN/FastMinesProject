using System.Linq;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.core.img;
using fmg.data.controller.types;
using fmg.uwp.utils;
using fmg.uwp.utils.win2d;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Representable <see cref="ESkillLevel"/> as image.
   /// <br/>
   /// Win2D impl
   /// </summary>
   public abstract class MosaicsSkillImg<TImage> : AMosaicsSkillImg<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {
      static MosaicsSkillImg() {
         StaticRotateImgConsts.Init();
      }

      protected readonly ICanvasResourceCreator _rc;

      /// <param name="skill">may be null. if Null - representable image of typeof(ESkillLevel)</param>
      protected MosaicsSkillImg(ESkillLevel? group, ICanvasResourceCreator resourceCreator)
         : base(group)
      {
         _rc = resourceCreator;
      }

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         ICanvasResourceCreator rc = Image;

         if (fillBk)
            ds.Clear(BackgroundColor.ToWinColor());

         var stars = GetCoords();
         foreach (var data in stars) {
            var points = data.Item2.ToArray();
            using (var geom = rc.BuildLines(points)) {
               ds.FillGeometry(geom, data.Item1.ToWinColor());
            }

            // draw perimeter border
            var clr = BorderColor;
            if (!clr.IsTransparent) {
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

         /// <param name="skill">may be null. if Null - representable image of typeof(ESkillLevel)</param>
         public CanvasBmp(ESkillLevel? group, ICanvasResourceCreator resourceCreator)
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

      /// <summary> Representable <see cref="ESkillLevel"/> as image.
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : MosaicsSkillImg<CanvasImageSource> {

         /// <param name="skill">may be null. if Null - representable image of typeof(ESkillLevel)</param>
         public CanvasImgSrc(ESkillLevel? group, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
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
