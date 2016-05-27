using fmg.core.mosaic.cells;

namespace fmg.core.mosaic.draw {

   /// <summary>
   /// Interface for drawing cell
   /// </summary>
   public interface ICellPaint<in TPaintable, TImage, TPaintContext>
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintCellContext<TImage>
   {
      TPaintContext PaintContext { get; set; }

      void Paint(BaseCell cell, TPaintable paint);

      void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      void PaintBorderLines(BaseCell cell, TPaintable paint);

      void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> залить ячейку нужным цветом </summary>
      void PaintComponentBackground(BaseCell cell, TPaintable paint);

   }

}
