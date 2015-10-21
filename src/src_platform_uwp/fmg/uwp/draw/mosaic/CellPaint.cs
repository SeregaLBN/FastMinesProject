using System;
using Windows.UI.ViewManagement;
using fmg.common;
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
      private static readonly UISettings UiSettings = new UISettings();

      protected CellPaint()
      {
         try {
            //DefaultBackgroundFillColor = new UISettings().UIElementColor(UIElementType.ButtonFace).ToFmColor();
            Color clr;
            try {
               clr = UiSettings.UIElementColor(UIElementType.ButtonFace).ToFmColor(); // desktop
            } catch (ArgumentException) {
               try {
                  clr = UiSettings.UIElementColor(1000 + UIElementType.ButtonFace).ToFmColor(); // mobile
               } catch (Exception) {
                  clr = MosaicGraphicContext.COLOR_BTNFACE; // hz
               }
            }
            DefaultBackgroundFillColor = clr;
         } catch (Exception ex) {
            System.Diagnostics.Debug.Fail(ex.Message);
         }
         //DefaultBackgroundFillColor = new UISettings().UIElementColor(UIElementType.ButtonFace).ToFmColor();
      }

      public GraphicContext GContext { get; set; }

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