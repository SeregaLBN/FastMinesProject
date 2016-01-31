using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
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
               Redraw();
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
               Redraw();
            }
         }
      }

      private T _entity;
      public T Entity {
         get { return _entity; }
         set {
            if (SetProperty(ref _entity, value))
               Redraw();
         }
      }

      protected abstract TImage CreateImage();
      private TImage _image;
      public TImage Image {
         get {
            //LoggerSimple.Put("getImage: {0}", Entity);
            if (_image == null)
               Image = CreateImage();
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
               Redraw();
         }
      }

      private Color _borderColor = Color.Red;
      public Color BorderColor {
         get { return _borderColor; }
         set {
            if (SetProperty(ref _borderColor, value))
               Redraw();
         }
      }

      private int _borderWidth = 3;
      public int BorderWidth {
         get { return _borderWidth; }
         set {
            if (SetProperty(ref _borderWidth, value))
               Redraw();
         }
      }

      private double _rotateAngle;
      /// <summary> -360° .. 0° .. +360° </summary>
      public double RotateAngle {
         get { return _rotateAngle; }
         set {
            if (SetProperty(ref _rotateAngle, value))
               Redraw();
         }
      }

      private Color _foregroundColor = Color.Aqua;
      public Color ForegroundColor {
         get { return _foregroundColor; }
         set {
            if (SetProperty(ref _foregroundColor, value)) {
               //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>(ForegroundColor, oldForegroundColor.Attenuate(160), "ForegroundColorAttenuate"));
               OnPropertyChanged(this, new PropertyChangedEventArgs("ForegroundColorAttenuate"));
               Redraw();
            }
         }
      }

      public Color ForegroundColorAttenuate => ForegroundColor.Attenuate(160);

      public bool OnlySyncDraw { get; set; } = Windows.ApplicationModel.DesignMode.DesignModeEnabled;

      private bool _deferredRedraw;
      protected void Redraw() {
         if (DeferredOn) {
            _deferredRedraw = true;
            return;
         }

         if (OnlySyncDraw)
            DrawSync();
         else
            DrawAsync();
      }

      private bool _sheduledDraw;
      /// <summary> schedule drawing (async operation) </summary>
      private void DrawAsync() {
         if (_sheduledDraw)
            return;
         _sheduledDraw = true;
         AsyncRunner.InvokeFromUiLater(() => { DrawSync(); _sheduledDraw = false; }, CoreDispatcherPriority.Low);
      }

      private void DrawSync() {
         using (Deferring(false)) {
            //LoggerSimple.Put("> DrawBegin: {0}", Entity);
            DrawBegin();
            DrawBody();
            DrawEnd();
            //LoggerSimple.Put("< DrawEnd: {0}", Entity);
         }
      }

      protected virtual void DrawBegin() { }

      protected abstract void DrawBody();

      protected virtual void DrawEnd() { }

      protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(this, sender), "WTF??");

         if (!DeferredOn) {
            base.OnPropertyChanged(sender, ev);
            return;
         }

         _deferredPropertyChanged[ev.PropertyName] = ev;
      }
      private readonly Dictionary<string, PropertyChangedEventArgs> _deferredPropertyChanged = new Dictionary<string, PropertyChangedEventArgs>();

      private bool _deferredOn;
      /// <summary> Deferr notifications and rendering </summary>
      private bool DeferredOn {
         get { return _deferredOn; }
         set {
            if (_deferredOn == value)
               return;

            _deferredOn = value;
            if (_deferredOn)
               return;

            if (_deferredRedraw) {
               _deferredRedraw = false;
               Redraw();
            }

            if (_deferredPropertyChanged.Any()) {
               foreach(var key in _deferredPropertyChanged.Keys) {
                  base.OnPropertyChanged(this, _deferredPropertyChanged[key]);
               }
               _deferredPropertyChanged.Clear();
            }
         }
      }

      private class DeferredLock : IDisposable {
         private readonly StaticImg<T, TImage> _owner;
         private readonly bool _locked;
         private readonly bool _redrawAfter;
         public DeferredLock(StaticImg<T, TImage> owner, bool redrawAfter) {
            _redrawAfter = redrawAfter;
            if ((_owner = owner).DeferredOn)
               return;
            _owner.DeferredOn = true;
            _locked = true;
         }
         public void Dispose() {
            if (_redrawAfter)
               _owner.Redraw();
            if (!_locked)
               return;
            _owner.DeferredOn = false;
         }
      }

      /// <summary> Deferr notifications and rendering </summary>
      public IDisposable Deferring(bool redrawAfter = true) {
         return new DeferredLock(this, redrawAfter);
      }

      public void Dispose() {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing) { }
   }
}