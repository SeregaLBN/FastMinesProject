using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;

namespace fmg.uwp.draw.mosaic
{

   /// <summary>
   /// Helper class for drawing info
   /// </summary>
   public abstract class CellPaint<TPaintable> : ICellPaint<TPaintable>
      where TPaintable : IPaintable
   {
      public GraphicContext GContext { get; set; }

      public abstract void Paint(BaseCell cell, TPaintable paint);

      public abstract void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      public abstract void PaintBorderLines(BaseCell cell, TPaintable paint);

      public abstract void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> залить ячейку нужным цветом </summary>
      public abstract void PaintComponentBackground(BaseCell cell, TPaintable paint);
   }
}