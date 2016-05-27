using fmg.core.mosaic.cells;

namespace fmg.core.mosaic.draw {

   /// <summary> Interface for drawing cell </summary>
   /// <typeparam name="TPaintable">see <see cref="IPaintable"/></typeparam>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   /// <typeparam name="TPaintContext">see <see cref="PaintContext&lt;TImage&gt;"/></typeparam>
   public interface ICellPaint<in TPaintable, TImage, TPaintContext>
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintContext<TImage>
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
