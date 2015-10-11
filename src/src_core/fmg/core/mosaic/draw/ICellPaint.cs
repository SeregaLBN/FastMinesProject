using fmg.common;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic.draw
{

   /// <summary>
   /// Interface for drawing
   /// </summary>
   public interface ICellPaint<TPaintable> where TPaintable : IPaintable
   {
      void Paint(BaseCell cell, TPaintable paint);

      void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      void PaintBorderLines(BaseCell cell, TPaintable paint);

      void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера </summary>
      Color DefaultBackgroundFillColor { get; }

      /// <summary> залить ячейку нужным цветом </summary>
      void PaintComponentBackground(BaseCell cell, TPaintable paint);
   }
}