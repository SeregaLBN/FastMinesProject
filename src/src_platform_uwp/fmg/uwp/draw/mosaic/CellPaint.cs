using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;

namespace fmg.uwp.draw.mosaic {

   /// <summary> Class for drawing cell </summary>
   /// <typeparam name="TPaintable">see <see cref="IPaintable"/></typeparam>
   /// <typeparam name="TImage">UWP specific image: <see cref="Windows.UI.Xaml.Media.Imaging.WriteableBitmap"/> or <see cref="Windows.UI.Xaml.Media.ImageSource"/>, etc... </typeparam>
   public abstract class CellPaint<TPaintable, TImage, TPaintContext> : ICellPaint<TPaintable, TImage, TPaintContext>
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintUwpContext<TImage>
   {

      public abstract void Paint(BaseCell cell, TPaintable paint, TPaintContext paintContext);

      protected abstract void PaintBorder(BaseCell cell, TPaintable paint, TPaintContext paintContext);

      /// <summary> draw border lines </summary>
      protected abstract void PaintBorderLines(BaseCell cell, TPaintable paint, TPaintContext paintContext);

      protected abstract void PaintComponent(BaseCell cell, TPaintable paint, TPaintContext paintContext);

      /// <summary> залить ячейку нужным цветом </summary>
      protected abstract void PaintComponentBackground(BaseCell cell, TPaintable paint, TPaintContext paintContext);

      /// <summary> draw image Flag or image Mine </summary>
      protected abstract void PaintImage(BaseCell cell, TPaintable paint, TPaintContext paintContext, TImage img);

   }

}
