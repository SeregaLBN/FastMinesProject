package fmg.swing.res.img;

import javax.swing.Icon;

import fmg.common.Color;
import fmg.common.geom.Bound;
import fmg.common.geom.Size;
import fmg.swing.res.Resources;

public abstract class StaticImg<T, TImage extends Icon> //implements INotifyPropertyChanged
   {
      public static final Color DefaultBkColor = Resources.DefaultBkColor;
      public static final int DefaultImageSize = 100;

      protected StaticImg(T entity) {
         this(entity, DefaultImageSize);
      }

      protected StaticImg(T entity, int widthAndHeight) {
         this(entity, widthAndHeight, null);
      }

      protected StaticImg(T entity, int widthAndHeight, Integer padding) {
         this(entity,
              new Size(widthAndHeight, widthAndHeight),
              new Bound(padding != null
                 ? (int)padding
                 : (int)(widthAndHeight * 0.05) // 5%
         ));
      }

      protected StaticImg(T entity, Size sizeImage, Bound padding) {
         _entity = entity;
         _size = sizeImage;
         _padding = padding;
      }
      
      <TI>boolean SetProperty(TI storage, TI value, String propertyName) {
         return false;
      }
      <TI>boolean SetPropertyForce(TI storage, TI value, String propertyName) {
         return false;
      }

      private Size _size;
      /// <summary> width and height in pixel </summary>
      public Size getSize() { return _size; }
      public void setSize(Size value) {
        if (SetProperty(_size, value, "Size")) {
           _image = null;
           DrawAsync();
        }
     }

      /// <summary> width image </summary>
      public int getWidth() { return getSize().width; }
      /// <summary> height image </summary>
      public int getHeight() { return getSize().height; }

      private Bound _padding;
      /// <summary> inside padding </summary>
      public Bound getPadding() { return _padding; }
      public void setPadding(Bound value) {
        if (value.getLeftAndRight() >= getWidth())
           throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
        if (value.getTopAndBottom() >= getHeight())
           throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
        if (SetProperty(_padding, value, "Padding")) {
           DrawAsync();
        }
     }

      public T _entity;
      public T getEntity() { return _entity; }
      public void setEntity(T value) {
        SetProperty(_entity, value, "Entity");
     }

      private TImage _image;
      protected TImage getImageInternal() { return _image; }
      public TImage getImage() {
        if (getOnlySyncDraw() && (_scheduledDraw || (_image == null)))
           DrawSync();
        return _image;
     }
     protected void setImage(TImage value) {
        SetPropertyForce(_image, value, "Image");
     }

      private Color _backgroundColor = DefaultBkColor;
      /// <summary> background fill color </summary>
      public Color getBackgroundColor() { return _backgroundColor; }
      public void setBackgroundColor(Color value) {
        if (SetProperty(_backgroundColor, value, "BackgroundColor"))
           DrawAsync();
      }

      private Color _borderColor = Color.Red;
      public Color getBorderColor() { return _borderColor; }
      public void setBorderColor(Color value) {
        if (SetProperty(_borderColor, value, "BorderColor"))
           DrawAsync();
      }

      private int _borderWidth = 3;
      public int getBorderWidth() { return _borderWidth; }
      public void setBorderWidth(int value) {
        if (SetProperty(_borderWidth, value, "BorderWidth"))
           DrawAsync();
      }

      private double _rotateAngle;
      /// <summary> -360° .. 0° .. +360° </summary>
      public double getRotateAngle() { return _rotateAngle; }
      public void setRotateAngle(double value) {
        if (SetProperty(_rotateAngle, value, "RotateAngle"))
           DrawAsync();
      }

      protected Color _fillColor = Color.Aqua;
      public Color getFillColor() { return _fillColor; }
      public void setFillColor(Color value) {
        if (SetProperty(_fillColor, value, "FillColor")) {
           //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>("FillColorAttenuate", ..., ...));
           //OnPropertyChanged(this, new PropertyChangedEventArgs("FillColorAttenuate"));
           DrawAsync();
        }
      }

      public Color getFillColorAttenuate() { return getFillColor().attenuate(160); }

      public boolean _onlySyncDraw;
      public boolean getOnlySyncDraw() { return _onlySyncDraw; }
      public void setEntity(boolean value) {
        SetProperty(_onlySyncDraw, value, "OnlySyncDraw");
     }

      private boolean _scheduledDraw;
      /// <summary> schedule drawing (async operation) </summary>
      protected void DrawAsync() {
         if (_scheduledDraw)
            return;

         _scheduledDraw = true;
         if (getOnlySyncDraw())
            return;

         //AsyncRunner.InvokeFromUiLater(DrawSync, CoreDispatcherPriority.Low);
      }

      protected void DrawSync() {
         DrawBegin();
         DrawBody();
         DrawEnd();
      }

      protected void DrawBegin() {
         _scheduledDraw = false;
      }

      protected abstract void DrawBody();

      protected void DrawEnd() { }

   }
