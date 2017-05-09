using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
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

      public bool SyncDraw { get; set; } = Windows.ApplicationModel.DesignMode.DesignModeEnabled;

      private MosaicImgView _view;

      protected class MosaicImgView : AMosaicView<PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>> {

         private ICellPaint<PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>> _cellPaint;
         public override ICellPaint<PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>> CellPaint => _cellPaint ?? (_cellPaint = new CellPaintWBmp());

         public WriteableBitmap Paintable { get; set; }
         public bool SyncDraw { get; set; }

         private MosaicsImg _owner;
         public MosaicImgView(MosaicsImg owner) {
            _owner = owner;
         }

         protected override PaintUwpContext<WriteableBitmap> CreatePaintContext() {
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

         protected bool _alreadyPainted = false;
         public override void Repaint(IEnumerable<BaseCell> modifiedCells, RectDouble? clipRegion) {
            var img = Paintable;
            //System.Diagnostics.Debug.Assert(img != null);
            if (img == null)
               return;

            System.Diagnostics.Debug.Assert(!_alreadyPainted);

            _alreadyPainted = true;

            //using (new Tracer())
            {

               if (modifiedCells == null)
                  modifiedCells = Mosaic.Matrix; // check to redraw all mosaic cells

               var pc = PaintContext;

               Action funcFillBk = () => {
                  if (clipRegion.HasValue) {
                     var rc = clipRegion.Value;
                     int lft = (int)rc.Left();
                     int top = (int)rc.Top();
                     int rgt = (int)rc.Right();
                     int btm = (int)rc.Bottom();
                     img.FillPolygon(new[] { lft, top,  rgt, top,  rgt, btm,  lft, btm, lft, top }, pc.BackgroundColor.ToWinColor());
                  } else {
                     img.Clear(pc.BackgroundColor.ToWinColor());
                  }
               };

               var matrix = Mosaic.Matrix;
               var paint = new PaintableWBmp(img);
               var paintContext = PaintContext;
               var cp = CellPaint;
               double padX = pc.Padding.Left, padY = pc.Padding.Top;
               if (SyncDraw) {
                  // sync draw
                  if (pc.IsUseBackgroundColor)
                     funcFillBk();
                  foreach (var cell in modifiedCells) {
                     if (!clipRegion.HasValue || cell.getRcOuter().MoveXY(padX, padY).Intersection(clipRegion.Value))
                        cp.Paint(cell, paint, paintContext);
                  }
               } else {
                  // async draw
                  AsyncRunner.InvokeFromUiLater(() => {
                     if (pc.IsUseBackgroundColor)
                        funcFillBk();
                     var rnd = ThreadLocalRandom.Current;
                     foreach (var cell in modifiedCells) {
                        if (clipRegion.HasValue && !cell.getRcOuter().MoveXY(padX, padY).Intersection(clipRegion.Value))
                           continue;
                        var tmp = cell;
                        AsyncRunner.InvokeFromUiLater(
                           () => cp.Paint(tmp, paint, paintContext),
                           ((rnd.Next() & 1) == 0)
                              ? CoreDispatcherPriority.Low
                              : CoreDispatcherPriority.Normal
                        );
                     }
                  }, CoreDispatcherPriority.Normal);
               }

            }

            _alreadyPainted = false;
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

      protected PaintUwpContext<WriteableBitmap> PaintContext => View.PaintContext;

      protected override void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
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

         base.OnSelfPropertyChanged(ev);

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

      protected override WriteableBitmap CreateImage() {
         return BitmapFactory.New(Size.Width, Size.Height); // new WriteableBitmap(w, h); // 
      }

      protected override void DrawBody() {
         View.Paintable = Image;
         switch (RotateMode) {
         case ERotateMode.FullMatrix:
            DrawBodyFullMatrix();
            break;
         case ERotateMode.SomeCells:
            DrawBodySomeCells();
            break;
         }
         View.Paintable = null;
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
         View.PaintContext.IsUseBackgroundColor = true;
         View.SyncDraw = (SyncDraw || LiveImage());
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
      private WriteableBitmap _imageCache;
      private WriteableBitmap ImageCache {
         get {
            if (_imageCache == null) {
               _imageCache = CreateImage();
               _invalidateCache = true;
            }
            if (_invalidateCache) {
               _invalidateCache = false;
               var save = View.Paintable;
               View.Paintable = _imageCache;
               DrawCache();
               View.Paintable = save; // restore
            }
            return _imageCache;
         }
      }

      /// <summary> copy cached image to original </summary>
      private void CopyFromCache() {
         WriteableBitmap paintableImg = View.Paintable;
         var rc = new Windows.Foundation.Rect(0, 0, Size.Width, Size.Height);
         paintableImg.Blit(rc, ImageCache, rc, WriteableBitmapExtensions.BlendMode.None);
      }

      private void DrawCache() { DrawStaticPart(); }

      private void DrawStaticPart() {
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
         View.SyncDraw = true;
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
         View.SyncDraw = true;
         View.Invalidate(rotatedCells);

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
               DrawStaticPart();
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
            View = null;
      }

   }

}
