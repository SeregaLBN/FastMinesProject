package fmg.data.view.draw;

import java.beans.PropertyChangeListener;
import java.util.Arrays;

import fmg.common.Color;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;

public class ColorText implements INotifyPropertyChanged {

   private Color[] colorOpen;
   private Color[] colorClose;
   protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

   public ColorText() {
      colorOpen = new Color[EOpen.values().length];
      colorClose = new Color[EClose.values().length];

      for (EOpen eOpen: EOpen.values())
         switch (eOpen) {
         case _Nil : colorOpen[eOpen.ordinal()] = Color.Black ; break;
         case _1   : colorOpen[eOpen.ordinal()] = Color.Navy  ; break;
         case _2   : colorOpen[eOpen.ordinal()] = Color.Green ; break;
         case _3   : colorOpen[eOpen.ordinal()] = Color.Red   ; break;
         case _4   : colorOpen[eOpen.ordinal()] = Color.Maroon; break;
         case _5   : colorOpen[eOpen.ordinal()] = Color.Blue  ; break;
         case _6   : colorOpen[eOpen.ordinal()] = Color.Black ; break;
         case _7   : colorOpen[eOpen.ordinal()] = Color.Olive ; break;
         case _8   : colorOpen[eOpen.ordinal()] = Color.Aqua  ; break;
         case _9   : colorOpen[eOpen.ordinal()] = Color.Navy  ; break;
         case _10  : colorOpen[eOpen.ordinal()] = Color.Green ; break;
         case _11  : colorOpen[eOpen.ordinal()] = Color.Red   ; break;
         case _12  : colorOpen[eOpen.ordinal()] = Color.Maroon; break;
         case _13  : colorOpen[eOpen.ordinal()] = Color.Navy  ; break;
         case _14  : colorOpen[eOpen.ordinal()] = Color.Green ; break;
         case _15  : colorOpen[eOpen.ordinal()] = Color.Red   ; break;
         case _16  : colorOpen[eOpen.ordinal()] = Color.Maroon; break;
         case _17  : colorOpen[eOpen.ordinal()] = Color.Blue  ; break;
         case _18  : colorOpen[eOpen.ordinal()] = Color.Black ; break;
         case _19  : colorOpen[eOpen.ordinal()] = Color.Olive ; break;
         case _20  : colorOpen[eOpen.ordinal()] = Color.Aqua  ; break;
         case _21  : colorOpen[eOpen.ordinal()] = Color.Navy  ; break;
         case _Mine: colorOpen[eOpen.ordinal()] = Color.Black ; break;
         default: throw new RuntimeException("add EOpen value");
         }

      for (EClose eClose: EClose.values())
         switch (eClose) {
         case _Unknown: colorClose[eClose.ordinal()] = Color.Teal ; break;
         case _Clear  : colorClose[eClose.ordinal()] = Color.Black; break;
         case _Flag   : colorClose[eClose.ordinal()] = Color.Red  ; break;
         default: throw new RuntimeException("add EClose value");
         }
   }

   public static final String PROPERTY_COLOR_OPEN    = "ColorOpen";
   public static final String PROPERTY_COLOR_OPEN_N_ = "ColorOpen.#";
   public static final String PROPERTY_COLOR_CLOSE   = "ColorClose";
   public static final String PROPERTY_COLOR_CLOSE_N = "ColorClose.#";

   public Color[] getColorOpen() {
      return colorOpen;
   }
   public Color getColorOpen(int i) {
      return colorOpen[i];
   }
   public void setColorOpen(Color[] colorOpen) {
      Color[] old = this.colorOpen;
      if (!Arrays.equals(old, colorOpen)) {
         this.colorOpen = colorOpen;
         _notifier.onPropertyChanged(old, colorOpen, PROPERTY_COLOR_OPEN);
      }
   }
   public void setColorOpen(int i, Color colorOpen) {
      Color old = this.colorOpen[i];
      if (!old.equals(colorOpen)) {
         this.colorOpen[i] = colorOpen;
         _notifier.onPropertyChanged(old, colorOpen, PROPERTY_COLOR_OPEN_N_ + i);
      }
   }

   public Color[] getColorClose() {
      return colorClose;
   }
   public Color getColorClose(int i) {
      return colorClose[i];
   }
   public void setColorClose(Color[] colorClose) {
      Color[] old = this.colorClose;
      if (!Arrays.equals(old, colorClose)) {
         this.colorClose = colorClose;
         _notifier.onPropertyChanged(old, colorClose, PROPERTY_COLOR_CLOSE);
      }
   }
   public void setColorClose(int i, Color colorClose) {
      Color old = this.colorClose[i];
      if (!old.equals(colorClose)) {
         this.colorClose[i] = colorClose;
         _notifier.onPropertyChanged(old, colorClose, PROPERTY_COLOR_CLOSE_N + i);
      }
   }

   @Override
   public int hashCode() {
      int result = 31 + Arrays.hashCode(colorClose);
      return 31 * result + Arrays.hashCode(colorOpen);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof ColorText)) return false;
      ColorText other = (ColorText) obj;
      return Arrays.equals(colorClose, other.colorClose) &&
             Arrays.equals(colorOpen, other.colorOpen);
   }

   @Override
   public void addListener(PropertyChangeListener listener) {
      _notifier.addListener(listener);
   }
   @Override
   public void removeListener(PropertyChangeListener listener) {
      _notifier.removeListener(listener);
   }

}
