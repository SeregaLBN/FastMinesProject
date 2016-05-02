package fmg.core.img;

import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.Bound;
import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.swing.res.Resources;

public abstract class StaticImg<T, TImage extends Object> extends NotifyPropertyChanged implements AutoCloseable {

   public static Consumer<Runnable> DEFERR_INVOKER;

   public static final Color DefaultBkColor = Resources.DefaultBkColor;
   public static final int DefaultImageSize = 100;

   protected StaticImg(T entity) {
      this(entity, DefaultImageSize);
   }
   protected StaticImg(T entity, int widthAndHeight) {
      this(entity, widthAndHeight, (int)(widthAndHeight * 0.05)); // 5%
   }
   protected StaticImg(T entity, int widthAndHeight, int padding) {
      this(entity, new Size(widthAndHeight, widthAndHeight), new Bound(padding));
   }
   protected StaticImg(T entity, Size sizeImage, Bound padding) {
      _size = sizeImage;
      _padding = padding;
      _entity = entity;
   }

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }

   private Size _size;
   /** width and height in pixel */
   public Size getSize() { return _size; }
   public void setSize(Size value) {
      if (setProperty(_size, value, "Size")) {
         setImage(createImage());
         invalidate();
      }
   }

   /** width image */
   public int getWidth() { return getSize().width; }
   /** height image */
   public int getHeight() { return getSize().height; }

   private Bound _padding;
   /** inside padding */
   public Bound getPadding() { return _padding; }
   public void setPadding(Bound value) {
      if (value.getLeftAndRight() >= getWidth())
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getHeight())
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      if (setProperty(_padding, value, "Padding")) {
         invalidate();
      }
   }

   public T _entity;
   public T getEntity() { return _entity; }
   public void setEntity(T value) {
      if (setProperty(_entity, value, "Entity"))
         invalidate();
   }

   enum EInvalidate {
      needRedraw,
      redrawing,
      redrawed
   }
   private EInvalidate _invalidate = EInvalidate.needRedraw;

   protected abstract TImage createImage();
   private TImage _image;
   public TImage getImage() {
      if (_image == null)
         setImage(createImage());
      if (_invalidate == EInvalidate.needRedraw)
         draw();
      return _image;
   }
   protected void setImage(TImage value) {
      setProperty(_image, value, "Image");
   }

   private Color _backgroundColor = DefaultBkColor;
   /** background fill color */
   public Color getBackgroundColor() { return _backgroundColor; }
   public void setBackgroundColor(Color value) {
      if (setProperty(_backgroundColor, value, "BackgroundColor"))
         invalidate();
   }

   private Color _borderColor = Color.Maroon.clone().darker(0.5);
   public Color getBorderColor() { return _borderColor; }
   public void setBorderColor(Color value) {
      if (setProperty(_borderColor, value, "BorderColor"))
         invalidate();
   }

   private int _borderWidth = 3;
   public int getBorderWidth() { return _borderWidth; }
   public void setBorderWidth(int value) {
      if (setProperty(_borderWidth, value, "BorderWidth"))
         invalidate();
   }

   private double _rotateAngle;
   /** 0° .. +360° */
   public double getRotateAngle() { return _rotateAngle; }
   public void setRotateAngle(double value) {
      if ((value > 360) || (value < 0)) {
         value %= 360;
         if (value < 0)
            value += 360;
      }
      if (setProperty(_rotateAngle, value, "RotateAngle"))
         invalidate();
   }

   private Color _foregroundColor = Color.Aqua;
   public Color getForegroundColor() { return _foregroundColor; }
   public void setForegroundColor(Color value) {
      if (setProperty(_foregroundColor, value, "ForegroundColor")) {
         //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>(ForegroundColor, oldForegroundColor.Attenuate(160), "ForegroundColorAttenuate"));
         onPropertyChanged("ForegroundColorAttenuate");
         invalidate();
      }
   }

   public Color getForegroundColorAttenuate() { return getForegroundColor().attenuate(160); }

   private boolean _onlySyncDraw = true;
   public boolean isOnlySyncDraw() { return _onlySyncDraw; }
   public void setOnlySyncDraw(boolean value) { _onlySyncDraw = value; }

   protected void invalidate() {
      if (_invalidate == EInvalidate.redrawing)
         return;
      _invalidate = EInvalidate.needRedraw;
      onPropertyChanged("Image");
   }

   private void draw() {
      drawBegin();
      drawBody();
      drawEnd();
   }

   protected void drawBegin() { _invalidate = EInvalidate.redrawing; }
   protected abstract void drawBody();
   protected void drawEnd() { _invalidate = EInvalidate.redrawed; }

   /** Deferr notifications */
   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if (isOnlySyncDraw())
         super.onPropertyChanged(oldValue, newValue, propertyName);
      else
         DEFERR_INVOKER.accept( () -> super.onPropertyChanged(oldValue, newValue, propertyName) );
   }

   @Override
   public void close() { close(true); }

   protected void close(boolean disposing) { }
}
