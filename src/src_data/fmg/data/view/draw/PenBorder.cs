using fmg.common;
using fmg.common.notyfier;

namespace fmg.data.view.draw {

   /// <summary> Характеристики кисти у рамки ячейки</summary>
   public class PenBorder : NotifyPropertyChanged {

      private Color _colorShadow, _colorLight;
      private int _width;

      public PenBorder() :
         this(Color.Black, Color.White, 3)
       //this(Color.GREEN, Color.RED, 1)
      { }

      public PenBorder(
            Color colorShadow,
            Color colorLight,
            int iWidth)
      {
         _colorShadow = colorShadow;
         _colorLight = colorLight;
         _width = iWidth;
      }

      public Color ColorShadow {
         get { return _colorShadow; }
         set { SetProperty(ref _colorShadow, value); }
      }

      public Color ColorLight {
         get { return _colorLight; }
         set { SetProperty(ref _colorLight, value); }
      }

      public int Width {
         get { return _width; }
         set { SetProperty(ref _width, value); }
      }

      public override int GetHashCode() {
         unchecked {
            var hashCode = _colorShadow.GetHashCode();
            hashCode = (hashCode * 397) ^ _colorLight.GetHashCode();
            hashCode = (hashCode * 397) ^ _width;
            return hashCode;
         }
      }

      protected bool Equals(PenBorder other) {
         return (_width == other._width)
               && _colorShadow.Equals(other._colorShadow)
               && _colorLight.Equals(other._colorLight);
      }

      public override bool Equals(object other) {
         var penObj = other as PenBorder;
         return (penObj != null) && Equals(penObj);
      }
   }
}