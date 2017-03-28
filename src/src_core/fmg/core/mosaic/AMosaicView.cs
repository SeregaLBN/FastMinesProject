using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.notyfier;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.data.view.draw;

namespace fmg.core.mosaic {

   /// <summary> MVC: view. Base implementation </summary>
   /// <typeparam name="TPaintable">see <see cref="IPaintable"/></typeparam>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   /// <typeparam name="TPaintContext">see <see cref="PaintContext{TImage}"/></typeparam>
   public abstract class AMosaicView</*in*/ TPaintable, TImage, /*in*/ TPaintContext> : Disposable, IMosaicView
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintContext<TImage>, new()
   {

      private Mosaic _mosaic;
      private TPaintContext _paintContext;

      public abstract ICellPaint<TPaintable, TImage, TPaintContext> CellPaint { get; }

      public abstract void Invalidate(IEnumerable<BaseCell> modifiedCells = null);

      public Mosaic Mosaic {
         get { return _mosaic; }
         set {
            if (_mosaic != null)
               _mosaic.PropertyChanged -= OnMosaicPropertyChanged;
            _mosaic = value;
            if (_mosaic != null)
               _mosaic.PropertyChanged += OnMosaicPropertyChanged;
         }
      }

      public virtual TPaintContext PaintContext {
         get {
            if (_paintContext == null)
               PaintContext = new TPaintContext(); // call setter
            return _paintContext;
         }
         private set {
            if (_paintContext != null) {
               _paintContext.PropertyChanged -= OnPaintContextPropertyChanged;
               _paintContext.Dispose();
            }
            _paintContext = value;
            if (_paintContext != null) {
               _paintContext.PropertyChanged += OnPaintContextPropertyChanged; // изменение контекста -> перерисовка мозаики
               ChangeSizeImagesMineFlag();
            }
         }
      }

      protected virtual void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is Mosaic);
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Mosaic));
         switch (ev.PropertyName) {
         case nameof(mosaic.Mosaic.MosaicType):
            ChangeFontSize();
            break;
         case nameof(mosaic.Mosaic.Area):
            ChangeFontSize(PaintContext.PenBorder);
            ChangeSizeImagesMineFlag();
            break;
         case nameof(mosaic.Mosaic.Matrix):
            Invalidate();
            break;
         }
      }

      private void OnPaintContextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is TPaintContext);

         switch (ev.PropertyName) {
         case nameof(PaintContext.PenBorder):
            var evex = ev as PropertyChangedExEventArgs<PenBorder>;
            var penBorder = evex?.NewValue ?? PaintContext.PenBorder;
            ChangeFontSize(penBorder);
            break;
         }
         //this.InvalidateCells();
         //OnSelfPropertyChanged(nameof(PaintContext));
         //OnSelfPropertyChanged(nameof(PaintContext) + "." + ev.PropertyName);
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize() { ChangeFontSize(PaintContext.PenBorder); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize(PenBorder penBorder) {
         PaintContext.FontInfo.Size = (int)Mosaic.CellAttr.GetSq(penBorder.Width);
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      protected abstract void ChangeSizeImagesMineFlag();

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            PaintContext = null; // call setter - unsubscribe & dispose
         }
      }

   }

}
