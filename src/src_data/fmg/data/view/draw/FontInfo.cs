using fmg.common.notyfier;

namespace fmg.data.view.draw {

   public class FontInfo : NotifyPropertyChanged {

      private string _name = "Arial"; // Times New Roman // Verdana // Courier New // SansSerif;
      private bool _bold = false;
      private int _size = 10;

      public string Name {
         get { return _name; }
         set { SetProperty(ref _name, value); }
      }

      public bool Bold {
         get { return _bold; }
         set { SetProperty(ref _bold, value); }
      }

      public int Size {
         get { return _size; }
         set { SetProperty(ref _size, value); }
      }

      protected bool Equals(FontInfo other) {
         return string.Equals(_name, other._name) && (_bold == other._bold) && (_size == other._size);
      }

      public override bool Equals(object obj) {
         if (ReferenceEquals(null, obj)) return false;
         if (ReferenceEquals(this, obj)) return true;
         return (GetType() == obj.GetType()) && Equals((FontInfo)obj);
      }

      public override int GetHashCode() {
         unchecked { 
            var hashCode = _name?.GetHashCode() ?? 0;
            hashCode = (hashCode*397) ^ _bold.GetHashCode();
            return (hashCode *397) ^ _size;
         }
      }

      public override string ToString() {
         return string.Format("FontInfo={{name={0}, bold={1}, size={2}}}", _name, _bold, _size);
      }
   }

}
