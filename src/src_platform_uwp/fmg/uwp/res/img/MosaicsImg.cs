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
using fmg.Common;
using fmg.uwp.draw.mosaic;
using fmg.uwp.utils;

namespace fmg.uwp.res.img {

   /// <summary> картинка поля конкретной мозаики. Используется для меню, кнопок, etc...
   /// <br>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsImg : core.img.MosaicsImg<PaintableBmp, WriteableBitmap, PaintContext<WriteableBitmap>> {

      static MosaicsImg() {
         if (DeferrInvoker == null)
            DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (TimerCreator == null)
            TimerCreator = () => new ui.Timer();
      }

      private const bool RandomCellBkColor = true;
      private Random Rand => new Random(Guid.NewGuid().GetHashCode());

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, sizeField, widthAndHeight, padding)
      {
         OnlySyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
      }

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeField, sizeImage, padding)
      {
         OnlySyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
      }

      private ICellPaint<PaintableBmp, WriteableBitmap, PaintContext<WriteableBitmap>> _cellPaint;
      public override ICellPaint<PaintableBmp, WriteableBitmap, PaintContext<WriteableBitmap>> CellPaint {
         get {
            if (_cellPaint == null)
               SetCellPaint(new CellPaintBmp<PaintContext<WriteableBitmap>>()); // call this setter
            return _cellPaint;
         }
      }
      private void SetCellPaint(ICellPaint<PaintableBmp, WriteableBitmap, PaintContext<WriteableBitmap>> value) {
         if (SetProperty(ref _cellPaint, value))  {
            dependency_PContext_CellPaint();
            Invalidate();
         }
      }

      private PaintContext<WriteableBitmap> _paintContext;
      protected PaintContext<WriteableBitmap> PaintContext {
         get {
            if (_paintContext == null)
               PaintContext = new PaintContext<WriteableBitmap>(true); // call this setter
            return _paintContext;
         }
         set {
            if (SetProperty(ref _paintContext, value)) {
               Dependency_PContext_CellAttribute();
               Dependency_PContext_PaddingFull();
               dependency_PContext_CellPaint();
               Dependency_PContext_BorderWidth();
               Dependency_PContext_BorderColor();
               Dependency_PContext_BkColor();
               Invalidate();
            }
         }
      }

      protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnPropertyChanged(sender, ev);
         switch (ev.PropertyName) {
         case "PaddingFull":
            Dependency_PContext_PaddingFull();
            break;
         case "CellAttr":
            Dependency_PContext_CellAttribute();
            break;
         case "BorderWidth":
            Dependency_PContext_BorderWidth();
            break;
         case "BorderColor":
            Dependency_PContext_BorderColor();
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
         PaintContext.ColorBk = BackgroundColor;
      }

      void dependency_PContext_CellPaint() {
         if (_cellPaint == null)
            return;
         CellPaint.PaintContext = PaintContext;
      }
      #endregion

      //protected override void Dispose(bool disposing) {
      //   if (disposing) {
      //      PContext = null; // call setter
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
         var pb = PaintContext.PenBorder;
         // save
         var borderWidth = BorderWidth;
         var borderColor = BorderColor;
         // modify
         pb.Width = 2 * borderWidth;
         pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);

         var matrix = Matrix;
         _rotatedElements.ForEach(cntxt => CellPaint.Paint(matrix[cntxt.index], paint));

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
