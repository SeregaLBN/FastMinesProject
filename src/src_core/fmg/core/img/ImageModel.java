package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

/** MVC: model. Common image characteristics. */
public class ImageModel implements IImageModel {

   public static final Color DefaultBkColor         = Color.DarkOrange;
   public static final Color DefaultForegroundColor = Color.Orchid;
   public static final int   DefaultImageSize = 100;
   public static final int   DefaultPadding = (int)(DefaultImageSize * 0.05); // 5%

   /** width and height in pixel */
   private SizeDouble _size = new SizeDouble(DefaultImageSize, DefaultImageSize);
   /** inside padding. Автоматически пропорционально регулирую при измениях размеров */
   private BoundDouble _padding = new BoundDouble(DefaultPadding);
   /** background fill color */
   private Color _backgroundColor = DefaultBkColor;
   private Color _borderColor = Color.Maroon.clone().darker(0.5);
   private double _borderWidth = 3;
   private Color _foregroundColor = DefaultForegroundColor;
   /** 0° .. +360° */
   private double _rotateAngle;

   protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);


   public static final String PROPERTY_PADDING          = "Padding";
   public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
   public static final String PROPERTY_BORDER_COLOR     = "BorderColor";
   public static final String PROPERTY_BORDER_WIDTH     = "BorderWidth";
   public static final String PROPERTY_FOREGROUND_COLOR = "ForegroundColor";
   public static final String PROPERTY_ROTATE_ANGLE     = "RotateAngle";


   /** width and height in pixel */
   @Override
   public SizeDouble getSize() { return _size; }
   public void setSize(double widhtAndHeight) { setSize(new SizeDouble(widhtAndHeight, widhtAndHeight)) ; }
   @Override
   public void setSize(SizeDouble value) {
      SizeDouble old = _size;
      if (_notifier.setProperty(_size, value, PROPERTY_SIZE))
         recalcPadding(old);
   }

   /** inside padding */
   public BoundDouble getPadding() { return _padding; }
   public void setPadding(double bound) { setPadding(new BoundDouble(bound)); }
   public void setPadding(BoundDouble value) {
      if (value.getLeftAndRight() >= getSize().width)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getSize().height)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      BoundDouble paddingNew = new BoundDouble(value.left, value.top, value.right, value.bottom);
      _notifier.setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }
   static BoundDouble recalcPadding(BoundDouble padding, SizeDouble current, SizeDouble old) {
      return new BoundDouble(padding.left   * current.width  / old.width,
                             padding.top    * current.height / old.height,
                             padding.right  * current.width  / old.width,
                             padding.bottom * current.height / old.height);
   }
   private void recalcPadding(SizeDouble old) {
      BoundDouble paddingNew = recalcPadding(_padding, _size, old);
      _notifier.setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }

   /** background fill color */
   public Color getBackgroundColor() { return _backgroundColor; }
   public void setBackgroundColor(Color value) {
      _notifier.setProperty(_backgroundColor, value, PROPERTY_BACKGROUND_COLOR);
   }

   public Color getBorderColor() { return _borderColor; }
   public void setBorderColor(Color value) {
      _notifier.setProperty(_borderColor, value, PROPERTY_BORDER_COLOR);
   }

   public double getBorderWidth() { return _borderWidth; }
   public void setBorderWidth(double value) {
      if (!DoubleExt.hasMinDiff(_borderWidth, value)) {
         double old = _borderWidth;
         _borderWidth = value;
         _notifier.onPropertyChanged(old, value, PROPERTY_BORDER_WIDTH);
      }
   }

   public Color getForegroundColor() { return _foregroundColor; }
   public void setForegroundColor(Color value) {
      _notifier.setProperty(_foregroundColor, value, PROPERTY_FOREGROUND_COLOR);
   }

   /** 0° .. +360° */
   public double getRotateAngle() { return _rotateAngle; }
   public void setRotateAngle(double value) {
      value = fixAngle(value);
      _notifier.setProperty(_rotateAngle, value, PROPERTY_ROTATE_ANGLE);
   }

   /** to diapason (0° .. +360°] */
   public static double fixAngle(double value) {
      return (value >= 360)
           ?              (value % 360)
           : (value < 0)
              ?           (value % 360) + 360
              :            value;
   }

   @Override
   public void close() {
      _notifier.close();
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
