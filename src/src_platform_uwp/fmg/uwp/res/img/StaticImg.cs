using System;
using System.ComponentModel;
using Windows.UI.Core;
using fmg.common;
using fmg.common.geom;
using FastMines.Common;
using FastMines.Presentation.Notyfier;

namespace fmg.uwp.res.img {

   public abstract class StaticImg<T, TImage> : NotifyPropertyChanged
      where TImage : class
   {
      public static readonly Color DefaultBkColor = Resources.DefaultBkColor;
      public const int DefaultImageSize = 100;

      protected StaticImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : this(entity,
              new Size(widthAndHeight, widthAndHeight),
              new Bound(padding.HasValue
                 ? padding.Value
                 : (int)(widthAndHeight * 0.05) // 5%
         ))
      { }

      protected StaticImg(T entity, Size sizeImage, Bound padding) {
         Entity = entity;
         _size = sizeImage;
         _padding = padding;
      }

      private Size _size;
      /// <summary> width and height in pixel </summary>
      public Size Size {
         get { return _size; }
         set {
            if (SetProperty(ref _size, value)) {
               Image = null;
               NeedRedraw();
            }
         }
      }

      /// <summary> width image </summary>
      public int Width => Size.width;
      /// <summary> height image </summary>
      public int Height => Size.height;

      private Bound _padding;
      /// <summary> inside padding </summary>
      public Bound Padding {
         get { return _padding; }
         set {
            if (value.LeftAndRight >= Width)
               throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (value.TopAndBottom >= Height)
               throw new ArgumentException("Padding size is very large. Should be less than Height.");
            if (SetProperty(ref _padding, value)) {
               NeedRedraw();
            }
         }
      }

      public T Entity { get; protected set; }

      protected abstract TImage CreateImage();
      private TImage _image;
      public TImage Image {
         get {
            if (_image == null)
               Image = CreateImage();
            if (OnlySyncDraw && _scheduledDraw)
               DrawSync();
            return _image;
         }
         protected set {
            SetProperty(ref _image, value);
         }
      }

      private Color _backgroundColor = DefaultBkColor;
      /// <summary> background fill color </summary>
      public Color BackgroundColor {
         get { return _backgroundColor; }
         set {
            if (SetProperty(ref _backgroundColor, value))
               NeedRedraw();
         }
      }

      private Color _borderColor = Color.Red;
      public Color BorderColor {
         get { return _borderColor; }
         set {
            if (SetProperty(ref _borderColor, value))
               NeedRedraw();
         }
      }

      private int _borderWidth = 3;
      public int BorderWidth {
         get { return _borderWidth; }
         set {
            if (SetProperty(ref _borderWidth, value))
               NeedRedraw();
         }
      }

      private double _rotateAngle;
      /// <summary> -360° .. 0° .. +360° </summary>
      public double RotateAngle {
         get { return _rotateAngle; }
         set {
            if (SetProperty(ref _rotateAngle, value))
               NeedRedraw();
         }
      }

      private Color _foregroundColor = Color.Aqua;
      public Color ForegroundColor {
         get { return _foregroundColor; }
         set {
            if (SetProperty(ref _foregroundColor, value)) {
               //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>(ForegroundColor, oldForegroundColor.Attenuate(160), "ForegroundColorAttenuate"));
               OnPropertyChanged(this, new PropertyChangedEventArgs("ForegroundColorAttenuate"));
               NeedRedraw();
            }
         }
      }

      public Color ForegroundColorAttenuate => ForegroundColor.Attenuate(160);

      public bool OnlySyncDraw { get; set; } = Windows.ApplicationModel.DesignMode.DesignModeEnabled;

      protected void NeedRedraw() {
         if (OnlySyncDraw)
         {
            _scheduledDraw = true;
            OnPropertyChanged("Image");
         } else {
            DrawAsync();
         }
      }

      private bool _scheduledDraw;
      /// <summary> schedule drawing (async operation) </summary>
      private void DrawAsync() {
         if (_scheduledDraw)
            return;
         _scheduledDraw = true;
         AsyncRunner.InvokeFromUiLater(DrawSync, CoreDispatcherPriority.Low);
      }

      protected virtual void DrawSync() {
         DrawBegin();
         DrawBody();
         DrawEnd();
      }

      protected virtual void DrawBegin() {
         _scheduledDraw = false;
      }

      protected abstract void DrawBody();

      protected virtual void DrawEnd() { }

   }
}