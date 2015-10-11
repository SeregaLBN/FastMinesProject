using Windows.UI.ViewManagement;
using fmg.common;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;

namespace fmg.winrt.draw.mosaic
{

   /// <summary>
   /// Helper class for drawing info
   /// </summary>
   public abstract class CellPaint<TPaintable> : ICellPaint<TPaintable> where TPaintable : IPaintable
   {
      protected GraphicContext _gContext;

      public CellPaint(GraphicContext gContext)
      {
         this._gContext = gContext;
         DefaultBackgroundFillColor = new UISettings().UIElementColor(UIElementType.ButtonFace).ToFmColor();
      }

      public abstract void Paint(BaseCell cell, TPaintable paint);

      public abstract void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      public abstract void PaintBorderLines(BaseCell cell, TPaintable paint);

      public abstract void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера </summary>
      public Color DefaultBackgroundFillColor { get; protected set; }

      /// <summary> залить ячейку нужным цветом </summary>
      public abstract void PaintComponentBackground(BaseCell cell, TPaintable paint);
   }
}