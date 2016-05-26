package fmg.swing.draw.mosaic;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;

public class MosaicGraphicContext extends GraphicContext {
   private Color     colorBk;
   private ImageIcon imgBckgrnd;

   public MosaicGraphicContext(JComponent owner) {
      super(owner, false);
   }

   public Color getColorBk() {
      if (colorBk == null) {
         fmg.common.Color clr = GraphicContext.getDefaultBackgroundFillColor();
         setColorBk(Cast.toColor(clr.darker(0.4)));
      }
      return colorBk;
   }

   public void setColorBk(Color colorBk) {
      Color old = this.colorBk;
      if (colorBk.equals(old))
         return;
      this.colorBk = colorBk;
      onPropertyChanged(old, colorBk, "ColorBk");
   }

   public ImageIcon getImgBckgrnd() {
      return imgBckgrnd;
   }

   public void setImgBckgrnd(ImageIcon imgBckgrnd) {
      Object old = this.imgBckgrnd;
      if (old == imgBckgrnd) // references compare 
         return;
      this.imgBckgrnd = imgBckgrnd;
      onPropertyChanged(old, imgBckgrnd, "ImgBckgrnd");
   }

}
