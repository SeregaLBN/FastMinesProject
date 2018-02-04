using System;
using Avalonia.Controls;
using Avalonia.Media;
using Avalonia.Media.Imaging;
using Avalonia.Rendering;
using fmg.core.img;
using fmg.core.types;

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
            StaticRotateImgConsts.Init();
         }

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         protected CommonImpl(EMosaicGroup? group)
            : base(group)
         { }

         protected void DrawBody(DrawingContext dc, bool fillBk) {
            //dc.FillRectangle
            dc.DrawRectangle(new Pen(0xFFFFFFFF), new Avalonia.Rect(0,0,20,20));
         }

      }


      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="EMosaicGroup"/> as image.
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : CommonImpl<RenderTargetBitmap> {

         private IVisualBrushRenderer _vbr;

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         public CanvasBmp(EMosaicGroup? group, IControl ctrl)
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
