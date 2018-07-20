using System;
using System.Linq;
using System.Collections.Generic;
using Avalonia.Controls;
using Avalonia.Media;
using Avalonia.Media.Imaging;
using Avalonia.Rendering;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.ava.utils;

namespace fmg.ava.img {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// Avalonia impl
   /// </summary>
   public static class MosaicGroupImg {

      /// <summary> Representable <see cref="EMosaicGroup"/> as image: common implementation part </summary>
      public abstract class View<TImage> : MosaicSkillOrGroupView<TImage, MosaicGroupModel>
         where TImage : class
      {

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         protected View(EMosaicGroup? group)
            : base(new MosaicGroupModel(group))
         { }

         /// <summary> get paint information of drawing basic image model </summary>
         protected override IEnumerable<Tuple<fmg.common.Color, IEnumerable<PointDouble>>> Coords { get => Model.Coords; }

         protected override void Disposing() {
            Model.Dispose();
            base.Disposing();
         }

      }


      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> MosaicsGroup image view implementation over <see cref="RenderTargetBitmap"/> </summary>
      public class RenderTargetBmp : View<RenderTargetBitmap> {

         private IVisualBrushRenderer _vbr;

         /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
         public RenderTargetBmp(EMosaicGroup? group, IControl ctrl)
            : base(group)
         {
            _vbr = new ImmediateRenderer(ctrl);
         }

         protected override RenderTargetBitmap CreateImage() {
            //var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new RenderTargetBitmap((int)Size.Width, (int)Size.Height);
         }

         protected override void DrawBody() {
            using (var dc = Image.CreateDrawingContext(_vbr)) {
               using (var dc1 = new DrawingContext(dc)) {
                  DrawBody(dc1);
               }
            }
         }

         protected override void Disposing() {
            (_vbr as IDisposable)?.Dispose();
            _vbr = null;
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

      /** MosaicsGroup image controller implementation for {@link Canvas} */
      public class ControllerRenderTargetBmp : MosaicGroupController<RenderTargetBitmap, MosaicGroupImg.RenderTargetBmp> {

         public ControllerRenderTargetBmp(EMosaicGroup? group, IControl ctrl)
            : base(!group.HasValue, new MosaicGroupImg.RenderTargetBmp(group, ctrl))
         { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

   }

}
