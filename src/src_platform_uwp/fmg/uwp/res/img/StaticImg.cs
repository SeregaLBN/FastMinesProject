using System;
using System.ComponentModel;
using Windows.UI.Core;
using fmg.common;
using fmg.common.geom;
using FastMines.Common;
using FastMines.Presentation.Notyfier;

namespace fmg.uwp.res.img {

   public abstract class StaticImg<T, TImage> : NotifyPropertyChanged, IDisposable
      where TImage : class
   {
      public static readonly Color DefaultBkColor = Resources.DefaultBkColor;
      public const int DefaultImageSize = 100;

      protected StaticImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : this(entity,
              new Size(widthAndHeight, widthAndHeight),
              new Bound(padding ?? (int)(widthAndHeight * 0.05) // 5%
         ))
      { }

      protected StaticImg(T entity, Size sizeImage, Bound padding) {
         _size = sizeImage;
         _padding = padding;
         _entity = entity;
      }

      private Size _size;
      /// <summary> width and height in pixel </summary>
      public Size Size {
         get { return _size; }
         set {
            if (SetProperty(ref _size, value)) {
               //LoggerSimple.Put("setSize: {0}", Entity);
               Image = CreateImage();
               Invalidate();
            }
         }
      }

      /// <summary> width image </summary>
      public int Width => Size.Width;
      /// <summary> height image </summary>
      public int Height => Size.Height;

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
               Invalidate();
            }
         }
      }

      private T _entity;
      public T Entity {
         get { return _entity; }
         set {
            if (SetProperty(ref _entity, value))
               Invalidate();
         }
      }

      enum EInvalidate {
         Invalidate,
         Redrawing,
         Redrawed
      }
      private EInvalidate _invalidate = EInvalidate.Invalidate;
      protected abstract TImage CreateImage();
      private TImage _image;
      public TImage Image {
         get {
            //LoggerSimple.Put("getImage: {0}", Entity);
            if (_image == null)
               Image = CreateImage();
            if (_invalidate == EInvalidate.Invalidate)
               Draw();
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
               Invalidate();
         }
      }

      private Color _borderColor = Color.Red;
      public Color BorderColor {
         get { return _borderColor; }
         set {
            if (SetProperty(ref _borderColor, value))
               Invalidate();
         }
      }

      private int _borderWidth = 3;
      public int BorderWidth {
         get { return _borderWidth; }
         set {
            if (SetProperty(ref _borderWidth, value))
               Invalidate();
         }
      }

      private double _rotateAngle;
      /// <summary> 0° .. +360° </summary>
      public double RotateAngle {
         get { return _rotateAngle; }
         set {
            if (value > 360 || value < 0) {
               value %= 360;
               if (value < 0)
                  value += 360;
            }
            if (SetProperty(ref _rotateAngle, value))
               Invalidate();
         }
      }

      private Color _foregroundColor = Color.Aqua;
      public Color ForegroundColor {
         get { return _foregroundColor; }
         set {
            if (SetProperty(ref _foregroundColor, value)) {
               //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>(ForegroundColor, oldForegroundColor.Attenuate(160), "ForegroundColorAttenuate"));
               OnPropertyChanged(this, new PropertyChangedEventArgs("ForegroundColorAttenuate"));
               Invalidate();
            }
         }
      }

      public Color ForegroundColorAttenuate => ForegroundColor.Attenuate(160);

      public bool OnlySyncDraw { get; set; } = Windows.ApplicationModel.DesignMode.DesignModeEnabled;

      protected void Invalidate() {
         if (_invalidate != EInvalidate.Redrawed)
            return;
         _invalidate = EInvalidate.Invalidate;
         OnPropertyChanged("Image");
      }

      private void Draw() {
         //LoggerSimple.Put("> DrawBegin: {0}", Entity);
         DrawBegin();
         DrawBody();
         DrawEnd();
         //LoggerSimple.Put("< DrawEnd: {0}", Entity);
      }

      protected virtual void DrawBegin() { _invalidate = EInvalidate.Redrawing; }
      protected abstract void DrawBody();
      protected virtual void DrawEnd() { _invalidate = EInvalidate.Redrawed; }

      /// <summary> Deferr notifications </summary>
      protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (OnlySyncDraw)
            base.OnPropertyChanged(sender, ev);
         else
            AsyncRunner.InvokeFromUiLater(() => base.OnPropertyChanged(sender, ev), CoreDispatcherPriority.Normal);
      }

      public void Dispose() {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing) { }

   }
}
