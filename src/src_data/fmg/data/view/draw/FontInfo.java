package fmg.data.view.draw;

import fmg.common.notyfier.NotifyPropertyChanged;

/** minimal font descripton */
public class FontInfo extends NotifyPropertyChanged {

   /** font name */
   private String _name = "SansSerif"; // Arial
   /** font is bold? */
   private boolean _bold = false;
   /** font size */
   private int _size = 10;

   public FontInfo() { }
   public FontInfo(String fontName, boolean isBold, int fontSize) {
      _name = fontName;
      _bold = isBold;
      _size = fontSize;
   }

   public static final String PROPERTY_NAME = "Name";
   public static final String PROPERTY_BOLD = "Bold";
   public static final String PROPERTY_SIZE = "Size";

   public String getName() { return _name; }
   public void setName(String fontName) {
      if (_name.equals(fontName))
         return;
      String old = _name;
      _name = fontName;
      onSelfPropertyChanged(old, fontName, PROPERTY_NAME);
   }

   public boolean isBold() { return _bold; }
   public void setBold(boolean isBold) {
      if (_bold == isBold)
         return;
      boolean old = _bold;
      _bold = isBold;
      onSelfPropertyChanged(old, isBold, PROPERTY_BOLD);
   }

   public int getSize() { return _size; }
   public void setSize(int size) {
      if (_size == size)
         return;
      int old = _size;
      _size = size;
      onSelfPropertyChanged(old, size, PROPERTY_SIZE);
   }

   @Override
   public int hashCode() {
      int result = 31 + ((_name == null) ? 0 : _name.hashCode());
      result = 31 * result + (_bold ? 1231 : 1237);
      return 31 * result + _size;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      FontInfo other = (FontInfo)obj;
      if (_name == null) {
         if (other._name != null)
            return false;
      }
      return _name.equals(other._name) &&
            (_bold == other._bold) &&
            (_size == other._size);
   }

   @Override
   public String toString() {
      return "FontInfo{fontName=" + _name + ", isBold=" + _bold + ", size=" + _size + "}";
   }

}
