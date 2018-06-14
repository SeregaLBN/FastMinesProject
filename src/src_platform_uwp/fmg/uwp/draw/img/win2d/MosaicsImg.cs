using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;
using fmg.uwp.mosaic.win2d;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image.
   /// <br/>
   /// Win2D impl
   /// </summary>
   public static class MosaicsImg {

      private const bool RandomCellBkColor = true;

      /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image: common Win2D implementation part </summary>
      public abstract class AMosaicImgWin2D<TImage> : AMosaicsImg<TImage>
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         protected readonly ICanvasResourceCreator _rc;
         private MosaicImgView _view;

         static AMosaicImgWin2D() {
            StaticRotateImgConsts.Init();
         }

         protected AMosaicImgWin2D(ICanvasResourceCreator resourceCreator) {
            _rc = resourceCreator;
         }

         protected class MosaicImgView : AMosaicViewWin2D {

            private AMosaicImgWin2D<TImage> _owner;
            public MosaicImgView(AMosaicImgWin2D<TImage> owner) {
               _owner = owner;
            }

            protected override PaintWin2DContext CreatePaintContext() {
               var cntxt = base.CreatePaintContext();
               cntxt.IconicMode = true;
               if (RandomCellBkColor)
                  cntxt.BkFill.Mode = 1 + ThreadLocalRandom.Current.Next(Mosaic.CellAttr.getMaxBackgroundFillModeValue());
               return cntxt;
            }

            public override SizeDouble Size {
               get {
                  var size = _owner.Size;
                  return new SizeDouble(size.Width, size.Height);
               }
            }

            public override void Invalidate(IEnumerable<BaseCell> modifiedCells = null) {
               Repaint(modifiedCells, null /* new RectDouble(_owner.Size.Width, _owner.Size.Height) */);
            }

            protected override void ChangeSizeImagesMineFlag() {
               // none...
            }

            protected override void Dispose(bool disposing) {
               if (Disposed)
                  return;

               base.Dispose(disposing);

               if (disposing)
                  _owner = null;
            }

         }

         protected MosaicImgView View {
            get {
               if (_view == null)
                  View = new MosaicImgView(this); // call this setter
               return _view;
            }
            set {
               if (_view != null)
                  _view.Dispose();
               _view = value;
               if (_view != null)
                  _view.Mosaic = Mosaic;
            }
         }

         protected PaintUwpContext<CanvasBitmap> PaintContext => View.PaintContext;

         protected override void OnPropertyChanged(PropertyChangedEventArgs ev) {
            //LoggerSimple.Put($">  OnPropertyChanged: {GetType().Name}.{Entity}: PropertyName={ev.PropertyName}");
            switch (ev.PropertyName) {
            case nameof(this.PaddingFull):
               PaintContext.Padding = PaddingFull;
               break;
            case nameof(this.BorderWidth):
               PaintContext.PenBorder.Width = BorderWidth;
               break;
            case nameof(this.BorderColor):
               var pb = PaintContext.PenBorder;
               pb.ColorLight = pb.ColorShadow = BorderColor;
               break;
            case nameof(this.BackgroundColor):
               PaintContext.BackgroundColor = BackgroundColor;
               break;
            }

            base.OnPropertyChanged(ev);

            if (RotateMode == ERotateMode.SomeCells) {
               switch (ev.PropertyName) {
               case nameof(this.Size):
                  ImageCache = null;
                  break;
               case nameof(this.RotatedElements):
               case nameof(this.BackgroundColor):
               case nameof(this.MosaicType):
               case nameof(this.SizeField):
                  _invalidateCache = true;
                  break;
               }
            }
         }

         protected override void DrawBody() {
            switch (RotateMode) {
            case ERotateMode.FullMatrix:
               DrawBodyFullMatrix();
               break;
            case ERotateMode.SomeCells:
               DrawBodySomeCells();
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
         protected void DrawBodyFullMatrix() {
            View.Invalidate(Matrix);
         }

         #endregion

         #region PART ERotateMode.SomeCells

         private const bool UseCache = true;

         /// <summary> need redraw the static part of the cache </summary>
         private bool _invalidateCache = true;
         /// <summary>
         /// Cached static part of the picture.
         /// ! Recreated only when changing the original image size (minimizing CreateImage calls).
         /// </summary>
         private CanvasBitmap _imageCache;
         private CanvasBitmap ImageCache {
            set {
               if (!ReferenceEquals(_imageCache, value)) {
                  _imageCache?.Dispose();
                  _imageCache = value;
                  _invalidateCache = true;
               }
            }
            get {
               if (_imageCache == null) {
                  var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                  ICanvasResourceCreator rc = Image;
                  ImageCache = new CanvasRenderTarget(rc, Size.Width, Size.Height, dpi);
               }
               if (_invalidateCache) {
                  _invalidateCache = false;
                  using (var ds = ((CanvasRenderTarget)_imageCache).CreateDrawingSession()) {
                     var save = View.Paintable;
                     View.Paintable = ds;
                     DrawCache();
                     View.Paintable = save; // restore
                  }
               }
               return _imageCache;
            }
         }

         /// <summary> copy cached image to original </summary>
         protected void CopyFromCache() {
            CanvasDrawingSession ds = View.Paintable;
            var rc = new Windows.Foundation.Rect(0, 0, Size.Width, Size.Height);
            ds.DrawImage(ImageCache, rc, rc, 1.0f, CanvasImageInterpolation.NearestNeighbor, CanvasComposite.Copy);
         }

         private void DrawCache() { DrawStaticPart(); }

         protected void DrawStaticPart() {
            View.PaintContext.IsUseBackgroundColor = true;

            IList<BaseCell> notRotated;
            if (!RotatedElements.Any()) {
               notRotated = Matrix;
            } else {
               var matrix = Matrix;
               var indexes = RotatedElements.Select(cntxt => cntxt.index).ToList();
               notRotated = new List<BaseCell>(matrix.Count - indexes.Count);
               int i = 0;
               foreach (BaseCell cell in matrix) {
                  if (!indexes.Contains(i))
                     notRotated.Add(cell);
                  ++i;
               }
            }
            View.Invalidate(notRotated);
         }

         protected void DrawRotatedPart() {
            if (!RotatedElements.Any())
               return;

            var pb = PaintContext.PenBorder;
            // save
            var borderWidth = BorderWidth;
            var borderColor = BorderColor;
            // modify
            pb.Width = 2 * borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);

            View.PaintContext.IsUseBackgroundColor = false;
            var matrix = Matrix;
            var rotatedCells = new List<BaseCell>(RotatedElements.Count);
            foreach (RotatedCellContext cntxt in RotatedElements)
               rotatedCells.Add(matrix[cntxt.index]);
            View.Invalidate(rotatedCells);

            // restore
            pb.Width = borderWidth; //BorderWidth = borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
         }

         protected void DrawBodySomeCells() {
            if (UseCache)
               CopyFromCache();
            else
               DrawStaticPart();
            DrawRotatedPart();
         }

         #endregion

         protected override void Dispose(bool disposing) {
            if (Disposed)
               return;

            if (disposing) {
               View = null;
               ImageCache = null;
            }

            base.Dispose(disposing);
         }

      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image.
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : AMosaicImgWin2D<CanvasBitmap> {

         public CanvasBmp(ICanvasResourceCreator resourceCreator)
            : base(resourceCreator)
         { }

         protected override CanvasBitmap CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasRenderTarget(_rc, Size.Width, Size.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               View.PaintContext.IsUseBackgroundColor = true;
               View.Paintable = ds;
               base.DrawBody();
               View.Paintable = null;
            }
         }

      }

      /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image.
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : AMosaicImgWin2D<CanvasImageSource> {

         public CanvasImgSrc(ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(resourceCreator)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasImageSource(_rc, Size.Width, Size.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(BackgroundColor.ToWinColor())) {
               View.PaintContext.IsUseBackgroundColor = false;
               View.Paintable = ds;
               base.DrawBody();
               View.Paintable = null;
            }
         }

      }

   }

}
