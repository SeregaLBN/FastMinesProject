using fmg.core.mosaic.cells;

namespace fmg.core.mosaic.draw
{

   /// <summary>
   /// Interface for drawing
   /// </summary>
   public interface ICellPaint<in TPaintable> where TPaintable : IPaintable
   {
      void Paint(BaseCell cell, TPaintable paint);

      void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      void PaintBorderLines(BaseCell cell, TPaintable paint);

      void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> залить ячейку нужным цветом </summary>
      void PaintComponentBackground(BaseCell cell, TPaintable paint);
   }
}