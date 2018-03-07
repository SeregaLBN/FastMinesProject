package fmg.core.img;

import fmg.common.Color;
import fmg.common.geom.Bound;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/** MVC: model. Common image characteristics. */
public class ImageProperties extends NotifyPropertyChanged implements IImageModel {

   public static final Color DefaultBkColor = new Color(0xFF, 0xFF, 0x8C, 0x00);
   public static final Color DefaultForegroundColor = Color.Orchid;
   public static final int DefaultImageSize = 100;
   public static final int DefaultPaddingInt = (int)(DefaultImageSize * 0.05); // 5%

   public ImageProperties() {
      _size = new Size(DefaultImageSize, DefaultImageSize);
      _padding = new BoundDouble(DefaultPaddingInt, DefaultPaddingInt, DefaultPaddingInt, DefaultPaddingInt);
   }

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }

   public static final String PROPERTY_PADDING          = "Padding";
   public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
   public static final String PROPERTY_BORDER_COLOR     = "BorderColor";
   public static final String PROPERTY_BORDER_WIDTH     = "BorderWidth";
   public static final String PROPERTY_FOREGROUND_COLOR = "ForegroundColor";
   public static final String PROPERTY_ROTATE_ANGLE     = "RotateAngle";

   private Size _size;
   /** width and height in pixel */
   @Override
   public Size getSize() { return _size; }
   public void setSize(int widhtAndHeight) { setSize(new Size(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(Size value) {
      Size old = _size;
      if (setProperty(_size, value, PROPERTY_SIZE))
         recalcPadding(old);
   }

   private BoundDouble _padding;
   /** inside padding */
   public Bound getPadding() { return new Bound((int)_padding.left, (int)_padding.top, (int)_padding.right, (int)_padding.bottom); }
   public void setPadding(int bound) { setPadding(new Bound(bound)); }
   public void setPadding(Bound value) {
      if (value.getLeftAndRight() >= getSize().width)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getSize().height)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      BoundDouble paddingNew = new BoundDouble(value.left, value.top, value.right, value.bottom);
      setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }
   static BoundDouble recalcPadding(BoundDouble padding, Size current, Size old) {
      return new BoundDouble(padding.left   * current.width  / old.width,
                             padding.top    * current.height / old.height,
                             padding.right  * current.width  / old.width,
                             padding.bottom * current.height / old.height);
   }
   private void recalcPadding(Size old) {
      BoundDouble paddingNew = recalcPadding(_padding, _size, old);
      setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }

   private Color _backgroundColor = DefaultBkColor;
   /** background fill color */
   public Color getBackgroundColor() { return _backgroundColor; }
   public void setBackgroundColor(Color value) {
      setProperty(_backgroundColor, value, PROPERTY_BACKGROUND_COLOR);
   }

   private Color _borderColor = Color.Maroon.clone().darker(0.5);
   public Color getBorderColor() { return _borderColor; }
   public void setBorderColor(Color value) {
      setProperty(_borderColor, value, PROPERTY_BORDER_COLOR);
   }

   private int _borderWidth = 3;
   public int getBorderWidth() { return _borderWidth; }
   public void setBorderWidth(int value) {
      setProperty(_borderWidth, value, PROPERTY_BORDER_WIDTH);
   }

   private Color _foregroundColor = DefaultForegroundColor;
   public Color getForegroundColor() { return _foregroundColor; }
   public void setForegroundColor(Color value) {
      setProperty(_foregroundColor, value, PROPERTY_FOREGROUND_COLOR);
   }

   private double _rotateAngle;
   /** 0° .. +360° */
   public double getRotateAngle() { return _rotateAngle; }
   public void setRotateAngle(double value) {
      value = fixAngle(value);
      setProperty(_rotateAngle, value, PROPERTY_ROTATE_ANGLE);
   }

   /** to diapason (0° .. +360°] */
   public static double fixAngle(double value) {
      return (value >= 360)
           ?              (value % 360)
           : (value < 0)
              ?           (value % 360) + 360
              :            value;
   }

}
