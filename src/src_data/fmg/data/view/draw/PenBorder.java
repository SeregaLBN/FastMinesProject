package fmg.data.view.draw;

import fmg.common.Color;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Характеристики кисти у рамки ячейки */
public class PenBorder extends NotifyPropertyChanged {

    private Color colorShadow, colorLight;
    private int width;

    public PenBorder() {
       this(Color.Black, Color.White, 3);
//       this(Color.Green, Color.Red, 1);
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

    public Color getColorShadow() {
      return colorShadow;
   }

   public void setColorShadow(Color colorShadow) {
      Color old = this.colorShadow;
      if (!old.equals(colorShadow)) {
         this.colorShadow = colorShadow;
         onPropertyChanged(old, colorShadow, "PenBorder_colorShadow");
      }
   }

   public Color getColorLight() {
      return colorLight;
   }

   public void setColorLight(Color colorLight) {
      Color old = this.colorLight;
      if (!old.equals(colorLight)) {
         this.colorLight = colorLight;
         onPropertyChanged(old, colorLight, "PenBorder_colorLight");
      }
   }

   public int getWidth() {
      return width;
   }

   public void setWidth(int iWidth) {
      int old = this.width;
      if (old != iWidth) {
         this.width = iWidth;
         onPropertyChanged(old, iWidth, "PenBorder_width");
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
