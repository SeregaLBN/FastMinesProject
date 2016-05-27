using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;

namespace fmg.uwp.draw.mosaic {

   /// <summary> Class for drawing cell </summary>
   /// <typeparam name="TPaintable">see <see cref="IPaintable"/></typeparam>
   /// <typeparam name="TImage">UWP specific image: <see cref="Windows.UI.Xaml.Media.Imaging.WriteableBitmap"/> or <see cref="Windows.UI.Xaml.Media.ImageSource"/>, etc... </typeparam>
   public abstract class CellPaint<TPaintable, TImage> : ICellPaint<TPaintable, TImage, PaintUwpContext<TImage>>
      where TPaintable : IPaintable
      where TImage : class
   {

      public abstract void Paint(BaseCell cell, TPaintable paint, PaintUwpContext<TImage> paintContext);

      public abstract void PaintBorder(BaseCell cell, TPaintable paint, PaintUwpContext<TImage> paintContext);

      /// <summary> draw border lines </summary>
      public abstract void PaintBorderLines(BaseCell cell, TPaintable paint, PaintUwpContext<TImage> paintContext);

      public abstract void PaintComponent(BaseCell cell, TPaintable paint, PaintUwpContext<TImage> paintContext);

      /// <summary> залить ячейку нужным цветом </summary>
      public abstract void PaintComponentBackground(BaseCell cell, TPaintable paint, PaintUwpContext<TImage> paintContext);

   }

}
