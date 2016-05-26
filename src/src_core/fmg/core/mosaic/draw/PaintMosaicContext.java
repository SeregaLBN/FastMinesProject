package fmg.core.mosaic.draw;

import fmg.common.Color;

/**
 * Information required for drawing the entire mosaic and cells
 *
 * @param <TImage> plaform specific image
 */
public class PaintMosaicContext<TImage> extends PaintCellContext<TImage> {

   protected static Color _defaultBkColor = Color.Gray;
   /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера */
   public static Color getDefaultBackgroundFillColor() {
      return _defaultBkColor;
   }

   private Color  colorBk;
   private TImage imgBckgrnd;

   public PaintMosaicContext(boolean iconicMode) {
      super(iconicMode);
   }

   public Color getColorBk() {
      if (colorBk == null)
         setColorBk(getDefaultBackgroundFillColor().darker(0.4));
      return colorBk;
   }

   public void setColorBk(Color colorBk) {
      Color old = this.colorBk;
      if (colorBk.equals(old))
         return;
      this.colorBk = colorBk;
      onPropertyChanged(old, colorBk, "ColorBk");
   }

   public TImage getImgBckgrnd() {
      return imgBckgrnd;
   }

   public void setImgBckgrnd(TImage imgBckgrnd) {
      Object old = this.imgBckgrnd;
      if (old == imgBckgrnd) // references compare
         return;
      this.imgBckgrnd = imgBckgrnd;
      onPropertyChanged(old, imgBckgrnd, "ImgBckgrnd");
   }

}
