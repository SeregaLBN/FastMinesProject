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
      this(Color.Black, Color.White, 3)
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

   public override int GetHashCode() {
      unchecked {
         int hashCode = colorShadow.GetHashCode();
         hashCode = (hashCode * 397) ^ colorLight.GetHashCode();
         hashCode = (hashCode * 397) ^ width;
         return hashCode;
      }
   }
   protected bool Equals(PenBorder other) {
      return (width == other.width)
            && colorShadow.Equals(other.colorShadow)
            && colorLight.Equals(other.colorLight);
   }
   public override bool Equals(object other) {
      var penObj = other as PenBorder;
      return (penObj != null) && Equals(penObj);
   }
}
}