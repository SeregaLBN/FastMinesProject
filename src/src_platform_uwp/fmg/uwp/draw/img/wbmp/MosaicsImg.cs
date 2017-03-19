using System;
using System.Linq;
using System.ComponentModel;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic.draw;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.wbmp;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {

   /// <summary> Representable <see cref="EMosaic"/> as image.
   /// <br/>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsImg : AMosaicsImg<WriteableBitmap> {

      static MosaicsImg() {
         StaticRotateImgConsts.Init();
      }

      private const bool RandomCellBkColor = true;
      private Random Rand => new Random(Guid.NewGuid().GetHashCode());

      public bool SyncDraw { get; set; } = Windows.ApplicationModel.DesignMode.DesignModeEnabled;

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField)
         : base(mosaicType, sizeField)
      { }

      private ICellPaint<PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>> _cellPaint;
      public ICellPaint<PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>> CellPaint {
         get {
            if (_cellPaint == null)
               _cellPaint = new CellPaintWBmp(); // call this setter
            return _cellPaint;
         }
      }

      private PaintUwpContext<WriteableBitmap> _paintContext;
      protected PaintUwpContext<WriteableBitmap> PaintContext {
         get {
            if (_paintContext == null)
               PaintContext = new PaintUwpContext<WriteableBitmap>() { // call this setter
                  IconicMode = true
               };
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

      //protected override void Dispose(bool disposing) {
      //   if (disposing) {
      //      PContext = null; // call setter
      //   }

      //   base.Dispose(disposing);
      //}

      protected override WriteableBitmap CreateImage() {
         return BitmapFactory.New(Size.Width, Size.Height); // new WriteableBitmap(w, h); // 
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
      ///   Т.к. WriteableBitmap есть DependencyObject, то его владелец может сам отслеживать отрисовку...
      /// }
      /// </summary>
      protected void DrawBodyFullMatrix() {
         var img = Image;

         Action funcFillBk = () => img.Clear(BackgroundColor.ToWinColor());

         var matrix = Matrix;
         var paint = new PaintableWBmp(img);
         var paintContext = PaintContext;
         var cp = CellPaint;
         if (SyncDraw || LiveImage()) {
            // sync draw
            funcFillBk();
            foreach (var cell in matrix)
               cp.Paint(cell, paint, paintContext);
         } else {
            // async draw
            AsyncRunner.InvokeFromUiLater(() => {
               funcFillBk();
               foreach (var cell in matrix) {
                  var tmp = cell;
                  AsyncRunner.InvokeFromUiLater(
                     () => cp.Paint(tmp, paint, paintContext),
                     ((Rand.Next() & 1) == 0)
                        ? CoreDispatcherPriority.Low
                        : CoreDispatcherPriority.Normal
                  );
               }
            }, CoreDispatcherPriority.Normal);
         }
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
      private WriteableBitmap _imageCache;
      private WriteableBitmap ImageCache {
         get {
            if (_imageCache == null) {
               _imageCache = CreateImage();
               _invalidateCache = true;
            }
            if (_invalidateCache) {
               _invalidateCache = false;
               DrawCache();
            }
            return _imageCache;
         }
      }

      /// <summary> copy cached image to original </summary>
      private void CopyFromCache() {
         var rc = new Windows.Foundation.Rect(0, 0, Size.Width, Size.Height);
         Image.Blit(rc, ImageCache, rc, WriteableBitmapExtensions.BlendMode.None);
      }

      private void DrawCache() { DrawStaticPart(_imageCache); }

      private void DrawStaticPart(WriteableBitmap targetImage) {
         var w = Size.Width;
         var h = Size.Height;
         targetImage.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor.ToWinColor());

         var paint0 = new PaintableWBmp(targetImage);
         var paintContext = PaintContext;
         var matrix = Matrix;
         var indexes = RotatedElements.Select(cntxt => cntxt.index).ToList();
         for (var i = 0; i < matrix.Count; ++i)
            if (!indexes.Contains(i))
               CellPaint.Paint(matrix[i], paint0, paintContext);
      }

      protected void DrawRotatedPart() {
         if (!RotatedElements.Any())
            return;

         var img = Image;
         var paint = new PaintableWBmp(img);
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

      protected void DrawBodySomeCells() {
         if (SyncDraw || LiveImage()) {
            // sync draw
            if (UseCache)
               CopyFromCache();
            else
               DrawStaticPart(Image);
            DrawRotatedPart();
         } else {
            // async draw
            DrawBodyFullMatrix();
         }
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

}
