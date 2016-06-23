using System;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   public static class StaticImgConsts {

      public static Action<Action> DeferrInvoker;
      public static readonly Color DefaultBkColor = new Color(0xFF, 0xFF, 0x8C, 0x00);

   }

   /// <summary> Abstract, platform independent, image characteristics </summary>
   /// <typeparam name="T">the entity of image</typeparam>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class StaticImg<T, TImage> : NotifyPropertyChanged
      where TImage : class
   {
      public const int DefaultImageSize = 100;

      protected StaticImg(T entity) {
         _entity = entity;
         _size = new Size(DefaultImageSize, DefaultImageSize);
         _padding = new Bound((int)(DefaultImageSize * 0.05)); // 5%
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

      private enum EInvalidate {
         NeedRedraw,
         Redrawing,
         Redrawed
      }
      private EInvalidate _invalidate = EInvalidate.NeedRedraw;
      protected abstract TImage CreateImage();
      private TImage _image;
      public TImage Image {
         get {
            //LoggerSimple.Put("getImage: {0}", Entity);
            if (_image == null) {
               Image = CreateImage();
               _invalidate = EInvalidate.NeedRedraw;
            }
            if (_invalidate == EInvalidate.NeedRedraw)
               Draw();
            return _image;
         }
         protected set {
            SetProperty(ref _image, value);
         }
      }

      private Color _backgroundColor = StaticImgConsts.DefaultBkColor;
      /// <summary> background fill color </summary>
      public Color BackgroundColor {
         get { return _backgroundColor; }
         set {
            if (SetProperty(ref _backgroundColor, value))
               Invalidate();
         }
      }

      private Color _borderColor = Color.Maroon.Darker(0.5);
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

      public Color ForegroundColorAttenuate => ForegroundColor.Brighter(0.25);

      public bool DeferredNotifications { get; set; } = true;
      public bool SyncDraw { get; set; } = false;

      protected void Invalidate() {
         if (_invalidate == EInvalidate.Redrawing)
            return;
         if (_invalidate == EInvalidate.NeedRedraw)
            return;
         _invalidate = EInvalidate.NeedRedraw;
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
         if (!DeferredNotifications)
            base.OnPropertyChanged(sender, ev);
         else
            StaticImgConsts.DeferrInvoker(() => base.OnPropertyChanged(sender, ev));
      }

   }

}
