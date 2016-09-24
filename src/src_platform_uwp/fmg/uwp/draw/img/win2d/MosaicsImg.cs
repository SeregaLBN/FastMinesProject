using System;
using System.Linq;
using System.ComponentModel;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic.draw;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Representable <see cref="EMosaic"/> as image.
   /// <br/>
   /// Win2D impl
   /// </summary>
   public static class MosaicsImg {

      /// <summary> Representable <see cref="EMosaic"/> as image: common implementation part </summary>
      public abstract class CommonImpl<TImage> : AMosaicsImg<PaintableWin2D, TImage, PaintUwpContext<CanvasBitmap>, CanvasBitmap>
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         static CommonImpl() {
            StaticRotateImgConsts.Init();
         }

         protected readonly ICanvasResourceCreator _rc;
         private const bool RandomCellBkColor = true;
         private Random Rand => new Random(Guid.NewGuid().GetHashCode());

         protected CommonImpl(EMosaic mosaicType, Matrisize sizeField, ICanvasResourceCreator resourceCreator)
            : base(mosaicType, sizeField)
         {
            _rc = resourceCreator;
         }

         private ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> _cellPaint;
         public override ICellPaint<PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>> CellPaint {
            get {
               if (_cellPaint == null)
                  _cellPaint = new CellPaintWin2D();
               return _cellPaint;
            }
         }

         private PaintUwpContext<CanvasBitmap> _paintContext;
         protected PaintUwpContext<CanvasBitmap> PaintContext {
            get {
               if (_paintContext == null)
                  PaintContext = new PaintUwpContext<CanvasBitmap>(true); // call this setter
               return _paintContext;
            }
            set {
               if (SetProperty(ref _paintContext, value)) {
                  Dependency_PContext_CellAttribute();
                  Dependency_PContext_PaddingFull();
                  Dependency_PContext_BorderWidth();
                  Dependency_PContext_BorderColor();
                  Dependency_PContext_BkColor();
                  Invalidate();
               }
            }
         }

         protected override void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
            //LoggerSimple.Put($">  OnSelfPropertyChanged: {GetType().Name}.{Entity}: PropertyName={ev.PropertyName}");
            base.OnSelfPropertyChanged(ev);
            switch (ev.PropertyName) {
            case nameof(this.PaddingFull):
               Dependency_PContext_PaddingFull();
               break;
            case nameof(this.CellAttr):
               Dependency_PContext_CellAttribute();
               break;
            case nameof(this.BorderWidth):
               Dependency_PContext_BorderWidth();
               break;
            case nameof(this.BorderColor):
               Dependency_PContext_BorderColor();
               break;
            case nameof(this.BackgroundColor):
               Dependency_PContext_BkColor();
               break;
            }

            if (RotateMode == ERotateMode.SomeCells) {
               switch (ev.PropertyName) {
               case nameof(this.Size):
                  _imageCache = null;
                  break;
               case nameof(this.RotatedElements):
               case nameof(this.BackgroundColor):
                  _invalidateCache = true;
                  break;
               }
            }
         }

         //protected override void OnSelfPropertyChangedAfter(bool sync, object sender, PropertyChangedEventArgs ev) {
         //   if (sync)
         //      LoggerSimple.Put($"<S OnSelfPropertyChanged: {GetType().Name}.{Entity}: PropertyName={ev.PropertyName}");
         //   else
         //      LoggerSimple.Put($"<A OnSelfPropertyChanged: {GetType().Name}.{Entity}: PropertyName={ev.PropertyName}");
         //}

         #region Dependencys
         void Dependency_PContext_CellAttribute() {
            if (_paintContext == null)
               return;
            if (RandomCellBkColor)
               PaintContext.BkFill.Mode = 1 + Rand.Next(CellAttr.getMaxBackgroundFillModeValue());
         }

         void Dependency_PContext_PaddingFull() {
            if (_paintContext == null)
               return;
            PaintContext.Padding = PaddingFull;
         }

         void Dependency_PContext_BorderWidth() {
            if (_paintContext == null)
               return;
            PaintContext.PenBorder.Width = BorderWidth;
         }

         void Dependency_PContext_BorderColor() {
            if (_paintContext == null)
               return;
            var pb = PaintContext.PenBorder;
            pb.ColorLight = pb.ColorShadow = BorderColor;
         }

         void Dependency_PContext_BkColor() {
            if (_paintContext == null)
               return;
            PaintContext.BackgroundColor = BackgroundColor;
         }
         #endregion

         protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
            switch (RotateMode) {
            case ERotateMode.FullMatrix:
               DrawBodyFullMatrix(ds, fillBk);
               break;
            case ERotateMode.SomeCells:
               DrawBodySomeCells(ds, fillBk);
               break;
            }
         }

         #region PART ERotateMode.FullMatrix

         /// <summary> Return painted mosaic bitmap 
         /// if (!OnlySyncDraw) {
         ///   Сама картинка возвращается сразу.
         ///   Но вот её отрисовка - в фоне.
         ///   Т.к. TImage есть DependencyObject, то его владелец может сам отслеживать отрисовку...
         /// }
         /// </summary>
         protected void DrawBodyFullMatrix(CanvasDrawingSession ds, bool fillBk) {
            var matrix = Matrix;
            var paint = new PaintableWin2D(ds);
            var paintContext = PaintContext;
            var cp = CellPaint;

            if (fillBk)
               ds.Clear(BackgroundColor.ToWinColor());
            foreach (var cell in matrix)
               cp.Paint(cell, paint, paintContext);
         }

         #endregion

         #region PART ERotateMode.SomeCells

         protected bool UseCache = true;

         /// <summary> need redraw the static part of the cache </summary>
         private bool _invalidateCache = true;
         /// <summary>
         /// Cached static part of the picture.
         /// ! Recreated only when changing the original image size (minimizing CreateImage calls).
         /// </summary>
         private CanvasBitmap _imageCache;
         private CanvasBitmap ImageCache {
            get {
               if (_imageCache == null) {
                  var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                  ICanvasResourceCreator rc = Image;
                  _imageCache = new CanvasRenderTarget(rc, Width, Height, dpi);
                  _invalidateCache = true;
               }
               if (_invalidateCache) {
                  _invalidateCache = false;
                  using (var ds = ((CanvasRenderTarget)_imageCache).CreateDrawingSession()) {
                     DrawCache(ds);
                  }
               }
               return _imageCache;
            }
         }

         /// <summary> copy cached image to original </summary>
         protected void CopyFromCache(CanvasDrawingSession ds) {
            if (UseCache) {
               var rc = new Windows.Foundation.Rect(0, 0, Width, Height);
               ds.DrawImage(ImageCache, rc, rc, 1.0f, CanvasImageInterpolation.NearestNeighbor, CanvasComposite.Copy);
            } else {
               ds.DrawImage(ImageCache);
            }
         }

         private void DrawCache(CanvasDrawingSession ds) { DrawStaticPart(ds); }

         protected void DrawStaticPart(CanvasDrawingSession ds) {
            ds.Clear(BackgroundColor.ToWinColor());

            var paint0 = new PaintableWin2D(ds);
            var paintContext = PaintContext;
            var matrix = Matrix;
            var indexes = RotatedElements.Select(cntxt => cntxt.index).ToList();
            for (var i = 0; i < matrix.Count; ++i)
               if (!indexes.Contains(i))
                  CellPaint.Paint(matrix[i], paint0, paintContext);
         }

         protected void DrawRotatedPart(CanvasDrawingSession ds) {
            if (!RotatedElements.Any())
               return;

            var paint = new PaintableWin2D(ds);
            var paintContext = PaintContext;
            var pb = PaintContext.PenBorder;
            // save
            var borderWidth = BorderWidth;
            var borderColor = BorderColor;
            // modify
            pb.Width = 2 * borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);

            var matrix = Matrix;
            foreach (var cntxt in RotatedElements)
               CellPaint.Paint(matrix[cntxt.index], paint, paintContext);

            // restore
            pb.Width = borderWidth; //BorderWidth = borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
         }

         protected void DrawBodySomeCells(CanvasDrawingSession ds, bool fillBk) {
            if (UseCache)
               CopyFromCache(ds);
            else
               DrawStaticPart(ds);
            DrawRotatedPart(ds);
         }

         #endregion

         protected override void Dispose(bool disposing) {
            if (Disposed)
               return;

            base.Dispose(disposing);

            if (disposing)
               PaintContext.Dispose();
         }

      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="EMosaic"/> as image.
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : CommonImpl<CanvasBitmap> {

         public CanvasBmp(EMosaic mosaicType, Matrisize sizeField, ICanvasResourceCreator resourceCreator)
            : base(mosaicType, sizeField, resourceCreator)
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

      /// <summary> Representable <see cref="EMosaic"/> as image.
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : CommonImpl<CanvasImageSource> {

         public CanvasImgSrc(EMosaic mosaicType, Matrisize sizeField, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(mosaicType, sizeField, resourceCreator)
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
