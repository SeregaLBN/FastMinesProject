using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;

namespace fmg.uwp.draw.mosaic {

   /// <summary>
   /// Class for drawing cell
   /// </summary>
   public abstract class CellPaint<TPaintable, TImage, TPaintContext> : ICellPaint<TPaintable, TImage, TPaintContext>
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintCellContext<TImage>
   {
      public TPaintContext PaintContext { get; set; }

      public abstract void Paint(BaseCell cell, TPaintable paint);

      public abstract void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      public abstract void PaintBorderLines(BaseCell cell, TPaintable paint);

      public abstract void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> залить ячейку нужным цветом </summary>
      public abstract void PaintComponentBackground(BaseCell cell, TPaintable paint);

   }

}
