namespace ua.ksn.fmg.view.draw {

   public delegate void OnPenBorderColorChanged(PenBorder source, bool light, Color oldVal);
   public delegate void OnPenBorderWidthChanged(PenBorder source, int oldVal);


/// <summary> Характеристики кисти у рамки ячейки</summary>
public class PenBorder {
   public event OnPenBorderColorChanged OnColorChanged = delegate { };
   public event OnPenBorderWidthChanged OnWidthChanged = delegate { };

   private Color colorShadow, colorLight;
   private int width;

   public PenBorder() :
      this(Color.BLACK, Color.WHITE, 3)
      //this(Color.GREEN, Color.RED, 1)
   {}
   public PenBorder(
         Color colorShadow,
         Color colorLight,
         int iWidth)
   {
      this.colorShadow = colorShadow;
      this.colorLight  = colorLight;
      this.width = iWidth;
   }

   public Color ColorShadow {
      get { return colorShadow; }
      set {
         Color old = colorShadow;
         colorShadow = value;
         OnColorChanged(this, false, old);
      }
   }
   public Color ColorLight {
      get { return colorLight; }
      set {
         Color old = colorLight;
         colorLight = value;
         OnColorChanged(this, true, old);
      }
   }
   public int Width {
      get { return width; }
      set {
         int old = width;
         width = value;
         OnWidthChanged(this, old);
      }
   }

   public override bool Equals(object obj) {
      PenBorder penObj = obj as PenBorder;
      if (penObj == null)
         return false;

      return (width == penObj.width)
            && colorShadow.Equals(penObj.colorShadow)
            && colorLight.Equals(penObj.colorLight);
   }
}
}