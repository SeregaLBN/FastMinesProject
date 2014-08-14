using System;
using ua.ksn.fmg.model.mosaics;

namespace ua.ksn.fmg.view.draw {

   public delegate void ColorPropertyChange(object sender, string hintPropertyName, Color oldColor, Color newColor);

   public class ColorText {
      public event ColorPropertyChange OnColorPropertyChange = delegate { };

      private Color[] colorOpen;
      private Color[] colorClose;

      public ColorText() {
         colorOpen = new Color[Enum.GetValues(typeof(EOpen)).Length];
         colorClose = new Color[Enum.GetValues(typeof(EClose)).Length];

         foreach (EOpen eOpen in Enum.GetValues(typeof(EOpen)))
            switch (eOpen) {
            case EOpen._Nil: colorOpen[eOpen.Ordinal()] = Color.Black; break;
            case EOpen._1: colorOpen[eOpen.Ordinal()] = Color.Navy; break;
            case EOpen._2: colorOpen[eOpen.Ordinal()] = Color.Green; break;
            case EOpen._3: colorOpen[eOpen.Ordinal()] = Color.Red; break;
            case EOpen._4: colorOpen[eOpen.Ordinal()] = Color.Maroon; break;
            case EOpen._5: colorOpen[eOpen.Ordinal()] = Color.Blue; break;
            case EOpen._6: colorOpen[eOpen.Ordinal()] = Color.Black; break;
            case EOpen._7: colorOpen[eOpen.Ordinal()] = Color.Olive; break;
            case EOpen._8: colorOpen[eOpen.Ordinal()] = Color.Aqua; break;
            case EOpen._9: colorOpen[eOpen.Ordinal()] = Color.Navy; break;
            case EOpen._10: colorOpen[eOpen.Ordinal()] = Color.Green; break;
            case EOpen._11: colorOpen[eOpen.Ordinal()] = Color.Red; break;
            case EOpen._12: colorOpen[eOpen.Ordinal()] = Color.Maroon; break;
            case EOpen._13: colorOpen[eOpen.Ordinal()] = Color.Navy; break;
            case EOpen._14: colorOpen[eOpen.Ordinal()] = Color.Green; break;
            case EOpen._15: colorOpen[eOpen.Ordinal()] = Color.Red; break;
            case EOpen._16: colorOpen[eOpen.Ordinal()] = Color.Maroon; break;
            case EOpen._17: colorOpen[eOpen.Ordinal()] = Color.Blue; break;
            case EOpen._18: colorOpen[eOpen.Ordinal()] = Color.Black; break;
            case EOpen._19: colorOpen[eOpen.Ordinal()] = Color.Olive; break;
            case EOpen._20: colorOpen[eOpen.Ordinal()] = Color.Aqua; break;
            case EOpen._21: colorOpen[eOpen.Ordinal()] = Color.Navy; break;
            case EOpen._Mine: colorOpen[eOpen.Ordinal()] = Color.Black; break;
            default: throw new Exception("add EOpen value");
            }

         foreach (EClose eClose in Enum.GetValues(typeof(EClose)))
            switch (eClose) {
            case EClose._Unknown: colorClose[eClose.Ordinal()] = Color.Teal; break;
            case EClose._Clear: colorClose[eClose.Ordinal()] = Color.Black; break;
            case EClose._Flag: colorClose[eClose.Ordinal()] = Color.Red; break;
            default: throw new Exception("add EClose value");
            }
      }
      public Color getColorOpen(int i) {
         return colorOpen[i];
      }

      public void setColorOpen(int i, Color colorOpen) {
         Color old = colorOpen;
         this.colorOpen[i] = colorOpen;
         OnColorPropertyChange(this, "ColorText_colorOpen" + i, old, colorOpen);
      }

      public Color getColorClose(int i) {
         return colorClose[i];
      }

      public void setColorClose(int i, Color colorClose) {
         Color old = colorClose;
         this.colorClose[i] = colorClose;
         OnColorPropertyChange(this, "ColorText_colorClose" + i, old, colorClose);
      }
   }
}