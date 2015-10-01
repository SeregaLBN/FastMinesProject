using System.Collections.Generic;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.core.mosaic.cells;

namespace fmg.winrt.draw.mosaic
{

   public interface IPaintable { }

   /// <summary>
   /// Helper class for drawing info
   /// </summary>
   public abstract class ICellPaint<TPaintable> where TPaintable : IPaintable
   {
      protected GraphicContext gContext;
      private IDictionary<Color, Brush> _brushCacheMap;

      public ICellPaint(GraphicContext gContext)
      {
         this.gContext = gContext;
         DefaultBackgroundFillColor = new UISettings().UIElementColor(UIElementType.ButtonFace).ToFmColor();
         _brushCacheMap = new Dictionary<Color, Brush>();
      }

      /// <summary> find cached solid brush. if not exist - create it </summary>
      private Brush FindBrush(Color clr) {
         if (!_brushCacheMap.ContainsKey(clr))
            _brushCacheMap.Add(clr, new SolidColorBrush(clr.ToWinColor()));
         return _brushCacheMap[clr];
      }
      private Brush BrushBorderShadow {
         get { return FindBrush(gContext.PenBorder.ColorShadow); }
      }
      private Brush BrushBorderLight {
         get { return FindBrush(gContext.PenBorder.ColorLight); }
      }

      public abstract void Paint(BaseCell cell, TPaintable paint);

      public abstract void PaintBorder(BaseCell cell, TPaintable paint);

      /// <summary> draw border lines </summary>
      public abstract void PaintBorderLines(BaseCell cell, TPaintable paint);

      protected abstract void PaintComponent(BaseCell cell, TPaintable paint);

      /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера </summary>
      public Color DefaultBackgroundFillColor { get; set; }

      /// <summary> залить ячейку нужным цветом </summary>
      protected abstract void PaintComponentBackground(BaseCell cell, TPaintable paint);
   }
}