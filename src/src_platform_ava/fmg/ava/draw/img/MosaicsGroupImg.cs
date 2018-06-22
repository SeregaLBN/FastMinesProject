using System;
using System.Linq;
using Avalonia.Controls;
using Avalonia.Media;
using Avalonia.Media.Imaging;
using Avalonia.Rendering;
using fmg.core.img;
using fmg.core.types;
using fmg.ava.utils;

namespace fmg.ava.draw.img {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// Avalonia impl
   /// </summary>
   public static class MosaicsGroupImg {

      /// <summary> Representable <see cref="EMosaicGroup"/> as image: common implementation part </summary>
      public abstract class CommonImpl<TImage> : AMosaicsGroupImg<TImage>
         where TImage : class
      {
         static CommonImpl() {
            StaticInitilizer.Init();
         }

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         protected CommonImpl(EMosaicGroup? group)
            : base(group)
         { }

         protected void DrawBody(DrawingContext dc, bool fillBk) {
            if (fillBk)
               dc.FillRectangle(new SolidColorBrush(BackgroundColor.ToAvaColor()), new Avalonia.Rect(Size.ToAvaSize()));

            var bw = BorderWidth;
            var needDrawPerimeterBorder = (!BorderColor.IsTransparent && (bw > 0));
            var borderColor = BorderColor.ToAvaColor();

            var shapes = GetCoords();
            foreach (var data in shapes) {
               IBrush brush = null;
               if (!data.Item1.IsTransparent)
                  brush = new SolidColorBrush(data.Item1.ToAvaColor());
               Pen pen = null;
               if (needDrawPerimeterBorder)
                  pen = new Pen(borderColor.ToUint32(), bw);

               var points = data.Item2.ToArray();
               var figure = new PathFigure {
                  StartPoint = points[0].ToAvaPoint(),
                  IsClosed = true,
                  IsFilled = false // TODO ??
               };
               for (int i = 1; i < points.Length; ++i)
                  figure.Segments.Add(new LineSegment {
                     Point = points[i].ToAvaPoint()
                  });

               PathGeometry geom = new PathGeometry();
               geom.Figures.Add(figure);

               dc.DrawGeometry(brush, pen, geom);
            }
         }

      }


      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="EMosaicGroup"/> as image.
      /// <br/>
      /// RenderTargetBitmap impl
      /// </summary>
      public class RenderTargetBmp : CommonImpl<RenderTargetBitmap> {

         private IVisualBrushRenderer _vbr;

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         public RenderTargetBmp(EMosaicGroup? group, IControl ctrl)
            : base(group)
         {
            _vbr = new ImmediateRenderer(ctrl);
         }

         protected override RenderTargetBitmap CreateImage() {
            //var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new RenderTargetBitmap(Size.Width, Size.Height);
         }

         protected override void DrawBody() {
            using (var dc = Image.CreateDrawingContext(_vbr)) {
               using (var dc1 = new DrawingContext(dc)) {
                  DrawBody(dc1, true);
               }
            }
         }

         protected override void Dispose(bool disposing) {
            if (Disposed)
               return;

            base.Dispose(disposing);

            if (disposing) {
               (_vbr as IDisposable)?.Dispose();
               _vbr = null;
            }
         }
      }

   //   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   //   /// <br/>
   //   /// Canvas impl
   //   /// </summary>
   //   public class CanvasImgSrc : CommonImpl<Canvas> {
   //
   //      /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
   //      public CanvasImgSrc(EMosaicGroup? group)
   //         : base(group)
   //      { }
   //
   //      protected override Canvas CreateImage() {
   //         return new Canvas {
   //            Width = Size.Width,
   //            Height = Size.Height
   //         };
   //      }
   //
   //      protected override void DrawBody() {
   //         DrawBody(Image, true);
   //      }
   //   }

   }

}
