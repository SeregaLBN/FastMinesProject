using fmg.core.mosaic.cells;

namespace fmg.core.mosaic.draw {

   /// <summary> Interface for drawing cell </summary>
   /// <typeparam name="TPaintable">see <see cref="IPaintable"/></typeparam>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   /// <typeparam name="TPaintContext">see <see cref="PaintContext&lt;TImage&gt;"/></typeparam>
   public interface ICellPaint<in TPaintable, TImage, in TPaintContext>
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintContext<TImage>
   {

      void Paint(BaseCell cell, TPaintable paint, TPaintContext paintContext);

   }

}
