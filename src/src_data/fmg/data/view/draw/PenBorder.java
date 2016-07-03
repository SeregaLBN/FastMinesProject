package fmg.data.view.draw;

import fmg.common.Color;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Характеристики кисти у рамки ячейки */
public class PenBorder extends NotifyPropertyChanged {

   private Color colorShadow, colorLight;
   private int width;

   public PenBorder() {
      this(Color.Black, Color.White, 3);
//      this(Color.Green, Color.Red, 1);
   }

   public PenBorder(
         Color colorShadow,
         Color colorLight,
         int iWidth)
   {
      this.colorShadow = colorShadow;
      this.colorLight  = colorLight;
      this.width = iWidth;
   }

   public static final String PROPERTY_COLOR_SHADOW = "ColorShadow";
   public static final String PROPERTY_COLOR_LIGHT  = "ColorLight";
   public static final String PROPERTY_WIDTH        = "Width";

   public Color getColorShadow() {
      return colorShadow;
   }

   public void setColorShadow(Color colorShadow) {
      Color old = this.colorShadow;
      if (!old.equals(colorShadow)) {
         this.colorShadow = colorShadow;
         onPropertyChanged(old, colorShadow, PROPERTY_COLOR_SHADOW);
      }
   }

   public Color getColorLight() {
      return colorLight;
   }

   public void setColorLight(Color colorLight) {
      Color old = this.colorLight;
      if (!old.equals(colorLight)) {
         this.colorLight = colorLight;
         onPropertyChanged(old, colorLight, PROPERTY_COLOR_LIGHT);
      }
   }

   public int getWidth() {
      return width;
   }

   public void setWidth(int iWidth) {
      int old = this.width;
      if (old != iWidth) {
         this.width = iWidth;
         onPropertyChanged(old, iWidth, PROPERTY_WIDTH);
      }
   }

   @Override
   public int hashCode() {
      int result = 31 + colorLight.hashCode();
      result = 31 * result + colorShadow.hashCode();
      return 31 * result + width;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof PenBorder)) return false;
      PenBorder penObj = (PenBorder) obj;
      return (width == penObj.width)
            && colorShadow.equals(penObj.colorShadow)
            && colorLight.equals(penObj.colorLight);
   }

}
