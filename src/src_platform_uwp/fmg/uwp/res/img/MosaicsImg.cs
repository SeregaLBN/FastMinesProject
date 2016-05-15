using System;
using System.Linq;
using System.ComponentModel;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.draw;
using fmg.uwp.draw;
using fmg.uwp.draw.mosaic.bmp;
using FastMines.Common;

namespace fmg.uwp.res.img {

   /// <summary> картинка поля конкретной мозаики. Используется для меню, кнопок, etc...
   /// <br>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsImg : core.img.MosaicsImg<PaintableBmp, WriteableBitmap> {

      static MosaicsImg() {
         if (DeferrInvoker == null)
            DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (TimerCreator == null)
            TimerCreator = () => new ui.Timer();
      }

      private const bool RandomCellBkColor = true;
      private Random Rand => GraphicContext.Rand;

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, sizeField, widthAndHeight, padding)
      { }

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeField, sizeImage, padding)
      { }

      private ICellPaint<PaintableBmp> _cellPaint;
      public override ICellPaint<PaintableBmp> CellPaint {
         get {
            if (_cellPaint == null)
               SetCellPaint(new CellPaintBmp()); // call this setter
            return _cellPaint;
         }
      }
      private void SetCellPaint(ICellPaint<PaintableBmp> value) {
         if (SetProperty(ref _cellPaint, value))  {
            Dependency_CellPaint_GContext();
            Invalidate();
         }
      }

      private GraphicContext _gContext;
      protected GraphicContext GContext {
         get {
            if (_gContext == null)
               GContext = new GraphicContext(true); // call this setter
            return _gContext;
         }
         set {
            if (SetProperty(ref _gContext, value)) {
               Dependency_GContext_CellAttribute();
               Dependency_GContext_PaddingFull();
               Dependency_CellPaint_GContext();
               Dependency_GContext_BorderWidth();
               Dependency_GContext_BorderColor();
               Invalidate();
            }
         }
      }

      protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnPropertyChanged(sender, ev);
         switch (ev.PropertyName) {
         case "PaddingFull":
            Dependency_GContext_PaddingFull();
            break;
         case "CellAttr":
            Dependency_GContext_CellAttribute();
            break;
         case "BorderWidth":
            Dependency_GContext_BorderWidth();
            break;
         case "BorderColor":
            Dependency_GContext_BorderColor();
            break;
         }

         if (RotateMode == ERotateMode.SomeCells) {
            switch (ev.PropertyName) {
            case "Size":
               _imageCache = null;
               break;
            case "RotatedElements":
               _invalidateCache = true;
               break;
            }
         }
      }

      #region Dependencys
      void Dependency_GContext_CellAttribute() {
         if (_gContext == null)
            return;
         if (RandomCellBkColor)
            GContext.BkFill.Mode = 1 + Rand.Next(CellAttr.getMaxBackgroundFillModeValue());
      }

      void Dependency_GContext_PaddingFull() {
         if (_gContext == null)
            return;
         GContext.Padding = PaddingFull;
      }

      void Dependency_GContext_BorderWidth() {
         if (_gContext == null)
            return;
         GContext.PenBorder.Width = BorderWidth;
      }

      void Dependency_GContext_BorderColor() {
         if (_gContext == null)
            return;
         var pb = GContext.PenBorder;
         pb.ColorLight = pb.ColorShadow = BorderColor;
      }

      void Dependency_CellPaint_GContext() {
         if (_cellPaint == null)
            return;
         System.Diagnostics.Debug.Assert(CellPaint is CellPaintBmp);
         ((CellPaintBmp)CellPaint).GContext = GContext;
      }
      #endregion

      //protected override void Dispose(bool disposing) {
      //   if (disposing) {
      //      GContext = null; // call setter
      //   }

      //   base.Dispose(disposing);
      //}

      protected override WriteableBitmap CreateImage() {
         var w = Width;
         var h = Height;
         return BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 
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

      ///////////// ================= PART ERotateMode.FullMatrix ======================= /////////////

      /// <summary> Return painted mosaic bitmap 
      /// if (!OnlySyncDraw) {
      ///   Сама картинка возвращается сразу.
      ///   Но вот её отрисовка - в фоне.
      ///   Т.к. WriteableBitmap есть DependencyObject, то его владелец может сам отслеживать отрисовку...
      /// }
      /// </summary>
      protected void DrawBodyFullMatrix() {
         var img = Image;

         Action funcFillBk = () => { img.Clear(BackgroundColor.ToWinColor()); };

         var matrix = Matrix;
         var paint = new PaintableBmp(img);
         var cp = CellPaint;
         if (OnlySyncDraw || LiveImage()) {
            // sync draw
            funcFillBk();
            foreach (var cell in matrix)
               cp.Paint(cell, paint);
         } else {
            // async draw
            AsyncRunner.InvokeFromUiLater(() => {
               funcFillBk();
               foreach (var cell in matrix) {
                  var tmp = cell;
                  AsyncRunner.InvokeFromUiLater(
                     () => cp.Paint(tmp, paint),
                     ((Rand.Next() & 1) == 0)
                        ? CoreDispatcherPriority.Low
                        : CoreDispatcherPriority.Normal
                  );
               }
            }, CoreDispatcherPriority.Normal);
         }
      }

      ///////////// ================= PART ERotateMode.someCells ======================= /////////////

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
         var rc = new Windows.Foundation.Rect(0, 0, Width, Height);
         Image.Blit(rc, ImageCache, rc);
      }

      private void DrawCache() { DrawStaticPart(_imageCache); }

      private void DrawStaticPart(WriteableBitmap toImage) {
         var w = Width;
         var h = Height;
         toImage.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor.ToWinColor());

         var paint0 = new PaintableBmp(toImage);
         var matrix = Matrix;
         var indexes = _rotatedElements.Select(cntxt => cntxt.index).ToList();
         for (var i = 0; i < matrix.Count; ++i)
            if (!indexes.Contains(i))
               CellPaint.Paint(matrix[i], paint0);
      }

      protected void DrawRotatedPart() {
         if (!_rotatedElements.Any())
            return;

         var img = Image;
         var paint = new PaintableBmp(img);
         var pb = GContext.PenBorder;
         // save
         var borderWidth = BorderWidth;
         var borderColor = BorderColor;
         // modify
         pb.Width = 2 * borderWidth;
         pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);

         var matrix = Matrix;
         var indexes = _rotatedElements
            .OrderBy(cntxt => cntxt.area) // Z-ordering
            .Select(cntxt => cntxt.index);
         foreach (var i in indexes)
            CellPaint.Paint(matrix[i], paint);

         // restore
         pb.Width = borderWidth; //BorderWidth = borderWidth;
         pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
      }

      protected void DrawBodySomeCells() {
         if (OnlySyncDraw || LiveImage()) {
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

   }

}
