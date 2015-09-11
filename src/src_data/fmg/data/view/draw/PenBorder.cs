using fmg.common;

namespace fmg.data.view.draw
{

   public delegate void OnPenBorderColorChanged(object sender, PenBorder.PropertyChangeColorEventArgs e);
   public delegate void OnPenBorderWidthChanged(object sender, PenBorder.PropertyChangeWidthEventArgs e);


   /// <summary> Характеристики кисти у рамки ячейки</summary>
   public class PenBorder
   {

      #region EventArgs
      public class PropertyChangeColorEventArgs : System.EventArgs
      {
         bool _light;
         Color _oldVal;

         public PropertyChangeColorEventArgs(bool light, Color oldVal)
         {
            _light = light;
            _oldVal = oldVal;
         }
      }

      public class PropertyChangeWidthEventArgs : System.EventArgs
      {
         int _oldVal;

         public PropertyChangeWidthEventArgs(int oldVal)
         {
            _oldVal = oldVal;
         }
      }
      #endregion

      public event OnPenBorderColorChanged OnColorChanged = delegate { };
      public event OnPenBorderWidthChanged OnWidthChanged = delegate { };

      private Color colorShadow, colorLight;
      private int width;

      public PenBorder() :
         this(Color.Black, Color.White, 3)
      //this(Color.GREEN, Color.RED, 1)
      { }

      public PenBorder(
            Color colorShadow,
            Color colorLight,
            int iWidth)
      {
         this.colorShadow = colorShadow;
         this.colorLight = colorLight;
         this.width = iWidth;
      }

      public Color ColorShadow
      {
         get { return colorShadow; }
         set
         {
            Color old = colorShadow;
            colorShadow = value;
            OnColorChanged(this, new PropertyChangeColorEventArgs(false, old));
         }
      }

      public Color ColorLight
      {
         get { return colorLight; }
         set
         {
            Color old = colorLight;
            colorLight = value;
            OnColorChanged(this, new PropertyChangeColorEventArgs(true, old));
         }
      }

      public int Width
      {
         get { return width; }
         set
         {
            int old = width;
            width = value;
            OnWidthChanged(this, new PropertyChangeWidthEventArgs(old));
         }
      }

      public override int GetHashCode()
      {
         unchecked
         {
            int hashCode = colorShadow.GetHashCode();
            hashCode = (hashCode * 397) ^ colorLight.GetHashCode();
            hashCode = (hashCode * 397) ^ width;
            return hashCode;
         }
      }

      protected bool Equals(PenBorder other)
      {
         return (width == other.width)
               && colorShadow.Equals(other.colorShadow)
               && colorLight.Equals(other.colorLight);
      }

      public override bool Equals(object other)
      {
         var penObj = other as PenBorder;
         return (penObj != null) && Equals(penObj);
      }
   }
}