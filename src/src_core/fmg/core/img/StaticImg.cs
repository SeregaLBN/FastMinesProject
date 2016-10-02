using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   public static class StaticImgConsts {

      public static Action<Action> DeferrInvoker;
      public static readonly Color DefaultBkColor = new Color(0xFF, 0xFF, 0x8C, 0x00);
      public static readonly Color DefaultForegroundColor = Color.LightSeaGreen;

   }

   /// <summary> Abstract, platform independent, image characteristics </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class StaticImg<TImage> : NotifyPropertyChanged
      where TImage : class
   {
      public const int DefaultImageSize = 100;
      public const int DefaultPaddingInt = (int)(DefaultImageSize* 0.05); // 5%

      protected StaticImg() {
         _size = new Size(DefaultImageSize, DefaultImageSize);
         _padding = new Bound(DefaultPaddingInt);
      }

      private Size _size;
      /// <summary> width and height in pixel </summary>
      public Size Size {
         get { return _size; }
         set {
            var old = _size;
            if (SetProperty(ref _size, value)) {
               Image = null;
               //Invalidate();
            }
         }
      }

      private Bound _padding;
      /// <summary> inside padding </summary>
      public Bound Padding {
         get { return _padding; }
         set {
            if (value.LeftAndRight >= Size.Width)
               throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (value.TopAndBottom >= Size.Height)
               throw new ArgumentException("Padding size is very large. Should be less than Height.");
            if (SetProperty(ref _padding, value)) {
               Invalidate();
            }
         }
      }
      public int PaddingInt {
         set { Padding = new Bound(value); }
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
            value = FixAngle(value);
            if (SetProperty(ref _rotateAngle, value))
               Invalidate();
         }
      }

      /// <summary> to diapason (0° .. +360°] </summary>
      protected static double FixAngle(double value) {
         return (value >= 360)
              ?               (value % 360)
              : (value < 0)
                 ?            (value % 360) + 360
                 :             value;
      }


      private Color _foregroundColor = StaticImgConsts.DefaultForegroundColor;
      public Color ForegroundColor {
         get { return _foregroundColor; }
         set {
            if (SetProperty(ref _foregroundColor, value))
               Invalidate();
         }
      }

      public bool DeferredNotifications { get; set; } = true;
      private readonly Dictionary<string, PropertyChangedEventArgs> DeferredNotificationsMap = new Dictionary<string, PropertyChangedEventArgs>();

      protected void Invalidate() {
         if (_invalidate == EInvalidate.Redrawing)
            return;
         //if (_invalidate == EInvalidate.NeedRedraw)
         //   return;
         _invalidate = EInvalidate.NeedRedraw;
         OnSelfPropertyChanged(nameof(this.Image));
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

      protected override void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
         if (!DeferredNotifications) {
            base.OnSelfPropertyChanged(ev);
            //OnPropertyChangingAfter(true, ev);
         } else {
            bool first = DeferredNotificationsMap.Any();
            if (!DeferredNotificationsMap.ContainsKey(ev.PropertyName))
               DeferredNotificationsMap.Add(ev.PropertyName, ev);
            else
               DeferredNotificationsMap[ev.PropertyName] = ev;
            if (first)
               StaticImgConsts.DeferrInvoker(() => {
                  foreach (PropertyChangedEventArgs item in DeferredNotificationsMap.Values.ToList()) {
                     base.OnSelfPropertyChanged(item);
                     //OnSelfPropertyChangedAfter(false, item);
                  }
                  DeferredNotificationsMap.Clear();
               });
         }
      }

      //protected abstract void OnSelfPropertyChangedAfter(bool sync, object sender, PropertyChangedEventArgs ev);

   }

}
