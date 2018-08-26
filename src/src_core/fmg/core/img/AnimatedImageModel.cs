using System;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   public static class AnimatedImageModelConsts {

      public static readonly Color DefaultBkColor         = Color.DarkOrange;
      public static readonly Color DefaultForegroundColor = Color.Orchid; // Color.LightSeaGreen;
      public const int             DefaultImageSize = 100;
      public const int             DefaultPadding = (int)(DefaultImageSize * 0.05); // 5%

   }

   /// <summary> MVC: model. Common animated image characteristics. </summary>
   public abstract class AnimatedImageModel : IAnimatedModel {

      /// <summary> width and height in pixel </summary>
      private SizeDouble _size = new SizeDouble(AnimatedImageModelConsts.DefaultImageSize, AnimatedImageModelConsts.DefaultImageSize);
      /// <summary> inside padding. Автоматически пропорционально регулирую при измениях размеров </summary>
      private BoundDouble _padding = new BoundDouble(AnimatedImageModelConsts.DefaultPadding);
      private Color _foregroundColor = AnimatedImageModelConsts.DefaultForegroundColor;
      /// <summary> background fill color </summary>
      private Color _backgroundColor = AnimatedImageModelConsts.DefaultBkColor;
      private Color _borderColor = Color.Maroon.Darker(0.5);
      private double _borderWidth = 3;
      /// <summary> 0° .. +360° </summary>
      private double _rotateAngle;

      /** animation of polar lights */
      private bool _polarLights = true;
      /** animation direction (example: clockwise or counterclockwise for simple rotation) */
      private bool _animeDirection = true;
      private readonly AnimatedInnerModel _innerModel = new AnimatedInnerModel();

      protected bool Disposed { get; private set; }
      public event PropertyChangedEventHandler PropertyChanged;
      protected readonly NotifyPropertyChanged _notifier;

      public AnimatedImageModel() {
         _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
         _innerModel.PropertyChanged += OnInnerModelPropertyChanged;
      }

      /// <summary> width and height in pixel </summary>
      public SizeDouble Size {
         get { return _size; }
         set {
            SizeDouble old = _size;
            if (_notifier.SetProperty(ref _size, value))
               RecalcPadding(old);
         }
      }
      public void SetSize(double widhtAndHeight) { Size = new SizeDouble(widhtAndHeight, widhtAndHeight); }

      /// <summary> inside padding </summary>
      public virtual BoundDouble Padding {
         get { return _padding; }
         set {
            if (value.LeftAndRight >= Size.Width)
               throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (value.TopAndBottom >= Size.Height)
               throw new ArgumentException("Padding size is very large. Should be less than Height.");
            _notifier.SetProperty(ref _padding, value);
         }
      }
      public void SetPadding(double bound) { Padding = new BoundDouble(bound); }

      internal static BoundDouble RecalcPadding(BoundDouble padding, SizeDouble current, SizeDouble old) {
         return new BoundDouble(padding.Left   * current.Width  / old.Width,
                                padding.Top    * current.Height / old.Height,
                                padding.Right  * current.Width  / old.Width,
                                padding.Bottom * current.Height / old.Height);
      }
      private void RecalcPadding(SizeDouble old) {
         Padding = RecalcPadding(_padding, _size, old);
      }

      public Color ForegroundColor {
         get { return _foregroundColor; }
         set { _notifier.SetProperty(ref _foregroundColor, value); }
      }

      /// <summary> background fill color </summary>
      public Color BackgroundColor {
         get { return _backgroundColor; }
         set { _notifier.SetProperty(ref _backgroundColor, value); }
      }

      public Color BorderColor {
         get { return _borderColor; }
         set { _notifier.SetProperty(ref _borderColor, value); }
      }

      public double BorderWidth {
         get { return _borderWidth; }
         set {
            // _notifier.SetProperty(ref _borderWidth, value);
            if (!_borderWidth.HasMinDiff(value)) {
               double old = _borderWidth;
               _borderWidth = value;
               _notifier.OnPropertyChanged<double>(old, value, nameof(this.BorderWidth));
            }
         }
      }

      /// <summary> 0° .. +360° </summary>
      public double RotateAngle {
         get { return _rotateAngle; }
         set { _notifier.SetProperty(ref _rotateAngle, FixAngle(value)); }
      }

      /// <summary> to diapason (0° .. +360°] </summary>
      internal static double FixAngle(double value) {
         return (value >= 360)
              ?               (value % 360)
              : (value < 0)
                 ?            (value % 360) + 360
                 :             value;
      }

      public bool Animated {
         get { return _innerModel.Animated; }
         set { _innerModel.Animated = value; }
      }

      /// <summary> Overall animation period (in milliseconds) </summary>
      public long AnimatePeriod {
         get { return _innerModel.AnimatePeriod; }
         set { _innerModel.AnimatePeriod = value; }
      }

      /// <summary> Total frames of the animated period </summary>
      public int TotalFrames {
         get { return _innerModel.TotalFrames; }
         set { _innerModel.TotalFrames = value; }
      }

      public int CurrentFrame {
         get { return _innerModel.CurrentFrame; }
         set { _innerModel.CurrentFrame = value; }
      }

      public bool PolarLights {
         get { return _polarLights; }
         set { _notifier.SetProperty(ref _polarLights, value); }
      }

      public bool AnimeDirection {
         get { return _animeDirection; }
         set { _notifier.SetProperty(ref _animeDirection, value); }
      }

      protected void OnInnerModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         // refire
         _notifier.OnPropertyChanged(ev);
      }

      // <summary>  Dispose managed resources </summary>/
      protected virtual void Disposing() {
         _innerModel.PropertyChanged -= OnInnerModelPropertyChanged;
         _notifier.Dispose();
      }

      public void Dispose() {
         if (Disposed)
            return;
         Disposed = true;
         Disposing();
         GC.SuppressFinalize(this);
      }

   }

}
